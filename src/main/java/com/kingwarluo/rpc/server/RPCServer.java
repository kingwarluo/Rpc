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
                            // ChannelOutboundHandler ��ע���ʱ����Ҫ�������һ��ChannelInboundHandler֮ǰ�������޷����ݵ�ChannelOutboundHandler��
                            .addLast(new MessageEncoder())
                            .addLast(collector);
                }
            });
            bootstrap
                    // Nagle�㷨�ǽ�С�����ݰ���װΪ�����֡Ȼ����з��ͣ�����������һ�η���һ��,
                    // ��������ݰ������ʱ���ȴ��������ݵĵ��ˣ���װ�ɴ�����ݰ����з��ͣ���Ȼ�÷�ʽ��Ч����������Ч
                    // ���ò��������þ��ǽ�ֹʹ��Nagle�㷨��ʹ����С���ݼ�ʱ����
                    .option(ChannelOption.TCP_NODELAY, true)
                    // ���ڿ��ܳ�ʱ��û�����ݽ���������
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // �����øö˿�
                    .option(ChannelOption.SO_REUSEADDR, true)
                    //����˴���ͻ�������������˳����ģ�����ͬһʱ��ֻ�ܴ���һ���ͻ������ӣ�����ͻ�������ʱ��
                    // ����˽����ܴ���Ŀͻ�������������ڶ����еȴ�����backlog����ָ���˶��еĴ�С
                    .option(ChannelOption.SO_BACKLOG, 100);
            ChannelFuture future = bootstrap.bind(new InetSocketAddress(ip, port));
            serverChannel = future.channel();
            future.sync();
            serverChannel.closeFuture().sync();//������
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
