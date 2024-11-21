package net.sasakonnect.wifi_portal.beans;
import java.util.concurrent.*;

import org.springframework.stereotype.Component;
@Component
public class ThreadExecuterBean {
    int corePoolSize = 1000; // Choose a reasonable number based on your system
    int maxPoolSize = 5000;
    long keepAliveTime = 60L;
	private  ExecutorService executor = new ThreadPoolExecutor(
	            corePoolSize,
	            maxPoolSize,
	            keepAliveTime,
	            TimeUnit.SECONDS,
	            new LinkedBlockingQueue<>(10000), // Bounded queue
	            new ThreadPoolExecutor.CallerRunsPolicy() // If the queue is full, run the task in the caller's thread
	        );
	
	public void addTask(Runnable task) {
		executor.submit(task);
	}
}
