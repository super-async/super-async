package org.superasync;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class AsyncObserver<V> implements Observer<V>, CancellableTask {

    private final Executor executor;
    private final ResultConsumer<V> resultConsumer;
    private final ErrorConsumer errorConsumer;
    private final OnCancelListener onCancelListener;
    final FutureInner future = new FutureInner();

    AsyncObserver(Executor executor, ResultConsumer<V> resultConsumer,
                  ErrorConsumer errorConsumer, OnCancelListener onCancelListener) {
        this.executor = executor;
        this.resultConsumer = resultConsumer;
        this.errorConsumer = errorConsumer;
        this.onCancelListener = onCancelListener;
    }

    @Override
    public void onResult(final V result) {
        future.set(result);
    }

    @Override
    public void onError(final Throwable e) {
        future.setException(e);
    }

    @Override
    public boolean isObserving() {
        return !future.isDone();
    }

    @Override
    public void cancel() {
        future.cancel(false);
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public void run() {

    }

    private class FutureInner extends FutureValue<V> {

        FutureInner() {
            super();
        }

        @Override
        public void set(V v) {
            super.set(v);
        }

        @Override
        public void setException(Throwable t) {
            super.setException(t);
        }

        @Override
        protected void done() {
            try {
                final V result = get();
                if (resultConsumer == null) {
                    return;
                }
                executor.execute(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            resultConsumer.onResult(result);
                        } catch (Exception e) {
                            errorConsumer.onError(e);
                        }
                    }
                });
            } catch (final Exception e) {
                if (e instanceof ExecutionException) {
                    if (errorConsumer == null) {
                        throw new RuntimeException(e);
                    }
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            errorConsumer.onError(e);
                        }
                    });
                } else {
                    if (onCancelListener == null) {
                        return;
                    }
                    executor.execute(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                onCancelListener.onCancel();
                            } catch (Exception e) {
                                errorConsumer.onError(e);
                            }
                        }
                    });
                }
            }
        }
    }
}
