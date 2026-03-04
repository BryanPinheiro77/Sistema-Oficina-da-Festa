package com.oficinadafesta.delivery.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "delivery.ors")
public class OrsProperties {

    private String apiKey;
    private String originAddress;
}
