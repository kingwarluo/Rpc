package com.kingwarluo.rpc.common;

import io.netty.channel.ChannelHandlerContext;

public class DefaultMessageHandler implements IMessageHandler<MessageInput> {

    @Override
    public void handle(ChannelHandlerContext ctx, String requestId, MessageInput message) {
        System.out.println("默认消息处理器:" + message.getRequestId());
        ctx.close();
    }
}
