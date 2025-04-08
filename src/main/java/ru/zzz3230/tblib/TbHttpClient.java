package ru.zzz3230.tblib;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;

public class TbHttpClient {
    private static final HttpClient client = HttpClient.newHttpClient();

    final String baseUrl;

    public TbHttpClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public static CompletableFuture<HttpResponse<String>> get(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.of(5, ChronoUnit.SECONDS))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
}
