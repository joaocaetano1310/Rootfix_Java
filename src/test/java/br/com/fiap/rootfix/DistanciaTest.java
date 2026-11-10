package br.com.fiap.rootfix;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import br.com.fiap.rootfix.service.Distancia;

class DistanciaTest {
    @Test
    void calculaDistanciaEntreDoisPontosDeSaoPaulo() {
        double km = Distancia.km(new BigDecimal("-23.5640"), new BigDecimal("-46.6527"),
                new BigDecimal("-23.5578"), new BigDecimal("-46.5999"));
        assertThat(km).isBetween(5.3, 5.6);
    }

    @Test
    void mesmoPontoTemDistanciaZero() {
        BigDecimal lat = new BigDecimal("-23.5640");
        BigDecimal lon = new BigDecimal("-46.6527");
        assertThat(Distancia.km(lat, lon, lat, lon)).isZero();
    }
}
