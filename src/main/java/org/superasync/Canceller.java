package org.superasync;

import java.util.Iterator;

class Canceller extends Publisher<Completable.Cancellable> implements Cancellable {

    private static final int INITIAL = 0, CANCELLED = 1, INTERRUPTED = 2;

    Canceller() {
        super(INITIAL);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return compareAndPublishRevision(INITIAL, mayInterruptIfRunning ? INTERRUPTED : CANCELLED);
    }

    void add(Completable.Cancellable cancellable) {
        subscribe(cancellable);
        Iterator<Wrapper> iterator = wrappers.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getObject().isDone()) {
                iterator.remove();
            }
        }
    }

    @Override
    void notifySubscriber(int revision, Wrapper wrapper) {
        switch (revision) {
            case CANCELLED:
            case INTERRUPTED:
                wrapper.getObject().cancel(revision == INTERRUPTED);
                wrapper.remove();
                break;
        }
    }
}
