package com.panamera.proxy.protocol.http;

import java.io.IOException;

import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.panamera.proxy.ippool.DefaultIpPool;
import com.panamera.proxy.ippool.IpPool;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

/**
 * 转发到代理服务器。<br>
 * 
 * <br>
 * 
 * <br>
 * 
 * @author lx
 * 
 */
public class HttpForwardHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("--------channelActive");
		super.channelActive(ctx);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		// TODO Auto-generated method stub
		if (!request.decoderResult().isSuccess()) {
//			sendError(ctx, BAD_REQUEST);
			System.out.println("BAD_REQUEST");
			return;
		}

		String url = request.uri();
		String param = null;
		// 分离uri 和参数
		String paramSplitRes[] = url.split("\\?");
		if (paramSplitRes.length >= 2) {
			url = paramSplitRes[0];
			param = paramSplitRes[1];
		}

		System.out.println("request.method:" + request.method());
		System.out.println("request.uri:" + request.uri());
		System.out.println("request.head :" + request.headers());
		System.out.println("param :" + param);
		System.out.println("Remote Address:" + ctx.channel().remoteAddress());

		/* 内置功能 */
//		String event = request.headers().get("event");
		String event = request.uri().substring(1, request.uri().length());
		if ("list_proxy_ips".equalsIgnoreCase(event)) {
			listProxyIps(ctx, request);
			
		} else if ("append_proxy_ips".equalsIgnoreCase(event)) {
			appendProxyIps(ctx, request);
			
		} else if ("refresh_proxy_ips".equalsIgnoreCase(event)) {
			refreshProxyIps(ctx, request);
			
		} else if ("forward_proxy_server".equalsIgnoreCase(event)) {
			forwardProxyServer(ctx, request);
			
		} else {
			forwardServer(ctx, request);
		}
	}
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Channel[" + ctx.channel() + "]已经好久没收到客户端的消息了！");
		super.userEventTriggered(ctx, evt);
	}

	/**
	 * 
	 * @param ctx
	 * @param request
	 */
	private void listProxyIps(ChannelHandlerContext ctx, FullHttpRequest request) {
		// TODO Auto-generated method stub

	}

	private void appendProxyIps(ChannelHandlerContext ctx, FullHttpRequest request) {
		// TODO Auto-generated method stub

	}

	private void refreshProxyIps(ChannelHandlerContext ctx, FullHttpRequest request) {
		// TODO Auto-generated method stub
		DefaultIpPool.getInstance().refreshIpPool();

		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
				Unpooled.copiedBuffer("refresh proxy ips success!\r\n", CharsetUtil.UTF_8));
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * 转发请求.
	 * 
	 * @param ctx
	 * @param request
	 */
	private void forwardServer(ChannelHandlerContext ctx, FullHttpRequest request) {
		// TODO Auto-generated method stub

	}

	/**
	 * 通过代理转发请求.<br>
	 * 当Server接受到http请求后，Server将根据相应的策略(默认按加入顺序)将http请求转发到代理服务器上。<br>
	 * 代理服务器IP资源来源于Ip池{@link IpPool}。<br>
	 * 
	 * @param ctx
	 * @param request
	 */
	private void forwardProxyServer(ChannelHandlerContext ctx, FullHttpRequest request) {
		// TODO Auto-generated method stub
		String[] proxyAddress = DefaultIpPool.getInstance().nextIp().split(":");
		String content = httpClientProxy(proxyAddress[0], Integer.valueOf(proxyAddress[1]));
		
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
				Unpooled.copiedBuffer("forwardProxyServer!\r\n", CharsetUtil.UTF_8));
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	public String httpClientProxy(String proxyHost, int proxyPort) {
		CloseableHttpClient httpClient = HttpClientUtil.getHttpClient(true, proxyHost, proxyPort, "", "", 1000);
		HttpGet httpGet = new HttpGet("http://ip111.cn");
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpGet);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		if (200 == response.getStatusLine().getStatusCode()) {
			try {
				return EntityUtils.toString(response.getEntity(), "UTF-8");
			} catch (ParseException | IOException e) {
				e.printStackTrace();
			}
		} else {
			return null;
		}
		
		return null;

	};

}
