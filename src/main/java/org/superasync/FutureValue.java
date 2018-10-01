package org.superasync;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FutureValue<V> implements Future<V> {

    private static final int WAITING = 0, SET = 1, EXCEPTIONAL = 2, CANCELLED = 3;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private final AtomicInteger state = new AtomicInteger(WAITING);
    private Object result;

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (state.compareAndSet(WAITING, CANCELLED)) {
            stopWaiting();
            return true;
        }
        return false;
    }

    @Override
    public boolean isCancelled() {
        return state.get() == CANCELLED;
    }

    @Override
    public boolean isDone() {
        return state.get() != WAITING;
    }

    public void set(V value) {
        if (state.compareAndSet(WAITING, SET)) {
            this.result = value;
            stopWaiting();
        }
    }

    public void setException(Throwable e) {
        if (state.compareAndSet(WAITING, EXCEPTIONAL)) {
            this.result = e;
            stopWaiting();
        }
    }

    private void stopWaiting() {
        countDownLatch.countDown();
        done();
    }

    protected void done() {
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        countDownLatch.await();
        return report();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        countDownLatch.await(timeout, unit);
        if (state.get() == WAITING) {
            throw new TimeoutException();
        }
        return report();
    }

    private V report() throws ExecutionException {
        switch (state.get()) {
            case SET:
                //noinspection unchecked
                return (V) result;
            case EXCEPTIONAL:
                throw new ExecutionException((Throwable) result);
            case CANCELLED:
                throw new CancellationException();
        }
        throw new IllegalStateException();
    }
}
