package org.superasync;

class Observation<V> {

    private final Wrapper<Observer<V>> observer;
    private final SuperFuture<V> future;

    Observation(Wrapper<Observer<V>> observer, SuperFuture<V> future) {
        this.observer = observer;
        this.future = future;
    }

    void stopObservation() {
        observer.remove();
    }
    SuperFuture future() {
        return future;
    };
}
