package org.example;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.List;

import org.json.JSONArray;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ServerHandler implements HttpHandler {
    public void sendInvalidRequestResponse(HttpExchange exchange, String response) throws IOException {
        System.out.println("Sending invalid response");
        exchange.sendResponseHeaders(400, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("Received request");
        String food;
        int images;
        try {
            String query = exchange.getRequestURI().getRawQuery();
            System.out.println("Received query: " + query);
            String[] parsed = query.split("&");
            food = URLDecoder.decode(parsed[0].split("=")[1], "UTF-8");
            images = Integer.parseInt(URLDecoder.decode(parsed[1].split("=")[1], "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            sendInvalidRequestResponse(exchange, "Invalid query.");
            return;
        }
        System.out.println("Searching for images...");
        Cache cache;
        try {
            cache = new Cache();
        } catch (Exception e) {
            System.out.println("Server not properly initialized.");
            sendInvalidRequestResponse(exchange, "Server not properly initialized.");
            return;
        }
        List<String> res = ImageDeliver.search(food, images, cache);
        JSONArray ja = new JSONArray(res);

        String response = ja.toString();
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
