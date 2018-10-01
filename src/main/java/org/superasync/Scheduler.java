package org.superasync;

public interface Scheduler {
    CancellableTask.ErrorEmitting schedule(Runnable task, long delay);
}
