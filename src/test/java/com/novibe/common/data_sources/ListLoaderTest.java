package com.novibe.common.data_sources;

import com.novibe.common.base_structures.BypassRoute;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListLoaderTest {

    private HttpServer server;
    private String baseUrl;
    private HostsOverrideListsLoader loader;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        addRoute("/hosts", 200, "1.2.3.4 chatgpt.com\n1.2.3.4 openai.com\n");
        addRoute("/not-found", 404, "404: Not Found");
        addRoute("/rate-limited", 429, "429: Too Many Requests");
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        loader = new HostsOverrideListsLoader();
        loader.setClient(HttpClient.newHttpClient());
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void loadsRoutesFromRespondingSource() {
        List<BypassRoute> routes = loader.fetchWebsites(List.of(baseUrl + "/hosts"));

        assertEquals(List.of("chatgpt.com", "openai.com"), routes.stream().map(BypassRoute::website).toList());
    }

    @Test
    void failsWhenSourceIsNotFound() {
        assertTrue(messageOfFailure(baseUrl + "/not-found").contains("response code 404"));
    }

    @Test
    void failsWhenSourceIsRateLimited() {
        assertTrue(messageOfFailure(baseUrl + "/rate-limited").contains("response code 429"));
    }

    @Test
    void failsWhenOnlyOneOfSourcesIsBroken() {
        List<String> urls = List.of(baseUrl + "/hosts", baseUrl + "/not-found");

        assertThrows(Exception.class, () -> loader.fetchWebsites(urls));
    }

    private String messageOfFailure(String url) {
        Exception exception = assertThrows(Exception.class, () -> loader.fetchWebsites(List.of(url)));
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause.getMessage() != null && cause.getMessage().contains("response code")) {
                return cause.getMessage();
            }
        }
        return "";
    }

    private void addRoute(String path, int code, String body) {
        server.createContext(path, exchange -> {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(code, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
    }
}
