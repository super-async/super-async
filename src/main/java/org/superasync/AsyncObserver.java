package org.superasync;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class AsyncObserver<V> implements Observer<V>, Completable.Cancellable {

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
    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(false);
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    private void deliverResult(final V result) {
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
    }

    private void deliverError(final Throwable error) {
        if (errorConsumer == null) {
            throw new RuntimeException(error);
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                errorConsumer.onError(error);
            }
        });
    }

    private void deliverCancellation() {
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
            V result;
            Exception exception;
            try {
                result = get();
                exception = null;
            } catch (Exception e) {
                result = null;
                exception = e;
            }

            if (exception == null) {
                deliverResult(result);
            } else if (exception instanceof ExecutionException) {
                deliverError(exception.getCause());
            } else {
                deliverCancellation();
            }
        }
    }
}
