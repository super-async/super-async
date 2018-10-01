package org.superasync;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

interface CancellableTask extends Runnable {

    void cancel();

    boolean isDone();

    interface ErrorEmitting extends CancellableTask {
        void setErrorConsumer(ErrorConsumer errorConsumer);
    }

    class Factory {
        static <V> CancellableTask fromCallable(Callable<V> callable, BaseObserver<V> observer) {
            return new FromCallable<V>(callable, observer);
        }
    }

    class FromCallable<V> implements CancellableTask {

        private final AtomicBoolean isDone = new AtomicBoolean(false);
        private final Callable<V> task;
        private final BaseObserver<V> observer;
        private V result;

        FromCallable(Callable<V> task, BaseObserver<V> observer) {
            this.task = task;
            this.observer = observer;
        }

        @Override
        public void run() {
            try {
                result = task.call();
            } catch (Exception e) {
                if (isDone.compareAndSet(false, true)) {
                    observer.onError(e);
                }
            }
            if (isDone.compareAndSet(false, true)) {
                observer.onResult(result);
            }
        }

        @Override
        public void cancel() {
            isDone.compareAndSet(false, true);
        }

        @Override
        public boolean isDone() {
            return isDone.get();
        }
    }
}
