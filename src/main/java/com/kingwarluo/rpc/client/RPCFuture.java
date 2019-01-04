package com.kingwarluo.rpc.client;

import java.util.concurrent.*;

public class RPCFuture<T> implements Future<T> {

    private T result;
    private Throwable error;
    private CountDownLatch countDown = new CountDownLatch(1);

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return result != null || error != null;
    }

    public void success(T result){
        this.result = result;
        countDown.countDown();
    }

    public void fail(Throwable error){
        this.error = error;
        countDown.countDown();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        countDown.await();
        if(error != null){
            throw new ExecutionException(error);
        }
        return result;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        countDown.await(timeout, unit);
        if(error != null){
            throw new ExecutionException(error);
        }
        return result;
    }

}
