package net.sasakonnect.wifi_portal.beans;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Component;

@Component
public class TransactionProcessor {
    private final ExecutorService executor = Executors.newFixedThreadPool(100); // Pool of 100 threads
   
    
//    private final TransactionQueue transactionQueue = new TransactionQueue(); // Your custom message queue
//
//    public void processTransaction(Transaction transaction) {
//        // Submit transaction to executor pool for processing
//        executor.submit(() -> handleTransaction(transaction));
//    }
//
//    private void handleTransaction(Transaction transaction) {
//        // Perform transaction logic here (e.g., confirmation)
//        System.out.println("Processing transaction: " + transaction);
//        // You could also enqueue the transaction in a message queue for later processing
//        transactionQueue.add(transaction);
//    }

}
