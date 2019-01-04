package com.kingwarluo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
 
public class OutboundHandler2 extends ChannelOutboundHandlerAdapter {

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		System.out.println("OutboundHandler2.write");
		// ִ����һ��OutboundHandler
		super.write(ctx, msg, promise);
	}
}
