package org.superasync;

import java.util.concurrent.*;

class DefaultExecutorProviderHolder {

    static final Executor SYNC_EXECUTOR = new Executor() {
        @Override
        public void execute(Runnable command) {
            command.run();
        }
    };

    private static final ExecutorProvider DEFAULT_EXECUTOR_PROVIDER = new ExecutorProvider() {

        private final Executor io = Executors.newCachedThreadPool();
        private final Executor sequential = Executors.newSingleThreadExecutor();
        private final Executor computational = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        private final Scheduler scheduler = new Scheduler() {
            @Override
            public Future<?> schedule(Runnable task, long delay) {
                return scheduledExecutorService.schedule(task, delay, TimeUnit.MILLISECONDS);
            }
        };
        private final Executor observing;

        {
            observing = SYNC_EXECUTOR;
        }

        @Override
        public Executor io() {
            return io;
        }

        @Override
        public Executor sequential() {
            return sequential;
        }

        @Override
        public Executor computation() {
            return computational;
        }

        @Override
        public Executor defaultObserving() {
            return observing;
        }

        @Override
        public Scheduler scheduler() {
            return scheduler;
        }
    };

    static ExecutorProvider getDefaultExecutorProvider() {
        return DEFAULT_EXECUTOR_PROVIDER;
    }
}
