package com.kingwarluo.rpc.client;

import com.kingwarluo.rpc.common.MessageInput;
import com.kingwarluo.rpc.common.MessageOutput;
import com.kingwarluo.rpc.common.MessageRegistry;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Sharable
public class MessageCollector extends ChannelInboundHandlerAdapter {

    private MessageRegistry registry;
    private RPCClient client;
    private ChannelHandlerContext context;
    private ConcurrentHashMap<String, RPCFuture<?>> pendingTasks = new ConcurrentHashMap<>();

    private Throwable connectionClosed = new Exception("rpc connection could not active error");

    public MessageCollector(MessageRegistry registry, RPCClient client){
        this.registry = registry;
        this.client = client;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx){
        System.out.println("client collector active");
        this.context = ctx;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx){
        this.context = null;
        pendingTasks.forEach((__, future) -> {
            future.fail(connectionClosed);
        });
        pendingTasks.clear();
        ctx.channel().eventLoop().schedule(() -> {
            client.reconnect();
        }, 1, TimeUnit.SECONDS);
    }

    public <T> RPCFuture<T> send(MessageOutput output){
        ChannelHandlerContext ctx = context;
        RPCFuture<T> future = new RPCFuture<T>();
        if(ctx != null){
            ctx.channel().eventLoop().execute(() -> {
                pendingTasks.put(output.getRequestId(), future);
                ctx.writeAndFlush(output);
            });
        }else{
            future.fail(connectionClosed);
        }
        return future;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        if(!(msg instanceof MessageInput)){
            return;
        }
        MessageInput input = (MessageInput) msg;
        Class<?> clazz = registry.get(input.getType());
        if(clazz == null){
            return;
        }
        Object o = input.getPayload(clazz);
        RPCFuture<Object> future = (RPCFuture<Object>) pendingTasks.remove(input.getRequestId());
        if (future == null) {
            return;
        }
        future.success(o);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(cause);
    }

    public void close(){
        ChannelHandlerContext ctx = context;
        if (ctx != null) {
            ctx.close();
        }
    }

}
