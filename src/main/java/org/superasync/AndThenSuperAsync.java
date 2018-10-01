package org.superasync;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

class AndThenSuperAsync<U, V> extends SuperAsync<V> {

    private final SuperAsync<U> original;
    private final Transformation<U, V> transformation;

    AndThenSuperAsync(Executor executor, SuperAsync<U> original, Transformation<U, V> transformation) {
        super(executor);
        this.original = original;
        this.transformation = transformation;
    }

    @Override
    void execute(final Observer<V> observer, final Canceller canceller) {
        original.execute(new Observer<U>() {
            @Override
            public void onResult(final U result) {
                CancellableTask cancellableTask = submit(new Callable<V>() {
                    @Override
                    public V call() throws Exception {
                        return transformation.perform(result);
                    }
                }, observer);
                canceller.add(cancellableTask);
            }

            @Override
            public void onError(Throwable e) {
                observer.onError(e);
            }

            @Override
            public boolean isObserving() {
                return observer.isObserving();
            }
        }, canceller);
    }
}
