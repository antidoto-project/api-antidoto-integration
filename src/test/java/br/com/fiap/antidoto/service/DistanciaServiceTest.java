package br.com.fiap.antidoto.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class DistanciaServiceTest {

    private final DistanciaService distancia = new DistanciaService();

    @Test
    void pracaDaSeAteOMaspDaUnsDoisQuilometrosEMeio() {
        double km = distancia.calcularKm(-23.5505, -46.6333, -23.5614, -46.6559);

        // MVP-01 §11: Sé × Av. Paulista ≈ 2,5–3 km
        assertThat(km).isBetween(2.5, 3.0);
    }

    @Test
    void mesmoPontoEZero() {
        assertThat(distancia.calcularKm(-23.5, -46.6, -23.5, -46.6)).isEqualTo(0.0, within(1e-9));
    }

    @Test
    void arredondaParaUmaCasa() {
        assertThat(distancia.arredondar(2.449)).isEqualTo(2.4);
        assertThat(distancia.arredondar(2.45)).isEqualTo(2.5);
        assertThat(distancia.arredondar(0.04)).isEqualTo(0.0);
    }
}
