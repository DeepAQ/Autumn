package cn.imaq.autumn.rpc.test;

import com.example.test.TestService;
import com.example.test.TestServiceImpl;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

public class RMIBenchmark {
    private static int NUM_THREADS = 8;

    public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {
        LocateRegistry.createRegistry(8887);
        Naming.rebind("rmi://127.0.0.1:8887/TestService", new TestServiceImpl());
        ClientThread[] threads = new ClientThread[NUM_THREADS];
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new ClientThread();
            threads[i].start();
        }
        new Thread(() -> {
            long latest = System.currentTimeMillis();
            int latestQuery = 0;
            while (true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                long current = System.currentTimeMillis();
                int totalQuery = 0;
                for (int i = 0; i < NUM_THREADS; i++) {
                    totalQuery += threads[i].queryCount;
                }
                int qps = (int) ((totalQuery - latestQuery) * 1000 / (current - latest));
                System.out.println("Current QPS: " + qps);
                latest = current;
                latestQuery = totalQuery;
            }
        }).start();
    }

    static class ClientThread extends Thread {
        int queryCount = 0;

        @Override
        public void run() {
            TestService testService = null;
            try {
                testService = (TestService) Naming.lookup("rmi://127.0.0.1:8887/TestService");
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                e.printStackTrace();
            }
            while (true) {
                try {
                    testService.echo("Hello");
                    queryCount++;
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
