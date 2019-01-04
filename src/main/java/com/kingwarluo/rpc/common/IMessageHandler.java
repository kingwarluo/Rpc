package com.kingwarluo.rpc.common;

import io.netty.channel.ChannelHandlerContext;

public interface IMessageHandler<T> {

    public void handle(ChannelHandlerContext ctx, String requestId, T message);

}
