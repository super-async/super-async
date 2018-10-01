package org.superasync;

public interface Scheduler {
    CompletableCancellable.ErrorEmitting schedule(Runnable task, long delay);
}
