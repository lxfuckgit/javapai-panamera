package com.panamera.server.tcp.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.ChannelGroupFutureListener;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.ImmediateEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.panamera.proxy.upstream.UpStreamHandler;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Server2ChannelInHandler extends SimpleChannelInboundHandler<byte[]> {

    private static final Logger log = LoggerFactory.getLogger(Server2ChannelInHandler.class);
    private static final EventLoopGroup proxyGroup = new NioEventLoopGroup();
    /**
     * 代理服务器和目标服务器之间的通道（从代理服务器出去所以是outbound过境）
     */
    private ChannelGroup allChannels = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);

//    @Autowired
//    private AppConfig appConfig;
    
//    @Autowired
//    private BackendServerRepository backendServerRepository;
    
    private volatile boolean frontendConnectStatus = false;

    /**
     * Closes the specified channel after all queued write requests are flushed.
     */
    public static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * 当客户端和代理服务器建立通道连接时，调用此方法
     *
     * @param ctx
     * @throws Exception
     */
    @Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		frontendConnectStatus = true;

		SocketAddress clientAddress = ctx.channel().remoteAddress();
		log.info("客户端地址：" + clientAddress);

		// 客户端和代理服务器的连接通道 入境的通道
		Channel inboundChannel = ctx.channel();
		createBootstrap(inboundChannel, "172.0.0.1", 10050);
	}

    /**
     * 在这里接收客户端的消息 在客户端和代理服务器建立连接时，也获得了代理服务器和目标服务器的通道outbound，
     * 通过outbound写入消息到目标服务器
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead0(final ChannelHandlerContext ctx, byte[] msg) throws Exception {
        log.info("客户端消息");
        allChannels.writeAndFlush(msg).addListener(new ChannelGroupFutureListener() {
            @Override
            public void operationComplete(ChannelGroupFuture future) throws Exception {
                //防止出现发送不成功造成的永久不读取消息的错误.
                ctx.channel().read();
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("代理服务器和客户端断开连接");
        frontendConnectStatus = false;
//        allChannels.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("发生异常：", cause);
        ctx.channel().close();
    }

    public void createBootstrap(final Channel inboundChannel, final String host, final int port) {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.option(ChannelOption.SO_KEEPALIVE,true);
            bootstrap.group(proxyGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new UpStreamHandler(inboundChannel));
//            bootstrap.handler(new BackendPipeline(inboundChannel, Server2ChannelInHandler.this, host, port));

            ChannelFuture f = bootstrap.connect(host, port);

            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        allChannels.add(future.channel());

                    } else {
                        log.debug("连接目标服务器失败");
//                        if (inboundChannel.isActive()) {
//                            log.info("Reconnect");
//                            final EventLoop loop = future.channel().eventLoop();
//                            loop.schedule(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Server2ChannelInHandler.this.createBootstrap(inboundChannel, host, port);
//                                }
//                            }, appConfig.getInterval(), TimeUnit.MILLISECONDS);
//                        } else {
//                            log.info("notActive");
//                        }
                    }
                    inboundChannel.read();
                }
            });

        } catch (Exception e) {
            log.error("连接后台服务失败", e);
        }
    }

    public boolean isConnect() {
        return frontendConnectStatus;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.ALL_IDLE) {
                log.debug("空闲时间到，关闭连接.");
                frontendConnectStatus = false;
               allChannels.close();
//                ctx.channel().close();
                closeOnFlush(ctx.channel());
            }
        }
    }

}