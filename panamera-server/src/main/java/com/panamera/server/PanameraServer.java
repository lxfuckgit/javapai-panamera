package com.panamera.server;

import com.panamera.common.codec.ProxyMessageDecoder;
import com.panamera.common.codec.ProxyMessageEncoder;
import com.panamera.server.tcp.handler.ServerChannelInHandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public final class PanameraServer {
	public static void main(String[] args) {
		if (args != null && "forward".equals(args[0])) {

		} else if (args != null && "reverse".equals(args[0])) {

		} else {

		}

	}

}

class TcpProxyChannelInitializer extends ChannelInitializer<SocketChannel> {
	private static final int MAX_FRAME_LENGTH = Integer.MAX_VALUE;
	private static final int LENGTH_FIELD_OFFSET = 0;
	private static final int LENGTH_FIELD_LENGTH = 4;
	private static final int LENGTH_ADJUSTMENT = 0;
	private static final int INITIAL_BYTES_TO_STRIP = 4;

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		// TODO Auto-generated method stub
		ch.pipeline().addLast(
				// 固定帧长解码器
				new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH,
						LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP),
				// 自定义协议解码器
				new ProxyMessageDecoder(),
				// 自定义协议编码器
				new ProxyMessageEncoder(),
				// 代理客户端连接代理服务器处理器
				new ServerChannelInHandler());
	}
}

class HttpProxyChannelInitializer extends ChannelInitializer<SocketChannel> {
	private static final int MAX_FRAME_LENGTH = Integer.MAX_VALUE;
	private static final int LENGTH_FIELD_OFFSET = 0;
	private static final int LENGTH_FIELD_LENGTH = 4;
	private static final int LENGTH_ADJUSTMENT = 0;
	private static final int INITIAL_BYTES_TO_STRIP = 4;

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		// TODO Auto-generated method stub
		ch.pipeline().addLast(
				// 固定帧长解码器
				new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH,
						LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP),
				// 自定义协议解码器
				new ProxyMessageDecoder(),
				// 自定义协议编码器
				new ProxyMessageEncoder(),
				// 代理客户端连接代理服务器处理器
				new ServerChannelInHandler());
	}
}