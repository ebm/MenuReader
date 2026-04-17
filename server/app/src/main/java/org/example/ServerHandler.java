package org.example;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class ServerHandler implements HttpHandler {
    public void sendInvalidRequestResponse(HttpExchange exchange, String response) throws IOException {
        exchange.sendResponseHeaders(400, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response;
        String food;
        String images;
        try {
            String query = exchange.getRequestURI().getQuery();
            String[] parsed = query.split("&");
            food = URLDecoder.decode(parsed[0].split("=")[1], "UTF-8");
            images = URLDecoder.decode(parsed[1].split("=")[1], "UTF-8");
        } catch (Exception e) {
            sendInvalidRequestResponse(exchange, "Invalid query.");
            return;
        }
        response = "Received query: " + food + ", " + images;
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
