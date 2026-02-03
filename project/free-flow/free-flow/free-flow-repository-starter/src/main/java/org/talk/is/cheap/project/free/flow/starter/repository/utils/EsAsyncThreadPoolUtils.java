package org.talk.is.cheap.project.free.flow.starter.repository.utils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EsAsyncThreadPoolUtils {

    public final static ThreadPoolExecutor ES_ASYNC_THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(4,8,30, TimeUnit.SECONDS,new LinkedBlockingQueue<>());
}
