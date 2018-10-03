package org.superasync;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SuperFuture<V> implements Future<V>, Completable.Cancellable {

    private static final int WAITING = 0, SET = 1, EXCEPTIONAL = 2, CANCELLED = 3, TIMEOUT = 4;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);
    private final AtomicInteger state = new AtomicInteger(WAITING);
    private Object result;
    private final Callbacks<Callback<V>> callbacks = new CallbacksInner();
    private final Callback<V> callbackInterface = new CallbackInterface();
    private final org.superasync.Cancellable cancellationDelegate;

    SuperFuture(org.superasync.Cancellable cancellationDelegate) {
        this.cancellationDelegate = cancellationDelegate;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (state.compareAndSet(WAITING, CANCELLED)) {
            done();
            cancellationDelegate.cancel(mayInterruptIfRunning);
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

    private void set(V value) {
        if (state.compareAndSet(WAITING, SET)) {
            this.result = value;
            done();
        }
    }

    private void setException(Throwable e) {
        if (state.compareAndSet(WAITING, EXCEPTIONAL)) {
            this.result = e;
            done();
        }
    }

    Callback<V> asCallback() {
        return callbackInterface;
    }

    private void done() {
        countDownLatch.countDown();
        if (state.get() != CANCELLED) {
            notifyCallbacks();
        }
    }


    @Override
    public V get() throws InterruptedException, ExecutionException {
        countDownLatch.await();
        return report();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        countDownLatch.await(timeout, unit);
        if (state.compareAndSet(WAITING, TIMEOUT)) {
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


    public Observation<V> observe(ResultConsumer<V> resultConsumer) {
        return observe(resultConsumer, null);
    }

    public Observation<V> observe(ResultConsumer<V> resultConsumer, ErrorConsumer errorConsumer) {
        return observe(resultConsumer, errorConsumer, null);
    }

    public Observation<V> observe(ResultConsumer<V> resultConsumer, ErrorConsumer errorConsumer, Executor observingExecutor) {
        Observer<V> observer = new Observer<V>(
                observingExecutor != null ? observingExecutor : ExecutorProviderStaticRef.getExecutorProvider().defaultObserving(),
                resultConsumer,
                errorConsumer);
        addCallback(observer);
        return new Observation<V>(observer, this);
    }

    private void addCallback(Callback<V> callback) {
        callbacks.add(callback);
    }

    void removeCallback(Callback<V> callback) {
        callbacks.remove(callback);
    }

    private void notifyCallbacks() {
        callbacks.notifyCallbacks();
    }

    private void notifyCallback(Callback<V> callback) {
        switch (state.get()) {
            case SET:
                //noinspection unchecked
                callback.onResult((V) result);
                break;
            case EXCEPTIONAL:
                callback.onError((Throwable) result);
                break;
            case TIMEOUT:
                callback.onError(new TimeoutException());
                break;
        }
    }

    private class CallbacksInner extends Callbacks<Callback<V>> {
        @Override
        void notifyCallback(Callback<V> callback) {
            SuperFuture.this.notifyCallback(callback);
        }
    }

    private class CallbackInterface implements Callback<V> {
        @Override
        public void onResult(V result) {
            set(result);
        }

        @Override
        public void onError(Throwable e) {
            setException(e);
        }
    }
}
