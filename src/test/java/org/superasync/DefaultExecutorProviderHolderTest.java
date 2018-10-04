package org.superasync;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DefaultExecutorProviderHolderTest {

    private int value = 0;

    @Test
    public void scheduler() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        DefaultExecutorProviderHolder.getDefaultExecutorProvider().scheduler().schedule(new Runnable() {
            @Override
            public void run() {
                value = 1;
                latch.countDown();
            }
        }, 10);
        latch.await(100, TimeUnit.MILLISECONDS);
        Assert.assertEquals(1, value);
    }

}