package main.java.com.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppManager {
    // Thread pool for background tasks
    public static final ExecutorService threadPool = Executors.newCachedThreadPool();
}
