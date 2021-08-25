package com.kingwarluo.rpc;

import com.kingwarluo.rpc.client.RPCClient;
import com.kingwarluo.rpc.common.remote.ExpRequest;
import com.kingwarluo.rpc.common.remote.ExpResponse;

public class ClientDemo {

    private RPCClient client;

    public ClientDemo(RPCClient client){
        this.client = client;
        this.client.rpc("fib_res", Long.class).rpc("exp_res", ExpResponse.class);
    }

    public static void main(String[] args) {
        RPCClient client = new RPCClient("localhost", 9999);
        ClientDemo demo = new ClientDemo(client);
        for (int i = 0; i < 30; i++) {
            System.out.printf("fib(%d) = %d\n", i, demo.fib(i));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                i--;
            }
        }
        for (int i = 0; i < 30; i++) {
            try {
                ExpResponse res = demo.exp(2, i);
                System.out.printf("exp(%d) = %d cost=%dns\n", i, res.getValue(), res.getCostInNanos());
                Thread.sleep(100);
            } catch (InterruptedException e) {
                i--;
            }
        }
        client.close();
    }

    private ExpResponse exp(int base, int exp) {
        return client.send("exp", new ExpRequest(base, exp));
    }

    private long fib(int i) {
        return (Long) client.send("fib", i);
    }

}
