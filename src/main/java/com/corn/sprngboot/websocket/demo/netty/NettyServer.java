package com.corn.sprngboot.websocket.demo.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class NettyServer {
    private int port;

    public NettyServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {

        //boss用来接收进入的连接
        //默认的大小为CPU*2
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        //worker用来处理已经接收的连接，一旦‘boss’接收到连接，就会把连接信息注册到worker上
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                     .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new MyNettyWebSocketChannelHandler())//workerGroup的handler  //(4)
                    .option(ChannelOption.SO_BACKLOG, 128)//队列大小// (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);//一直保持活跃的连接 // (6)

            System.out.println("SimpleChatServer 启动了");

            // 绑定端口，开始接收进来的连接
            ChannelFuture f = b.bind(port).sync(); // (7)
            //future添加监听，操作完成回调
            f.addListener(new GenericFutureListener<Future<? super Void>>() {
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if(future.isSuccess()){
                        System.out.println("监听端口成功");
                    }else{
                        System.out.println("监听端口失败");
                    }
                }
            });

            // 等待服务器  socket 关闭 。
            // 在这个例子中，这不会发生，但你可以优雅地关闭你的服务器。
            f.channel().closeFuture().sync();

        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();

            System.out.println("SimpleChatServer 关闭了");
        }
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }
        new NettyServer(port).run();

    }
}
