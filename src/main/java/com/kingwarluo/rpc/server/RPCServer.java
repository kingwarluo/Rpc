package com.kingwarluo.rpc.server;

import com.kingwarluo.rpc.common.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;

public class RPCServer {

    private String ip;
    private int port;
    private int ioThread;
    private int workerThreads;
    private MessageHandlers handlers = new MessageHandlers();
    private MessageRegistry registry = new MessageRegistry();
    {
        handlers.defaultHandler(new DefaultMessageHandler());
    }
    public RPCServer(String ip, int port, int ioThread, int workerThreads){
        this.ip = ip;
        this.port = port;
        this.ioThread = ioThread;
        this.workerThreads = workerThreads;
    }

    private ServerBootstrap bootstrap;
    private EventLoopGroup group;
    private MessageCollector collector;
    private Channel serverChannel;

    public RPCServer service(String type, Class<?> clazz, IMessageHandler message){
        handlers.put(type, message);
        registry.register(type, clazz);
        return this;
    }

    public void start(){
        try {
            bootstrap = new ServerBootstrap();
            group = new NioEventLoopGroup(ioThread);
            bootstrap.group(group);
            collector = new MessageCollector(handlers, registry, workerThreads);
            bootstrap.channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel channel) throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast(new ReadTimeoutHandler(60))
                            .addLast(new MessageDecoder())
                            // ChannelOutboundHandler 在注册的时候需要放在最后一个ChannelInboundHandler之前，否则将无法传递到ChannelOutboundHandler。
                            .addLast(new MessageEncoder())
                            .addLast(collector);
                }
            });
            bootstrap
                    // Nagle算法是将小的数据包组装为更大的帧然后进行发送，而不是输入一次发送一次,
                    // 因此在数据包不足的时候会等待其他数据的到了，组装成大的数据包进行发送，虽然该方式有效提高网络的有效
                    // 而该参数的作用就是禁止使用Nagle算法，使用于小数据即时传输
                    .option(ChannelOption.TCP_NODELAY, true)
                    // 用于可能长时间没有数据交流的连接
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 允许共用该端口
                    .option(ChannelOption.SO_REUSEADDR, true)
                    //服务端处理客户端连接请求是顺序处理的，所以同一时间只能处理一个客户端连接，多个客户端来的时候，
                    // 服务端将不能处理的客户端连接请求放在队列中等待处理，backlog参数指定了队列的大小
                    .option(ChannelOption.SO_BACKLOG, 100);
            ChannelFuture future = bootstrap.bind(new InetSocketAddress(ip, port));
            serverChannel = future.channel();
            future.sync();
            serverChannel.closeFuture().sync();//会阻塞
            System.out.println("ServerBootStrap started...");
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("ServerBootStrap start fail.....");
        }
    }

    public void stop(){
        serverChannel.close();
        group.shutdownGracefully();
        collector.closeGracefully();
    }

}
