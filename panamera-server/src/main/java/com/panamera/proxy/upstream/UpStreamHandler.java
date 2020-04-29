package com.panamera.proxy.upstream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 负载均衡 [算法实现]及 [转发策略].
 * 
 * @author lx
 *
 */
public class UpStreamHandler extends SimpleChannelInboundHandler<byte[]> {
	private static final Logger logger = LoggerFactory.getLogger(UpStreamHandler.class);

	private Channel lbsChannel;

	public UpStreamHandler(Channel channel) {
		// TODO Auto-generated constructor stub
		this.lbsChannel = channel;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		logger.info("目标服务器地址：" + ctx.channel().remoteAddress());
		super.channelActive(ctx);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, byte[] msg) throws Exception {
		// TODO Auto-generated method stub
		System.out.println(new String(msg));
	}

	/**
	 * 读取负载实例。<br>
	 */
	public void getConfig() {
	}

	/**
	 * (配置文件)读取静态的负载实例。<br>
	 */
	private void getStaticConfig() {
	}

	/**
	 * (注册中心)读取动态的负载实例。<br>
	 */
	private void getDynamicConfig() {
	}

}
