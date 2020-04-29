package com.panamera.server.http;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.panamera.common.config.ServerConfig;
import com.panamera.proxy.ippool.DefaultIpPool;
import com.panamera.server.ProxyServer;
import com.panamera.server.http.handler.HttpForwardHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

public class HttpProxyServer implements ProxyServer {
	private static final Logger logger = LoggerFactory.getLogger(HttpProxyServer.class);
	
	EventLoopGroup bossGroup = new NioEventLoopGroup(1);
	EventLoopGroup workerGroup = new NioEventLoopGroup();
	
	public static void main(String[] args) {
		new HttpProxyServer().start();
		
		final HashedWheelTimer timer = new HashedWheelTimer();
		timer.newTimeout(new TimerTask() {
			@Override
			public void run(Timeout timeout) throws Exception {
				// TODO Auto-generated method stub
				DefaultIpPool.getInstance().refreshIpPool();
				timer.newTimeout(this, 1, TimeUnit.MINUTES);// 结束时候再次注册
			}
		}, 1, TimeUnit.MINUTES);
		
		final HashedWheelTimer check = new HashedWheelTimer();
		check.newTimeout(new TimerTask() {
			@Override
			public void run(Timeout timeout) throws Exception {
				// TODO Auto-generated method stub
				DefaultIpPool.getInstance().healthCheck();
				timer.newTimeout(this, 3, TimeUnit.MINUTES);// 结束时候再次注册
			}
		}, 1, TimeUnit.MINUTES);
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		ServerBootstrap server = new ServerBootstrap();
		server.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
						ch.pipeline().addLast("http-decoder", new HttpRequestDecoder());
						logger.debug("------Server服务器正在初始化请求解码器......");
						logger.debug("------Http Request Decoder 解码器初始化完毕，欢迎客户端发起请求访问......");
						
//						ch.pipeline().addLast("http-decoder", new io.netty.handler.codec.http.HttpServerCodec());
						ch.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65536 * 100));
						ch.pipeline().addLast("http-compressor", new HttpContentCompressor());
						
						ch.pipeline().addLast("http-encoder", new HttpResponseEncoder());
						logger.debug("------Server服务器正在初始化响应解码器......");
						logger.debug("------Http Response Encoder 解码器初始化完毕，随时等待响应客户端请求......");
						
						ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
						ch.pipeline().addLast("http-forward-handler", new HttpForwardHandler());
						ch.pipeline().addLast("idle-state-handler", new IdleStateHandler(3, 0, 0, TimeUnit.MINUTES));
//						ch.pipeline().addLast("life-cycle-handler", new com.panamera.server.http.handler.LifeCycleInBoundHandler());
					}
				}).option(ChannelOption.SO_BACKLOG, 128).option(ChannelOption.SO_KEEPALIVE, true);
		
		String host = String.valueOf(ServerConfig.getConfig("server-host").toString());
		Integer port = Integer.valueOf(ServerConfig.getConfig("server-port").toString());
		try {
			// 启动代理监听.
			Channel channel = server.bind(host, port).sync().channel();
			logger.info("------HTTP代理服务启动完毕,映射端口:{}", port);
			logger.info("------please visit：http://{}:{}", host, port);
			
			// 监听关闭事件.
//			channel.closeFuture().sync();
			channel.closeFuture().addListener((ChannelFutureListener) future -> {
				channel.deregister();
				logger.warn("------channel从workerGroup取消注册!!!");
				channel.close();
				logger.warn("------channel通道已关闭!!!");
			});
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			stop();
			e.printStackTrace();
		}finally {
			
		}
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		// 释放线程资源.
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
		logger.warn("-----------Server Accepter Close!---------------");
	}

}
