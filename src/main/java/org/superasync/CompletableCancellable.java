package org.superasync;

public interface CompletableCancellable extends Cancellable, Completable {
    interface ErrorEmitting extends CompletableCancellable {
        void setErrorConsumer(ErrorConsumer errorConsumer);
    }
}
