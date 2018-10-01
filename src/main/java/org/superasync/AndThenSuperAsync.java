package org.superasync;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

class AndThenSuperAsync<U, V> extends SuperAsync<V> {

    private final SuperAsync<U> original;
    private final Transformation<U, V> transformation;

    AndThenSuperAsync(Executor executor, SuperAsync<U> original, Transformation<U, V> transformation) {
        super(executor);
        this.original = original;
        this.transformation = transformation;
    }

    @Override
    void execute(final BaseObserver<V> observer, final CancellersHolder cancellersHolder) {
        original.execute(new BaseObserver<U>() {
            @Override
            public void onResult(final U result) {
                Future<V> future = submit(new Callable<V>() {
                    @Override
                    public V call() throws Exception {
                        return transformation.perform(result);
                    }
                }, observer);
                cancellersHolder.add(future);
            }

            @Override
            public void onError(Throwable e) {
                observer.onError(e);
            }

            @Override
            public boolean isObserving() {
                return observer.isObserving();
            }
        }, cancellersHolder);
    }
}