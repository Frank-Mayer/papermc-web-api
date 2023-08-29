package io.frankmayer.papermcwebapi;

import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

class HttpFrontend {
    private class HelloWorldHandler implements HttpHandler {
        @Override
        public void handle(final HttpExchange t) throws IOException {
            final String response = "Hello World!";
            t.sendResponseHeaders(200, response.length());
            final OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    private final HttpServer server;

	public HttpFrontend(final int port) throws IOException, BindException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/hello_world", new HelloWorldHandler());
        this.server.setExecutor(null);
        this.server.start();
    }

    public void dispose() {
        this.server.stop(1);
	}
}
