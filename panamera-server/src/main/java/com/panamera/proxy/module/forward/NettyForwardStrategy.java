package com.panamera.proxy.module.forward;

import java.net.MalformedURLException;
import java.net.URL;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;

public final class NettyForwardStrategy implements ForwardStrategy {
	/**/
	private static final EventLoopGroup proxyGroup = new NioEventLoopGroup();

	/**/
	private Channel callbackChannel;

	public NettyForwardStrategy(Channel channel) {
		this.callbackChannel = channel;
	}

	@Override
	public FullHttpResponse normalForward(FullHttpRequest request) {
		// TODO Auto-generated method stub
		FullHttpResponse response = null;
		URL url = null;
		try {
			url = new URL(request.uri());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		doExcute(url.getHost(), url.getPort(), request);

		return response;
	}

	@Override
	public FullHttpResponse proxyForward(FullHttpRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	public void doExcute(String host, int port, Object msg) {
		/* 1、建立client. */
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(proxyGroup) // 注册线程池
				.channel(NioSocketChannel.class) // 使用NioSocketChannel来作为连接用的channel类
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						// TODO Auto-generated method stub
						ch.pipeline().addLast(new HttpClientCodec());
						ch.pipeline().addLast(new HttpObjectAggregator(6553600));
						ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
							@Override
							public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
								// TODO Auto-generated method stub
								callbackChannel.writeAndFlush(msg);
							}
						});
					}
				});

		/* 2、:建立connect. */
		ChannelFuture cf = bootstrap.connect(host, port);
		cf.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture future) throws Exception {
				if (future.isSuccess()) {
					/* 3、向目标发起请求. */
					future.channel().writeAndFlush(msg);
				} else {
					callbackChannel.close();
				}
			}
		});
	};

}
