package org.superasync;

public interface Completable {
    boolean isDone();

    interface Cancellable extends Completable {
        void cancel();

        interface ErrorEmitting extends Completable.ErrorEmitting, Cancellable {
        }
    }

    interface ErrorEmitting extends Completable {
        void setErrorConsumer(ErrorConsumer errorConsumer);
    }
}
