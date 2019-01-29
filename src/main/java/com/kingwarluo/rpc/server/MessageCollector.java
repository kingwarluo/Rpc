package com.kingwarluo.rpc.server;

import com.kingwarluo.rpc.common.IMessageHandler;
import com.kingwarluo.rpc.common.MessageHandlers;
import com.kingwarluo.rpc.common.MessageInput;
import com.kingwarluo.rpc.common.MessageRegistry;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Sharable
public class MessageCollector extends ChannelInboundHandlerAdapter {

    private ThreadPoolExecutor executor;
    private MessageHandlers handlers;
    private MessageRegistry registry;

    public MessageCollector(MessageHandlers handlers, MessageRegistry registry, int workerThreads){

        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(1000);

        ThreadFactory factory = new ThreadFactory() {

            AtomicInteger seq = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setName("rpc-" + seq.getAndIncrement());
                return t;
            }
        };

        executor = new ThreadPoolExecutor(1, workerThreads, 30, TimeUnit.SECONDS,
                queue, factory, new ThreadPoolExecutor.CallerRunsPolicy());
        this.handlers = handlers;
        this.registry = registry;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 客户端来了一个新链接
        System.out.println("connection comes");
    }

    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 客户端走了一个
        System.out.println("connection leaves");
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof MessageInput){
            System.out.println("read a message");
            this.executor.execute(() -> {
                this.handleMessage(ctx, (MessageInput) msg);
            });
        }
    }

    private void handleMessage(ChannelHandlerContext ctx, MessageInput msg) {
        try {
            Class<?> clazz = registry.get(msg.getType());
            if(clazz == null){
                handlers.defaultHandler().handle(ctx, msg.getRequestId(), msg);
            }
            Object o = msg.getPayload(clazz);
            IMessageHandler<Object> handler = handlers.get(msg.getType());
            if (handler != null) {
                handler.handle(ctx, msg.getRequestId(), o);
            } else {
                handlers.defaultHandler().handle(ctx, msg.getRequestId(), msg);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    public void closeGracefully() {
        // 优雅一点关闭，先通知，再等待，最后强制关闭
        this.executor.shutdown();
        try {
            this.executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.executor.shutdownNow();
    }
}
