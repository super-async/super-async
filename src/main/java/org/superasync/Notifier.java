package org.superasync;

import java.util.ArrayList;
import java.util.Collection;

abstract class Notifier<C> {
    private final Collection<C> collection = new ArrayList<C>();
    private boolean notified = false;

    synchronized void add(C callback) {
        if (notified) {
            notifyCallback(callback);
            return;
        }
        collection.add(callback);
    }

    synchronized void remove(C callback) {
        collection.remove(callback);
    }

    synchronized void notifyCallbacks() {
        notified = true;
        for (C callback : collection) {
            notifyCallback(callback);
        }
    }

    abstract void notifyCallback(C callback);
}
