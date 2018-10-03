package org.superasync;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

interface Task extends Runnable, Completable.Cancellable {

    class Factory {
        static <V> Task fromCallable(Callable<V> callable, Observer<V> observer) {
            return new FromCallable<V>(callable, observer);
        }
    }

    class FromCallable<V> implements Task {

        private enum State {INITIAL, RUNNING, DONE}

        private final AtomicReference<State> state = new AtomicReference<State>(State.INITIAL);
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
            if (!state.compareAndSet(State.INITIAL, State.RUNNING)) {
                return;
            }
            runner = Thread.currentThread();
            try {
                result = task.call();
            } catch (Exception e) {
                if (state.compareAndSet(State.RUNNING, State.DONE)) {
                    observer.onError(e);
                }
            }
            if (state.compareAndSet(State.RUNNING, State.DONE)) {
                observer.onResult(result);
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {

            State old = state.getAndSet(State.DONE);
            if (old == State.DONE) {
                return false;
            }

            if (mayInterruptIfRunning) {
                Thread r = runner;
                if (r != null) {
                    r.interrupt();
                }
            }
            return true;
        }

        @Override
        public boolean isDone() {
            return state.get() == State.DONE;
        }
    }
}
