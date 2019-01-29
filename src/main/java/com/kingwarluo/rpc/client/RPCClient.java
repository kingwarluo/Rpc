package com.kingwarluo.rpc.client;

import com.kingwarluo.rpc.common.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class RPCClient {

    private String ip;
    private int port;
    private boolean started;
    private boolean stopped;

    private MessageRegistry registry = new MessageRegistry();

    public RPCClient(String ip, int port){
        this.ip = ip;
        this.port = port;
        init();
    }

    private Bootstrap bootstrap;
    private EventLoopGroup group;
    private MessageCollector collector;

    public RPCClient rpc(String type, Class<?> clazz){
        registry.register(type, clazz);
        return this;
    }

    public <T> T send(String type, Object payload){
        RPCFuture<T> future = sendAsync(type, payload);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    private <T> RPCFuture<T> sendAsync(String type, Object payload) {
        if(!started){
            connect();
            started = true;
        }
        MessageOutput output = new MessageOutput(RequestId.next(), type, payload);
        return collector.send(output);
    }

    public void init(){
        bootstrap = new Bootstrap();
        group = new NioEventLoopGroup(1);
        bootstrap.group(group);
        collector = new MessageCollector(registry, this);
        bootstrap.channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel channel) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast(new ReadTimeoutHandler(60))
                        .addLast(new MessageDecoder())
                        .addLast(new MessageEncoder())
                        .addLast(collector);
            }
        });
        bootstrap.option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture future = connect();
        future.addListener(new GenericFutureListener() {
            @Override
            public void operationComplete(Future future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("服务器连接已经完成，可以确保消息准确传输。");
                }
            }
        });
    }

    public ChannelFuture connect() {
        return bootstrap.connect(ip, port).syncUninterruptibly();
    }

    public void reconnect() {
        if (stopped) {
            return;
        }
        bootstrap.connect(ip, port).addListener(future -> {
            if (future.isSuccess()) {
                return;
            }
            if(!stopped){
                group.schedule(() -> {
                    reconnect();
                }, 1, TimeUnit.SECONDS);
            }
        });
    }

    public void close(){
        stopped = true;
        collector.close();
        group.shutdownGracefully();
    }
}
