package com.kingwarluo.rpc;

import com.kingwarluo.rpc.common.IMessageHandler;
import com.kingwarluo.rpc.common.MessageOutput;
import com.kingwarluo.rpc.common.remote.ExpRequest;
import com.kingwarluo.rpc.common.remote.ExpResponse;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;

class FibMessageHandler implements IMessageHandler<Integer> {

    private List<Long> fibs = new ArrayList<>();

    {
        fibs.add(1L); // fib(0) = 1
        fibs.add(1L); // fib(1) = 1
    }

    @Override
    public void handle(ChannelHandlerContext ctx, String requestId, Integer n) {
        for (int i = fibs.size(); i < n + 1; i++) {
            long value = fibs.get(i - 2) + fibs.get(i - 1);
            fibs.add(value);
        }
        ctx.writeAndFlush(new MessageOutput(requestId, "fib_res", fibs.get(n)));
    }
}

class ExpRequestHandler implements IMessageHandler<ExpRequest> {

    @Override
    public void handle(ChannelHandlerContext ctx, String requestId, ExpRequest message) {
        int base = message.getBase();
        int exp = message.getExp();
        long start = System.nanoTime();
        long res = 1;
        for (int i = 0; i < exp; i++) {
            res *= base;
        }
        long cost = System.nanoTime() - start;
        ctx.writeAndFlush(new MessageOutput(requestId, "exp_res", new ExpResponse(res, cost)));
    }

}

public class ServerDemo {

    public static void main(String[] args) {
        RPCServer server = new RPCServer("localhost", 9999, 2, 16);
        server.service("fib", Integer.class, new FibMessageHandler()).service("exp", ExpRequest.class, new ExpRequestHandler());
        server.start();
    }

}
