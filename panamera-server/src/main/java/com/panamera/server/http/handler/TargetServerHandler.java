package com.panamera.server.http.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;

public class TargetServerHandler extends ChannelInboundHandlerAdapter {
	private final Logger logger = LoggerFactory.getLogger(TargetServerHandler.class);
	
	/* channel(proxy server <---> target server) */
	private Channel channel;
	
	public TargetServerHandler(Channel channel) {
		this.channel = channel;
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// TODO Auto-generated method stub
		FullHttpResponse response = (FullHttpResponse) msg;
//		logger.info("客户端消息：" + msg.toString());
//		logger.info(response.content().toString(io.netty.util.CharsetUtil.UTF_8));
		/* 4、将target-server返回的msg写入proxy-server. */
		channel.writeAndFlush(response);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		super.channelReadComplete(ctx);
		logger.info("---------------target server read complate!---------------");
	}

}
