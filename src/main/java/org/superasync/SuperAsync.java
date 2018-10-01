package org.superasync;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public abstract class SuperAsync<V> {

    public static <V> SuperAsync<V> newInstance(Executor executor, Callable<V> callable) {
        return new SingleSuperAsync<V>(executor, callable);
    }

    public static <V> SuperAsync<V> newIO(Callable<V> callable) {
        return newInstance(ExecutorProviderStaticRef.getExecutorProvider().io(), callable);
    }

    public static <V> SuperAsync<V> newSequential(Callable<V> callable) {
        return newInstance(ExecutorProviderStaticRef.getExecutorProvider().sequential(), callable);
    }


    public static <V> SuperAsync<V> newComputation(Callable<V> callable) {
        return newInstance(ExecutorProviderStaticRef.getExecutorProvider().computation(), callable);
    }

    private final Executor executor;

    SuperAsync(Executor executor) {
        this.executor = executor;
    }

    Future<V> submit(Callable<V> task, BaseObserver<V> observer) {
        SuperFuture<V> future = new SuperFuture<V>(task, observer);
        executor.execute(future);
        return future;
    }

    public final Execution<V> execute(ResultConsumer<V> resultConsumer, ErrorConsumer errorConsumer) {
        return execute(resultConsumer, errorConsumer, null);
    }

    public final Execution<V> execute(ResultConsumer<V> resultConsumer) {
        return execute(resultConsumer, null);
    }


    public final Execution<V> execute(ResultConsumer<V> resultConsumer, ErrorConsumer errorConsumer,
                                   OnCancelListener onCancelListener) {
        return execute(resultConsumer, errorConsumer, onCancelListener, null);
    }

    public final Execution<V> execute(ResultConsumer<V> resultConsumer,
                                   ErrorConsumer errorConsumer,
                                   OnCancelListener onCancelListener, Executor observingExecutor) {
        CancellersHolder cancellersHolder = new CancellersHolder();
        Observer<V> observer = new Observer<V>(
                observingExecutor != null ? observingExecutor
                        : ExecutorProviderStaticRef.getExecutorProvider().defaultObserving(),
                resultConsumer, errorConsumer, onCancelListener);
        cancellersHolder.add(observer.future);
        execute(observer, cancellersHolder);
        return new Execution<V>(cancellersHolder, observer.future);
    }


    abstract void execute(BaseObserver<V> observer, CancellersHolder cancellersHolder);

    public final <U> SuperAsync<U> andThen(final Transformation<V, U> transformation) {
        return new AndThenSuperAsync<V, U>(executor, this, transformation);
    }

    public final <U, R> SuperAsync<R> zipWith(Callable<U> other, ZipFunc<V, U, R> zipFunc) {
        return new ZipSuperAsync<V, U, R>(executor, this, new SingleSuperAsync<U>(executor, other), zipFunc);
    }

    public final SuperAsync<V> retryWhen(RetryCondition retryCondition) {
        return new RetryingSuperAsync<V>(executor, this, retryCondition);
    }

}
