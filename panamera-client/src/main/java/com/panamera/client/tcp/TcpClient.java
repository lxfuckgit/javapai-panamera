package com.panamera.client.tcp;

import com.panamera.common.codec.ProxyMessageDecoder;
import com.panamera.common.codec.ProxyMessageEncoder;
import com.panamera.common.config.ServerConfig;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class TcpClient {
	private static final int MAX_FRAME_LENGTH = Integer.MAX_VALUE;
	private static final int LENGTH_FIELD_OFFSET = 0;
	private static final int LENGTH_FIELD_LENGTH = 4;
	private static final int LENGTH_ADJUSTMENT = 0;
	private static final int INITIAL_BYTES_TO_STRIP = 4;

	private static final EventLoopGroup workerGroup = new NioEventLoopGroup();

	public static void main(String[] args) {
		new TcpClient().start();
		System.out.println("------------->Client Start Over!!!");
	}

	public void start() {
		String serverHost = (String) ServerConfig.getConfig("server-host");
		int serverPort = (Integer) ServerConfig.getConfig("server-port");

		ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel channel) throws Exception {
				channel.pipeline().addLast(
						new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH,
								LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP),
						new ProxyMessageDecoder(), new ProxyMessageEncoder(), new ClientHandler());
			}
		};

		ClientBootStrapHelper clientBootStrapHelper = new ClientBootStrapHelper();
		clientBootStrapHelper.start(workerGroup, channelInitializer, serverHost, serverPort);
	}

}
