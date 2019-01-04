package com.kingwarluo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
 
public class HelloClientIntHandler extends ChannelInboundHandlerAdapter {

	@Override
	// ��ȡ����˵���Ϣ
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("HelloClientIntHandler.channelRead");
		ByteBuf result = (ByteBuf) msg;
		byte[] result1 = new byte[result.readableBytes()];
		result.readBytes(result1);
		result.release();
		ctx.close();
		System.out.println("RPCServer said:" + new String(result1));
	}

	@Override
	// �����ӽ�����ʱ�������˷�����Ϣ ��channelActive �¼������ӽ�����ʱ��ᴥ��
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("HelloClientIntHandler.channelActive");
		String msg = "Are you ok?";
		ByteBuf encoded = ctx.alloc().buffer(4 * msg.length());
		encoded.writeBytes(msg.getBytes());
		ctx.write(encoded);
		ctx.flush();
	}
}
