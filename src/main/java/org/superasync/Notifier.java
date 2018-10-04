package org.superasync;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

abstract class Notifier<C> {
    private final Collection<Wrapper> collection = new ConcurrentLinkedQueue<Wrapper>();
    private final AtomicBoolean isNotified = new AtomicBoolean(false);

    Wrapper add(C callback) {

        Wrapper w = new Wrapper(callback);
        if (isNotified.get()) {
            notifyCallback(callback);
            return w;
        }
        collection.add(w);
        if (isNotified.get()) {
            notifyCallback(callback);
        }
        return w;

    }

    void notifyCallbacks() {
        if (isNotified.compareAndSet(false, true)) {
            for (Wrapper w : collection) {
                w.notifyCallback();
            }
        }
    }

    abstract void notifyCallback(C callback);

    class Wrapper implements Removable {
        private final C callback;
        private AtomicBoolean isNotified = new AtomicBoolean(false);

        private Wrapper(C callback) {
            this.callback = callback;
        }

        void notifyCallback() {
            if (isNotified.compareAndSet(false, true)) {
                Notifier.this.notifyCallback(callback);
            }
        }

        @Override
        public void remove() {
            collection.remove(this);
        }
    }
}
