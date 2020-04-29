package com.panamera.server.tcp;

import java.util.concurrent.TimeUnit;

import com.panamera.common.codec.ProxyMessageDecoder;
import com.panamera.common.codec.ProxyMessageEncoder;
import com.panamera.server.ProxyServer;
import com.panamera.server.tcp.handler.Server2ChannelInHandler;
import com.panamera.server.tcp.handler.ServerChannelInHandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

public class TcpProxyServer implements ProxyServer {
	private static final int MAX_FRAME_LENGTH = Integer.MAX_VALUE;
	private static final int LENGTH_FIELD_OFFSET = 0;
	private static final int LENGTH_FIELD_LENGTH = 4;
	private static final int LENGTH_ADJUSTMENT = 0;
	private static final int INITIAL_BYTES_TO_STRIP = 4;

	public static void main(String[] args) {
		new TcpProxyServer().start();
		System.out.println("------Server Start Over!!!");
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		/* 线程组 */
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		/**/
		ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				// TODO Auto-generated method stub
				ch.pipeline().addLast("idleStateHandler", new IdleStateHandler(0, 0, 1, TimeUnit.MINUTES));
				ch.pipeline().addLast(
						// 固定帧长解码器
						new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH,
								LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP),
						// 自定义协议解码器
						new ProxyMessageDecoder(),
						// 自定义协议编码器
						new ProxyMessageEncoder(),
						// 代理客户端连接代理服务器处理器
						new Server2ChannelInHandler());
//						new ServerChannelInHandler());
			}
		};

		/**/
		try {
			ChannelConnection connection = new ChannelConnection();
			connection.bootStart(bossGroup, workerGroup, channelInitializer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

}
