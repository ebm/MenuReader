package org.example;

import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

public class App {
    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

            server.createContext("/search", new ServerHandler());
            server.start();
        } catch (Exception e) {
            System.out.println("Error starting server " + e.getMessage());
        }
    }
}
