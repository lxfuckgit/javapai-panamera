package com.panamera.client.http;

import java.net.URI;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

public class HttpClientHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		FullHttpResponse response = (FullHttpResponse) msg;

		System.out.println("content:" + System.getProperty("line.separator") + response.content().toString(CharsetUtil.UTF_8));
		System.out.println("headers:" + System.getProperty("line.separator") + response.headers().toString());
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
//		URI url = new URI("http://127.0.0.1:1111");
		URI url = new URI("http://www.ip111.cn");
		String meg = "hello";
		
		ctx.channel();

		// 配置HttpRequest的请求数据和一些配置信息
		FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.GET, url.toASCIIString(),
				Unpooled.wrappedBuffer(meg.getBytes("UTF-8")));

		request.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8")
				// 开启长连接
				.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
				// 设置传递请求内容的长度
				.set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());

		// 发送数据
		ctx.writeAndFlush(request);
	}
}
