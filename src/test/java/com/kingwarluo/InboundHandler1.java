package com.kingwarluo;
 
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

 
public class InboundHandler1 extends ChannelInboundHandlerAdapter {

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("channelActive1");
		// ִ֪ͨ����һ��InboundHandler
		ctx.fireChannelActive();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("InboundHandler1.channelRead: ctx :" + ctx);
		// ִ֪ͨ����һ��InboundHandler
		ctx.fireChannelRead(msg);
	}
 
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		System.out.println("InboundHandler1.channelReadComplete");
		ctx.flush();
	}
}
