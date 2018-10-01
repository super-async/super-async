package org.superasync;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class SuperFuture<V> extends FutureTask<V> {

    private final BaseObserver<V> callback;

    SuperFuture(Callable<V> callable, BaseObserver<V> callback) {
        super(callable);
        this.callback = callback;
    }

    @Override
    protected void done() {
        //noinspection CatchMayIgnoreException
        try {
            V result = get();
            callback.onResult(result);
        } catch (Exception e) {
            if (e instanceof ExecutionException) {
                callback.onError(e.getCause());
            }
        }
    }
}
