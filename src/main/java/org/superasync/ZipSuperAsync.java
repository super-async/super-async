package org.superasync;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

public class ZipSuperAsync<U, V, R> extends SuperAsync<R> {

    private final SuperAsync<U> superAsync1;
    private final SuperAsync<V> superAsync2;
    private final ZipFunc<U, V, R> zipFunc;

    ZipSuperAsync(Executor executor, SuperAsync<U> superAsync1, SuperAsync<V> superAsync2,
                  ZipFunc<U, V, R> zipFunc) {
        super(executor);
        this.superAsync1 = superAsync1;
        this.superAsync2 = superAsync2;
        this.zipFunc = zipFunc;
    }

    @Override
    public void execute(BaseObserver<R> observer, CancellersHolder cancellersHolder) {

        AtomicReference<Object> firstOne = new AtomicReference<Object>(null);

        superAsync1.execute(new ResultConsumer1(observer, cancellersHolder, firstOne), cancellersHolder);
        superAsync2.execute(new ResultConsumer2(observer, cancellersHolder, firstOne), cancellersHolder);
    }

    private class ResultConsumer1 implements BaseObserver<U> {

        private final CancellersHolder cancellersHolder;
        private final BaseObserver<R> observer;
        private final AtomicReference<Object> firstOne;

        ResultConsumer1(BaseObserver<R> observer,
                        CancellersHolder cancellersHolder,
                        AtomicReference<Object> firstOne) {
            this.observer = observer;
            this.cancellersHolder = cancellersHolder;
            this.firstOne = firstOne;
        }

        @Override
        public void onResult(final U result) {
            if (!firstOne.compareAndSet(null, result)) {
                Future<R> future = submit(new Callable<R>() {
                    @Override
                    public R call() throws Exception {
                        //noinspection unchecked
                        return zipFunc.zip(result, (V) firstOne.get());
                    }
                }, observer);
                cancellersHolder.add(future);
            }
        }

        @Override
        public void onError(Throwable e) {
            observer.onError(e);
        }

        @Override
        public boolean isObserving() {
            return observer.isObserving();
        }
    }

    private class ResultConsumer2 implements BaseObserver<V> {

        private final CancellersHolder cancellersHolder;
        private final BaseObserver<R> observer;
        private final AtomicReference<Object> firstOne;

        ResultConsumer2(BaseObserver<R> observer,
                        CancellersHolder cancellersHolder,
                        AtomicReference<Object> firstOne) {
            this.observer = observer;
            this.cancellersHolder = cancellersHolder;
            this.firstOne = firstOne;
        }

        @Override
        public void onResult(final V result) {
            if (!firstOne.compareAndSet(null, result)) {
                Future<R> future = submit(new Callable<R>() {
                    @Override
                    public R call() throws Exception {
                        //noinspection unchecked
                        return zipFunc.zip((U) firstOne.get(), result);
                    }
                }, observer);
                cancellersHolder.add(future);
            }
        }

        @Override
        public void onError(Throwable e) {
            observer.onError(e);
        }

        @Override
        public boolean isObserving() {
            return observer.isObserving();
        }
    }
}
