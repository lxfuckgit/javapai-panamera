package com.panamera.proxy.protocol.http;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.proxy.HttpProxyHandler;

public class xxx {
	
	public String nettyProxy(String proxyHost, int proxyPort) {
		final String ua = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";

		Bootstrap b = new Bootstrap();
		b.group(new NioEventLoopGroup()).channel(NioSocketChannel.class)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline p = ch.pipeline();
						p.addLast(new HttpClientCodec());
						p.addLast(new HttpContentDecompressor());
						p.addLast(new HttpObjectAggregator(10_485_760));
						p.addLast(new ChannelInboundHandlerAdapter() {
							@Override
							public void channelActive(final ChannelHandlerContext ctx) throws Exception {
								HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
										"/");
								request.headers().set("HOST", proxyHost + ":" + proxyPort);
								request.headers().set("USER_AGENT", ua);
//								request.headers().set("CONNECTION", CLOSE);
								ctx.writeAndFlush(request);
								System.out.println("!sent");
							}

							@Override
							public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
								System.out.println("!answer");
								if (msg instanceof FullHttpResponse) {
									FullHttpResponse httpResp = (FullHttpResponse) msg;
									ByteBuf content = httpResp.content();
									String strContent = content.toString(Charset.forName("UTF_8"));
									System.out.println("body: " + strContent);
									return;
								}

								super.channelRead(ctx, msg);

							}

							@Override
							public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
								cause.printStackTrace(System.err);
								ctx.close();
							}

						});
//						p.addLast(new Socks4ProxyHandler(new InetSocketAddress("149.202.68.167", 37678)));
						p.addLast(new HttpProxyHandler(new InetSocketAddress(proxyHost, proxyPort)));
					}

				});

		b.connect(proxyHost, proxyPort).awaitUninterruptibly();

		System.out.println("!connected");

//		finish.await(1, MINUTES);

		return null;
	};

}
