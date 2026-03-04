package com.oficinadafesta.delivery.service;

import com.oficinadafesta.delivery.client.DistanceClient;
import com.oficinadafesta.delivery.config.DeliveryFeeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DeliveryFeeService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryFeeService.class);

    private final DistanceClient distanceClient;
    private final DeliveryFeeProperties props;

    public DeliveryFeeService(DistanceClient distanceClient, DeliveryFeeProperties props) {
        this.distanceClient  = distanceClient;
        this.props = props;
    }

    /**
     * Calcula a taxa de entrega para um endereço
     * Em caso de falha na api usa fallback por distancia manual.
     */
    public BigDecimal calcularTaxa(String enderecoDestino){
        try{
            double km = distanceClient.calcularDistanciaKm(enderecoDestino);
            BigDecimal taxa = calcularPorFaixaKm(km);
            log.info("Taxa calculada via API: emdereco = '{}', km = {}, taxa = {}", enderecoDestino, km, taxa);
            return taxa;
        } catch (Exception e){
            log.warn("Falha na API de distancia, usando fallback: {}", e.getMessage());
            return fallback();
        }
    }

    /**
     * Fallback: Retorna maior faixa como valor conservador
     *
     */
    public BigDecimal fallback() {
        log.warn("Usando taxa fallback de entrega");
        return props.getTier3().getValue();
    }

    private BigDecimal calcularPorFaixaKm(double km) {
        DeliveryFeeProperties.Tier t1 = props.getTier1();
        DeliveryFeeProperties.Tier t2 = props.getTier2();
        DeliveryFeeProperties.Tier t3 = props.getTier3();
        DeliveryFeeProperties.AboveTier above = props.getAboveTier3();

        if (km <= t1.getMaxKm()) return t1.getValue();
        if (km <= t2.getMaxKm()) return t2.getValue();
        if (km <= t3.getMaxKm()) return t3.getValue();

        BigDecimal extra = BigDecimal.valueOf(km - t3.getMaxKm())
                .multiply(above.getPerKm())
                .setScale(2, RoundingMode.HALF_UP);

        return above.getBase().add(extra);
    }


}
