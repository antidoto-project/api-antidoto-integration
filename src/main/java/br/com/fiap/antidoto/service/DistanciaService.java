package br.com.fiap.antidoto.service;

import org.springframework.stereotype.Service;

/** Distância em linha reta entre dois pontos (Haversine), como no Afetto. */
@Service
public class DistanciaService {

    private static final int RAIO_TERRA = 6371; // km

    public double calcularKm(double latitude1, double longitude1, double latitude2, double longitude2) {
        double dLat = Math.toRadians(latitude2 - latitude1);
        double dLon = Math.toRadians(longitude2 - longitude1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(latitude1))
                * Math.cos(Math.toRadians(latitude2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RAIO_TERRA * c;
    }

    /** Uma casa decimal, como o contrato pede no distanciaKm (ex.: 2.4). */
    public double arredondar(double km) {
        return Math.round(km * 10) / 10.0;
    }
}
