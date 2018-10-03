package org.superasync;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

interface Task extends Runnable, Completable.Cancellable {

    class Factory {
        static <V> Task fromCallable(Callable<V> callable, Observer<V> observer) {
            return new FromCallable<V>(callable, observer);
        }
    }

    class FromCallable<V> implements Task {

        private final AtomicBoolean isDone = new AtomicBoolean(false);
        private final Callable<V> task;
        private final Observer<V> observer;
        private volatile Thread runner;
        private V result;

        FromCallable(Callable<V> task, Observer<V> observer) {
            this.task = task;
            this.observer = observer;
        }

        @Override
        public void run() {
            runner = Thread.currentThread();
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
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (isDone.compareAndSet(false, true)) {
                if (mayInterruptIfRunning) {
                    Thread r = runner;
                    if (r != null) {
                        r.interrupt();
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean isDone() {
            return isDone.get();
        }
    }
}
