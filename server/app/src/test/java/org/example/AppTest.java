package org.example;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLEncoder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

public class AppTest {
    HttpServer server;

    @Before
    public void startServer() throws Exception {
        try {
            server = HttpServer.create(new InetSocketAddress(8000), 0);

            server.createContext("/search", new ServerHandler());
            server.start();
        } catch (Exception e) {
            System.out.println("Error starting server " + e.getMessage());
        }
    }

    @After
    public void stopServer() {
        server.stop(0);
    }

    @Test
    public void testHttpServerResponse() {
        try {
            String encoded = URLEncoder.encode("query=example_food&per_page=4", "UTF-8");
            URL url = new URL("http://localhost:8000/search?" + encoded);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            System.out.println("Http server response: " + sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Http request failed with " + e.getMessage());
        }
    }
}
