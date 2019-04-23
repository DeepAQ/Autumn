package cn.imaq.autumn.rpc.test;

import cn.imaq.autumn.rpc.client.AutumnRPCClient;
import cn.imaq.autumn.rpc.server.AutumnRPC;
import com.example.test.TestService;

import java.io.IOException;
import java.rmi.RemoteException;

public class AutumnBenchmark {
    private static int NUM_THREADS = 8;

    public static void main(String[] args) throws IOException {
        AutumnRPC.start();
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
            AutumnRPCClient client = new AutumnRPCClient("127.0.0.1", 8801);
            TestService testService = client.getService(TestService.class, 3000);
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
