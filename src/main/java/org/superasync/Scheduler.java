package org.superasync;

import java.util.concurrent.Future;

public interface Scheduler {
    Future<?> schedule(Runnable task, long delay);
}
