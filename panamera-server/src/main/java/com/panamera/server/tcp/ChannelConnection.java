package com.panamera.server.tcp;

import com.panamera.common.config.ServerConfig;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public final class ChannelConnection {
	/**
	 * 启动Server.<br>
	 * 
	 * @param bossGroup
	 * @param workerGroup
	 * @param channelInitializer
	 * @throws Exception
	 * 
	 * @return void
	 */
	public synchronized void bootStart(EventLoopGroup bossGroup, EventLoopGroup workerGroup,
			ChannelInitializer<SocketChannel> channelInitializer) throws Exception {
		String serverHost = String.valueOf(ServerConfig.getConfig("server-host"));
		Integer serverPort = Integer.valueOf(ServerConfig.getConfig("server-port").toString());
		bootStart(bossGroup, workerGroup, serverHost, serverPort, channelInitializer);
	}

	/**
	 * 启动监听.<br>
	 * 
	 * @param bossGroup
	 * @param workerGroup
	 * @param serverHost
	 * @param serverPort
	 * @param channelInitializer
	 * @throws Exception
	 * 
	 * @return void
	 */
	public synchronized void bootStart(EventLoopGroup bossGroup, EventLoopGroup workerGroup, String serverHost,
			int serverPort, ChannelInitializer<SocketChannel> channelInitializer) {

		try {
			ServerBootstrap ss = new ServerBootstrap();
			ss.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(channelInitializer)
					.childOption(ChannelOption.SO_KEEPALIVE, true);
			// 绑定并监听.
			Channel channel = ss.bind(serverHost, serverPort).sync().channel();
			System.out.println("------Server服务已完成绑定:" + serverHost + ":" + serverPort);

			// 监听关闭事件.
			channel.closeFuture().addListener((ChannelFutureListener) future -> {
				channel.deregister();
				System.out.println("------channel从workerGroup取消注册!!!");
				channel.close();
				System.out.println("------channel关闭!!!");
			});
		} catch (Exception e) {
			// 释放线程资源.
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();

			e.printStackTrace();
		}
	}
}
