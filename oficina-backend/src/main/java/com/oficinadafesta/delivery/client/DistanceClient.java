package com.oficinadafesta.delivery.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oficinadafesta.delivery.config.OrsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Component
public class DistanceClient {

    private static final Logger log = LoggerFactory.getLogger(DistanceClient.class);
    private static final String GEOCODE_URL = "https://api.openrouteservice.org/geocode/search";
    private static final String DISTANCE_MATRIX_URL = "https://api.openrouteservice.org/v2/matrix/driving-car";

    private final OrsProperties orsProperties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // coordenadas fixas da loja
    private double[] originCoords = null;

    public DistanceClient(OrsProperties orsProperties){
        this.orsProperties = orsProperties;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }


    /**
     * Retorna distancia em km entre a loja e o endereço de entrega.
     * Resultado é cacheado por endereço
     */

    @Cacheable(value = "distanciaEntrega", key = "#enderecoDestino")
    public double calcularDistanciaKm(String enderecoDestino) {
        try{
            double[] origem = getOrigemCoords();
            double[] destino = geocode(enderecoDestino);
            return matrix(origem, destino);
        } catch (Exception e){
            log.error("Erro ao calcular distância para endereço '{}': {}", enderecoDestino, e.getMessage(), e);
            throw new RuntimeException("Não foi possível calcular a distância para o endereço informado.");
        }
    }

    private double[] getOrigemCoords() throws Exception{
        if (originCoords == null) {
            log.info("Geocodificando endereço de origem da loja");
            originCoords = geocode(orsProperties.getOriginAddress());
            log.info("Coordenadas da loja: lon={}, lat={}", originCoords[0], originCoords[1]);
        }
        return originCoords;
    }

    private double[] geocode(String endereco) throws Exception {
        String encoded = URLEncoder.encode(endereco, StandardCharsets.UTF_8);
        String url = GEOCODE_URL + "?api_key=" + orsProperties.getApiKey() + "&text=" + encoded + "&size=1";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode coordsNode = root.path("features").path(0).path("geometry").path("coordinates");

        double lon = coordsNode.get(0).asDouble();
        double lat = coordsNode.get(1).asDouble();

        log.debug("Geocode '{}' -> lon={}, lat={}", endereco, lon, lat);
        return new double[]{lon, lat};

    }

    private double matrix(double[] origem, double[] destino) throws Exception {
        String body = String.format(
                "{\"locations\":[[%f,%f],[%f,%f]],\"metrics\":[\"distance\"]}",
                origem[0], origem[1], destino[0], destino[1]
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DISTANCE_MATRIX_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", orsProperties.getApiKey())
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode root = objectMapper.readTree(response.body());
        double distanciaMetros = root.path("distances").get(0).get(1).asDouble();
        double distanciaKm = distanciaMetros / 1000.0;

        log.debug("Distância calculada: {}m = {}km", distanciaMetros, distanciaKm);
        return distanciaKm;
    }
}
