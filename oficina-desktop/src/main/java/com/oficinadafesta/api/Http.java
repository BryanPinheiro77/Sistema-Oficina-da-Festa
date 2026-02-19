package com.oficinadafesta.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Http {

    private final String baseUrl;
    private final HttpClient client;
    private final ObjectMapper mapper;

    public Http(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length()-1) : baseUrl;
        this.client = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule()); // LocalDateTime etc.
    }

    private URI uri(String path) {
        String p = path.startsWith("/") ? path : "/" + path;
        return URI.create(baseUrl + p);
    }

    public <T> T get(String path, Class<T> responseType) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(uri(path))
                .GET()
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() < 200 || res.statusCode() >= 300) {
            throw new RuntimeException("HTTP " + res.statusCode() + " - " + res.body());
        }
        return mapper.readValue(res.body(), responseType);
    }

    public <T> T post(String path, Object body, Class<T> responseType) throws Exception {
        String json = mapper.writeValueAsString(body);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(uri(path))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() < 200 || res.statusCode() >= 300) {
            throw new RuntimeException("HTTP " + res.statusCode() + " - " + res.body());
        }

        // Alguns endpoints podem retornar texto simples (login retorna string/setor)
        if (responseType == String.class) {
            return responseType.cast(res.body().replace("\"", ""));
        }
        return mapper.readValue(res.body(), responseType);
    }
}
