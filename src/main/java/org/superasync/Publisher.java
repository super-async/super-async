package org.superasync;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

abstract class Publisher<S> {
    private final AtomicInteger revision;
    final Collection<Wrapper> wrappers = new ConcurrentLinkedQueue<Wrapper>();
    private final int initialRevision;

    Publisher(int initialRevision) {
        revision = new AtomicInteger(initialRevision);
        this.initialRevision = initialRevision;
    }

    boolean publishRevision(int revision) {
        int old = this.revision.getAndSet(revision);
        if (old != revision) {
            updateWrappers(revision);
            return true;
        }
        return false;
    }

    private void updateWrappers(int revision) {
        for (Wrapper w : wrappers) {
            w.update(revision);
        }
    }

    Wrapper subscribe(S subscriber) {

        int currentRevision = revision.get();
        Wrapper wrapper = new Wrapper(subscriber);
        wrapper.update(currentRevision);

        if (revisionIsFinal(currentRevision)) {
            return wrapper;
        }

        wrappers.add(wrapper);

        wrapper.update(revision.get());

        return wrapper;
    }


    abstract void notifySubscriber(int revision, S subscriber);

    abstract boolean revisionIsFinal(int revision);

    class Wrapper implements Removable {

        private final S subscriber;
        private final AtomicInteger revision;
        Wrapper(S subscriber) {
            this.subscriber = subscriber;
            this.revision = new AtomicInteger(initialRevision);
        }

        void update(int revision) {
            int old = this.revision.getAndSet(revision);
            if (old != revision) {
                notifySubscriber(revision, subscriber);
                if (revisionIsFinal(revision)) {
                    remove();
                }
            }
        }

        S getObject() {
            return subscriber;
        }

        @Override
        public void remove() {
            wrappers.remove(this);
        }
    }
}
