package org.superasync;

import java.util.Iterator;

class Canceller extends Publisher<Completable.Cancellable> implements Cancellable {

    private static final int INITIAL = 0, CANCELLED = 1, INTERRUPTED = 2;

    Canceller() {
        super(INITIAL);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return publishRevision(mayInterruptIfRunning ? INTERRUPTED : CANCELLED);
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
    void notifySubscriber(int revision, Completable.Cancellable cancellable) {
        switch (revision) {
            case CANCELLED:
                cancellable.cancel(false);
                break;
            case INTERRUPTED:
                cancellable.cancel(true);
                break;
        }
    }

    @Override
    boolean revisionIsFinal(int revision) {
        return revision > INITIAL;
    }
}
