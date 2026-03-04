package com.oficinadafesta.delivery.config;



import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Data
@ConfigurationProperties(prefix = "delivery.fee")
public class DeliveryFeeProperties {

    private Tier tier1 = new Tier(1.0, new BigDecimal("5.00"));
    private Tier tier2 = new Tier(1.5, new BigDecimal("7.50"));
    private Tier tier3 = new Tier(2.0, new BigDecimal("10.00"));
    private AboveTier aboveTier3 = new AboveTier(new BigDecimal("10.00"), new BigDecimal("2.50"));

    public static class Tier {
        private double maxKm;
        private BigDecimal value;

        public Tier() {
        }

        public Tier(double maxKm, BigDecimal value) {
            this.maxKm = maxKm;
            this.value = value;
        }

        public double getMaxKm() {
            return maxKm;
        }

        public void setMaxKm(double maxKm) {
            this.maxKm = maxKm;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }
    }
        public static class AboveTier {
            private BigDecimal base;
            private BigDecimal PerKm;

            public AboveTier() {}
            public AboveTier(BigDecimal baseFee, BigDecimal additionalPerKm) {
                this.base = baseFee;
                this.PerKm = additionalPerKm;
            }
            public BigDecimal getBase() {return base;}
            public void setBase(BigDecimal base) {this.base = base;}
            public BigDecimal getPerKm() {return PerKm;}
            public void setPerKm(BigDecimal perKm) {this.PerKm = perKm;}
        }
    }

