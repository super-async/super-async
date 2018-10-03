package org.superasync;

public class Observation<V> {

    private final Observer<V> observer;
    private final SuperFuture<V> future;

    Observation(Observer<V> observer, SuperFuture<V> future) {
        this.observer = observer;
        this.future = future;
    }

    void stopObservation() {
        future.removeCallback(observer);
    }
    SuperFuture future() {
        return future;
    };
}
