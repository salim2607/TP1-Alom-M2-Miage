package fr.lille.alom;

import java.util.HashMap;
import java.util.Map;

public class Helloworld extends Thread {
    private long startTime;
    private String name;
    private Map<Long, String> logs = new HashMap<>();
    private volatile boolean running = true;

    public Helloworld(long startTime, String name) {
        this.startTime = startTime;
        this.name = name;
    }

    public void run() {
        while (running) {
            long currentTime = System.nanoTime() - startTime;
            logs.put(currentTime, "Hello " + name + " !");
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopGracefully() {
        running = false;
    }

    public Map<Long, String> getLogs() {
        return logs;
    }
}