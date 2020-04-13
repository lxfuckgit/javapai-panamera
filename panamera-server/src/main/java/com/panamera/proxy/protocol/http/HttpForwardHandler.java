package com.panamera.proxy.protocol.http;

import com.panamera.proxy.ippool.DefaultIpPool;
import com.panamera.proxy.module.forward.ForwardStrategy;
import com.panamera.proxy.module.forward.HttpClientForwardStrategy;

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
import io.netty.handler.timeout.IdleStateEvent;
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

		/* 内置功能 */
//		String event = request.headers().get("event");
		String event = request.uri();
		if (event.startsWith("/")) {
			event = request.uri().substring(1, request.uri().length());
		}
		if ("list_proxy_ips".equalsIgnoreCase(event)) {
			listProxyIps(ctx, request);

		} else if ("append_proxy_ips".equalsIgnoreCase(event)) {
			appendProxyIps(ctx, request);

		} else if ("refresh_proxy_ips".equalsIgnoreCase(event)) {
			refreshProxyIps(ctx, request);

		} else if ("forward_proxy_server".equalsIgnoreCase(event)) {
			ForwardStrategy forward = new HttpClientForwardStrategy();
			ctx.writeAndFlush(forward.proxyForward(request)).addListener(ChannelFutureListener.CLOSE);

		} else {
			forwardServer(ctx, request);
		}
	}
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		// TODO Auto-generated method stub
		IdleStateEvent event = (IdleStateEvent) evt;
		switch (event.state()) {
		case READER_IDLE:
			System.out.println("Channel[" + ctx.channel() + "]已经好久没收到客户端的消息了！");
//			eventType = "读空闲";
//			readIdleTimes++; // 读空闲的计数加1
			break;
		case WRITER_IDLE:
//			eventType = "写空闲";
			System.out.println("Channel[" + ctx.channel() + "]已经好久没发送客户端的消息了！");
			// 不处理
			break;
		case ALL_IDLE:
//			eventType = "读写空闲";
			// 不处理
			break;
		}
		
//		System.out.println(ctx.channel().remoteAddress() + "超时事件：" +eventType);
//        if(readIdleTimes > 3){
//            System.out.println(" [server]读空闲超过3次，关闭连接");
//            ctx.channel().writeAndFlush("you are out");
//            ctx.channel().close();
//        }
//		super.userEventTriggered(ctx, evt);
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

	private FullHttpResponse getHttpResponse(String content) {
		// TODO Auto-generated method stub
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
				Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
		return response;
	}

	private void getHttpResponseAndFlush(ChannelHandlerContext ctx, String content) {
		// TODO Auto-generated method stub
		ctx.writeAndFlush(getHttpResponse(content)).addListener(ChannelFutureListener.CLOSE);
	}

}
