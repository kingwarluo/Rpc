package com.kingwarluo.rpc.common;

import com.alibaba.fastjson.JSON;
import com.kingwarluo.rpc.common.utils.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class MessageEncoder extends MessageToMessageEncoder<MessageOutput> {

    @Override
    protected void encode(ChannelHandlerContext ctx, MessageOutput message, List<Object> list) throws Exception {
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.directBuffer();
        writeStr(buf, message.getRequestId());
        writeStr(buf, message.getType());
        writeStr(buf, JSON.toJSONString(message.getPayload()));
        list.add(buf);
    }

    public void writeStr(ByteBuf buf, String msg) throws UnsupportedEncodingException {
        buf.writeInt(msg.length());
        buf.writeBytes(msg.getBytes(Charsets.UTF8));
    }
}
