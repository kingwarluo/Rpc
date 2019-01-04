package com.kingwarluo.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.ReplayingDecoder;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class MessageDecoder extends ReplayingDecoder<MessageInput> {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> list) throws Exception {
        String requestId = readStr(buf);
        String type = readStr(buf);
        String payload = readStr(buf);
        list.add(new MessageInput(requestId, type, payload));
    }

    public String readStr(ByteBuf buf) throws UnsupportedEncodingException {
        // 字符串先长度后字节数组，统一UTF8编码
        int len = buf.readInt();
        if(len < 0 || len > (1 << 20)){
            throw new DecoderException("String too long len=" + len);
        }
        byte bytes[] = new byte[len];
        buf.readBytes(bytes);
        return new String(bytes, Charsets.UTF8);
    }

}
