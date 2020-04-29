package com.panamera.server.tcp.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.panamera.common.codec.ProxyMessageDecoder;
import com.panamera.common.codec.ProxyMessageEncoder;
import com.panamera.common.config.ServerConfig;
import com.panamera.common.protocol.ProxyMessage;
import com.panamera.proxy.upstream.UpStreamHandler;
import com.panamera.server.tcp.ChannelConnection;

/**
 * 处理服务器接收到的客户端连接.<br>
 * 
 * @author lx
 *
 */
public class ServerChannelInHandler extends ChannelInboundHandlerAdapter {
    private static ArrayList<String> clients = new ArrayList<>();

    //统一管理客户端channel和remote channel
    private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    //所有remote channel共享的线程池，减少线程创建
    private static final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup();

    private ChannelConnection remoteHelper = new ChannelConnection();

    //客户端标识clientKey
    private String clientKey;
    //代理客户端的ChannelHandlerContext
    private ChannelHandlerContext ctx;
    //判断代理客户端是否已注册授权
    private boolean isRegister = false;

    public ChannelHandlerContext getCtx() {
        return ctx;
    }
    
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		this.ctx = ctx;
		System.out.println("有客户端建立连接，客户端地址为：" + ctx.channel().remoteAddress());
		
		/* 随机挑一台去连接 */
		List<Map<String, Object>> list = (ArrayList<Map<String, Object>>) ServerConfig.getConfig("server-upstream");
		list.forEach(server -> {
			String host = String.valueOf(server.get("proxy-host"));
			int port = Integer.valueOf(server.get("proxy-port").toString());

			/* 创建-目标服务 */
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(workerGroup).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							// TODO Auto-generated method stub
							ch.pipeline().addLast("idleStateHandler", new IdleStateHandler(0, 0, 2, TimeUnit.MINUTES));
							ch.pipeline().addLast(new ProxyMessageDecoder());
							ch.pipeline().addLast(new ProxyMessageEncoder());
							ch.pipeline().addLast(new UpStreamHandler(ctx.channel()));
						}
					});
			
			/* 连接-目标服务 */
			ChannelFuture cf = bootstrap.connect(host, port);
			cf.addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					// TODO Auto-generated method stub
					if (future.isSuccess()) {
						channels.add(future.channel());
					}
					ctx.channel().read();
				}
			});
		});

		/*
		ServerChannelInHandler serverHandler = this;
		list.forEach(action ->{
			String host = String.valueOf(action.get("proxy-host"));
			int port = Integer.valueOf(action.get("proxy-port").toString());
			
			ChannelInitializer channelInitializer = new ChannelInitializer() {
				@Override
				protected void initChannel(Channel channel) throws Exception {
					channel.pipeline().addLast(new ByteArrayDecoder(), new ByteArrayEncoder(),
							new RemoteHandler(serverHandler, port));
					// 向channelGroup注册remote channel
					channels.add(channel);
				}
			};
			
			remoteHelper.bootStart(bossGroup, workerGroup, host, port, channelInitializer);
		});
		*/
		
	}

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ProxyMessage message = (ProxyMessage) msg;
        if (message.getType() == ProxyMessage.TYPE_REGISTER){
            //处理客户端注册请求
            processRegister(message);
        }else if (isRegister){
            switch (message.getType()){
                case ProxyMessage.TYPE_DISCONNECTED :
                    processDisconnect(message);
                    break;
                case ProxyMessage.TYPE_KEEPALIVE :
                    //心跳，不做处理
                    break;
                case ProxyMessage.TYPE_DATA :
                    processData(message);
                    break;
            }
        }else {
            System.out.println("有未授权的客户端尝试发送消息，断开连接");
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        channels.remove(ctx.channel());
        System.out.println("客户端连接中断："+ctx.channel().remoteAddress());
    }

    /**
     * @Description 判断客户端是否有授权
     * @Date 18:04 2020/2/22
     * @Param [clientKey]
     * @return boolean
     **/
	public synchronized boolean isLegal(String clientKey) {
		for (String item : (ArrayList<String>) ServerConfig.getConfig("clients-auth")) {
			if (item.equals(clientKey)) {
				// 一个client-key只允许一个代理客户端使用
				for (String client : clients) {
					if (client.equals(clientKey)) {
						return false;
					}
				}
				clients.add(clientKey);
				this.clientKey = clientKey;
				return true;
			}
		}
		return false;
	}

    /**
     * @Description 处理客户端注册请求
     * @Date 16:00 2020/2/20
     * @Param [message]
     * @return void
     **/
    public void processRegister(ProxyMessage message) throws Exception{
        HashMap<String,Object> metaData = new HashMap<>();

        ServerChannelInHandler serverHandler = this;

        String clientKey = message.getMetaData().get("clientKey").toString();
        //客户端合法性判断
        if (isLegal(clientKey)){
            String host = (String) ServerConfig.getConfig("server-host");
            //指定服务器需要开启的对外访问端口
            ArrayList<Integer> ports = (ArrayList<Integer>) message.getMetaData().get("ports");
            try {
                for (int port : ports){
                    ChannelInitializer channelInitializer = new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline().addLast(
                                    new ByteArrayDecoder(),
                                    new ByteArrayEncoder()
//                                    ,new RemoteHandler(serverHandler,port)
                            );
                            //向channelGroup注册remote channel
                            channels.add(channel);
                        }
                    };
                    host = "47.103.82.134";
					remoteHelper.bootStart(bossGroup, workerGroup, host, port, channelInitializer);
                }
                metaData.put("isSuccess",true);
                isRegister = true;
                System.out.println("客户端注册成功，clientKey为："+clientKey);
            }catch (Exception e){
                metaData.put("isSuccess",false);
                metaData.put("reason",e.getMessage());
                System.out.println("启动器出错，客户端注册失败，clientKey为："+clientKey);
            }
        }else {
            metaData.put("isSuccess",false);
            metaData.put("reason","client-key is wrong");
            System.out.println("客户端注册失败，使用了不合法的clientKey，clientKey为："+clientKey);
        }

        ProxyMessage res = new ProxyMessage();
        res.setType(ProxyMessage.TYPE_AUTH);
        res.setMetaData(metaData);
        ctx.writeAndFlush(res);
    }

    /**
     * @Description 处理客户端断开请求
     * @Date 16:25 2020/2/20
     * @Param [message]
     * @return void
     **/
    public void processDisconnect(ProxyMessage message){
        channels.close(new ChannelMatcher() {
            @Override
            public boolean matches(Channel channel) {
                return channel.id().asLongText().equals(message.getMetaData().get("channelId"));
            }
        });
        System.out.println("有客户端请求断开，clientKey为："+clientKey);
    }

    /**
     * @Description 处理客户端发送的数据
     * @Date 16:25 2020/2/20
     * @Param [message]
     * @return void
     **/
    public void processData(ProxyMessage message){
        if (message.getData()==null || message.getData().length<=0){
            return;
        }
        //根据channelId转发到channelGroup上注册的相应remote channel(外部请求)
        channels.writeAndFlush(message.getData(),new ChannelMatcher() {
            @Override
            public boolean matches(Channel channel) {
                return channel.id().asLongText().equals(message.getMetaData().get("channelId"));
            }
        });
        System.out.println("收到客户端返回数据，数据量为"+message.getData().length+"字节");
    }

}
