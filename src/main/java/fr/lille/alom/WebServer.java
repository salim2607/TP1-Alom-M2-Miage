package fr.lille.alom;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebServer {
    private int port;
    private ServerSocket serverSocket;
    private boolean isRunning;
    private ExecutorService threadPool;
    private ConcurrentLinkedQueue<Request> requestQueue;
    private RequestAnalyzer requestAnalyzer;

    public WebServer(int port) {
        this.port = port;
        this.isRunning = false;
        this.threadPool = Executors.newCachedThreadPool();
        this.requestQueue = new ConcurrentLinkedQueue<>();
        this.requestAnalyzer = new RequestAnalyzer();
        
        // Register the HelloWorldServlet
        ServletFactory.getInstance().registerServlet("hello", new HelloWorldServlet());
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            System.out.println("Server started on port " + port);
            
            // Start request processor threads
            startRequestProcessors();
            
            // Main server loop
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New connection from: " + clientSocket.getInetAddress());
                    
                    // Create request and add to queue
                    Request request = new Request(clientSocket);
                    requestQueue.add(request);
                    
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("Error accepting connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server on port " + port + ": " + e.getMessage());
        }
    }

    private void startRequestProcessors() {
        // Start multiple processor threads
        for (int i = 0; i < 10; i++) {
            threadPool.execute(new RequestProcessor());
        }
    }

    public void stop() {
        isRunning = false;
        threadPool.shutdown();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
        System.out.println("Server stopped");
    }

    private class RequestProcessor implements Runnable {
        @Override
        public void run() {
            while (isRunning) {
                try {
                    Request request = requestQueue.poll();
                    if (request != null) {
                        processRequest(request);
                    } else {
                        // Sleep briefly to avoid busy waiting
                        Thread.sleep(10);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Error in request processor: " + e.getMessage());
                }
            }
        }

        private void processRequest(Request request) {
            try {
                requestAnalyzer.handleRequest(request);
            } catch (CanNotCompleteTheRequestException e) {
                try {
                    new ErrorRequestHandler().handleRequest(request);
                } catch (IOException ex) {
                    System.err.println("Error handling failed request: " + ex.getMessage());
                }
            } catch (IOException e) {
                System.err.println("IO Error processing request: " + e.getMessage());
            } finally {
                closeSocket(request.getSocket());
            }
        }

        private void closeSocket(Socket socket) {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number: " + args[0] + ". Using default port 8080.");
            }
        }

        WebServer server = new WebServer(port);
        
        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            server.stop();
        }));

        server.start();
    }
}