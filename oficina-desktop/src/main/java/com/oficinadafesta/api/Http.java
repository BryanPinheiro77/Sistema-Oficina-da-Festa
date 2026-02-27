package com.oficinadafesta.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Http {

    public interface UnauthorizedListener {
        void onUnauthorized(int statusCode, String body);
    }

    private final String baseUrl;
    private final HttpClient client;
    private final ObjectMapper mapper;

    private String bearerToken;
    private UnauthorizedListener unauthorizedListener;

    public void setBearerToken(String token) {
        this.bearerToken = token;
    }

    public void setUnauthorizedListener(UnauthorizedListener listener) {
        this.unauthorizedListener = listener;
    }

    public Http(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    private URI uri(String path) {
        String p = path.startsWith("/") ? path : "/" + path;
        return URI.create(baseUrl + p);
    }

    private HttpRequest.Builder baseRequest(String path) {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(uri(path))
                .header("Accept", "application/json");

        if (bearerToken != null && !bearerToken.isBlank()) {
            b.header("Authorization", "Bearer " + bearerToken);
        }
        return b;
    }

    private void handleIfUnauthorized(HttpResponse<String> res) {
        if (res.statusCode() == 401 && unauthorizedListener != null) {
            unauthorizedListener.onUnauthorized(res.statusCode(), res.body());
        }
    }

    public <T> T get(String path, Class<T> responseType) throws Exception {
        HttpRequest req = baseRequest(path)
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        handleIfUnauthorized(res);

        if (res.statusCode() < 200 || res.statusCode() >= 300) {
            throw new RuntimeException("HTTP " + res.statusCode() + " - " + res.body());
        }
        return mapper.readValue(res.body(), responseType);
    }

    public <T> T post(String path, Object body, Class<T> responseType) throws Exception {
        String json = mapper.writeValueAsString(body);

        HttpRequest req = baseRequest(path)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        handleIfUnauthorized(res);

        if (res.statusCode() < 200 || res.statusCode() >= 300) {
            throw new RuntimeException("HTTP " + res.statusCode() + " - " + res.body());
        }

        if (responseType == String.class) {
            return responseType.cast(res.body().replace("\"", ""));
        }
        return mapper.readValue(res.body(), responseType);
    }
}