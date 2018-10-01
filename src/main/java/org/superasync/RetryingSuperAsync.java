package org.superasync;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

class RetryingSuperAsync<V> extends SuperAsync<V> {

    private final SuperAsync<V> superAsync;
    private final RetryCondition condition;

    RetryingSuperAsync(Executor executor, SuperAsync<V> superAsync, RetryCondition condition) {
        super(executor);
        this.superAsync = superAsync;
        this.condition = condition;
    }

    @Override
    public void execute(final BaseObserver<V> baseObserver, CancellersHolder cancellersHolder) {
        AtomicInteger count = new AtomicInteger(0);
        superAsync.execute(new ErrorConsumerInner(baseObserver, count, cancellersHolder), cancellersHolder);
    }

    private class ErrorConsumerInner implements BaseObserver<V> {
        private final BaseObserver<V> original;
        private final AtomicInteger count;
        private final CancellersHolder cancellersHolder;

        ErrorConsumerInner(BaseObserver<V> original, AtomicInteger count,
                           CancellersHolder cancellersHolder) {
            this.original = original;
            this.count = count;
            this.cancellersHolder = cancellersHolder;
        }

        @Override
        public void onResult(V result) {
            original.onResult(result);
        }

        @Override
        public void onError(Throwable e) {
            final long delay = condition.check(e, count.getAndIncrement());
            if (delay < 0) {
                original.onError(e);
            } else {
                if (delay == 0L) {
                    superAsync.execute(this, cancellersHolder);
                } else {
                    cancellersHolder.add(ExecutorProviderStaticRef.getExecutorProvider().scheduler().schedule(new Runnable() {
                        @Override
                        public void run() {
                            superAsync.execute(ErrorConsumerInner.this, cancellersHolder);
                        }
                    }, delay));
                }
            }
        }

        @Override
        public boolean isObserving() {
            return original.isObserving();
        }
    }
}
