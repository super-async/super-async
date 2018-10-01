package org.superasync;

interface Observer<V> {
    void onResult(V result);

    void onError(Throwable e);

    boolean isObserving();
}
