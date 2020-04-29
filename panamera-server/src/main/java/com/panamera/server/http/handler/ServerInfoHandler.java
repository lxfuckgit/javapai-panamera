package com.panamera.server.http.handler;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.panamera.proxy.ippool.DefaultIpPool;
import com.panamera.proxy.module.forward.ForwardStrategy;
import com.panamera.proxy.module.forward.HttpClientForwardStrategy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

/**
 * Server Setting Info.
 * <br>
 * 
 * @author lx
 * 
 */
public class ServerInfoHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
	private final Logger logger = LoggerFactory.getLogger(ServerInfoHandler.class);
	 
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("--------channelActive");
		super.channelActive(ctx);
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("--------channelRead");
		super.channelRead(ctx, msg);
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
		if ("get_proxy_ips".equalsIgnoreCase(event)) {
//			request.headers().get(HttpHeaderNames.CONTENT_TYPE);
			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
					Unpooled.copiedBuffer(DefaultIpPool.getInstance().nextIp(), CharsetUtil.UTF_8));
			response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
			ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);

		} else if ("list_proxy_ips".equalsIgnoreCase(event)) {
			listProxyIps(ctx, request);

		} else if ("append_proxy_ips".equalsIgnoreCase(event)) {
			appendProxyIps(ctx, request);

		} else if ("refresh_proxy_ips".equalsIgnoreCase(event)) {
			refreshProxyIps(ctx, request);

		} else if ("forward_proxy_server".equalsIgnoreCase(event)) {
			ForwardStrategy forward = new HttpClientForwardStrategy();
			ctx.writeAndFlush(forward.proxyForward(request)).addListener(ChannelFutureListener.CLOSE);
			
		} else if ("client_keep_alive".equalsIgnoreCase(event)) {
			/* 1、make client info to registry */
			
			/* 2、response info to client */
			FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.copiedBuffer("收到", CharsetUtil.UTF_8));
			response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=UTF-8; charset=UTF-8");
			response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
			response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS, "*");
			response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS,"GET, POST, PUT,DELETE");
			response.headers().set(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
			response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());
			boolean keepAlive = HttpUtil.isKeepAlive(request);
			if (!keepAlive) {
				logger.info("!keepAlive");
				ctx.write(response).addListener(ChannelFutureListener.CLOSE);
			} else {
				logger.info("keepAlive");
				response.headers().set(CONNECTION, KEEP_ALIVE);
				ctx.write(response);
			}
			ctx.flush();
			
		} else {
//			forwardServer(ctx, request);
			refreshProxyIps(ctx, request);
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
		System.out.println("--------forward to targer url()----------");
		
		URL url = null;
		try {
			url = new URL(request.uri());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		/* 1、create client */
		Bootstrap bootstrap = new Bootstrap();
		/*
		bootstrap.group(ctx.channel().eventLoop()).channel(ctx.channel().getClass())
				.handler(new TargetServerInitializer(ctx.channel()));
		*/
//		/*
		bootstrap.group(ctx.channel().eventLoop()).channel(ctx.channel().getClass())
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						// TODO Auto-generated method stub
						ch.pipeline().addLast(new HttpClientCodec());
						ch.pipeline().addLast(new HttpObjectAggregator(1048576 * 10));
						ch.pipeline().addLast(new TargetServerHandler(ctx.channel()));
					}
				});
//		*/		
		
		/*
		bootstrap.group(ctx.channel().eventLoop()).channel(ctx.channel().getClass())
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						// TODO Auto-generated method stub
						ch.pipeline().addLast(new HttpClientCodec());
						ch.pipeline().addLast(new HttpObjectAggregator(1048576 * 10));
//						ch.pipeline().addLast(new TargetServerHandler(ctx.channel()));
						ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
							Channel innerChannel = ctx.channel();
							@Override
							public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
								innerChannel.writeAndFlush(msg);
//								ctx.writeAndFlush((FullHttpResponse) msg).addListener(ChannelFutureListener.CLOSE);
							}
						});
					}
				});
		*/
		
		/* 2、connect target server */
		ChannelFuture cf = bootstrap.connect(url.getHost(), url.getPort() != -1 ? url.getPort() : url.getDefaultPort());
        cf.addListener(new ChannelFutureListener() {
			/* 3、握手完成后进行request请求 */
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    future.channel().writeAndFlush(request);
                } else {
                    ctx.channel().close();
                }
            }
        });
        
//		ctx.writeAndFlush("ok ok").addListener(ChannelFutureListener.CLOSE);
//		ctx.writeAndFlush(getHttpResponse("ok ok")).addListener(ChannelFutureListener.CLOSE);
	}

	private FullHttpResponse getHttpResponse(String content) {
		// TODO Auto-generated method stub
		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
				Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));
		response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
		return response;
	}

}
