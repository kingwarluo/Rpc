package com.kingwarluo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
 
public class InboundHandler2 extends ChannelInboundHandlerAdapter {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("channelActive2");
		// ִ֪ͨ����һ��InboundHandler
		ctx.fireChannelActive();
	}

	@Override
	// ��ȡClient���͵���Ϣ������ӡ����
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("InboundHandler2.channelRead: ctx :" + ctx);
		ByteBuf result = (ByteBuf) msg;
		byte[] result1 = new byte[result.readableBytes()];
		result.readBytes(result1);
		String resultStr = new String(result1);
		System.out.println("RPCClient said:" + resultStr);
		result.release();

		// ChannelInboundHandler֮��Ĵ��ݣ�ͨ������ ctx.fireChannelRead(msg) ʵ�֣�
		// ����ctx.write(msg) �����ݵ�ChannelOutboundHandler��
		// ctx.write()����ִ�к���Ҫ����flush()����������������ִ�С�
		ctx.write(msg);
	}
 
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		System.out.println("InboundHandler2.channelReadComplete");
		ctx.flush();
	}
 
}
