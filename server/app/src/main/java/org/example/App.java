package org.example;

import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

public class App {
    public static void main(String[] args) {
        try {
            System.out.println("Starting server");
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

            server.createContext("/search", new ServerHandler());
            server.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0)));
        } catch (Exception e) {
            System.out.println("Error starting server " + e.getMessage());
        }
    }
}
