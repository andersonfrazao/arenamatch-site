package br.com.arenamatch.service;

import org.springframework.stereotype.Service;

@Service
public class DistanciaService {

    // Raio da Terra em KM
    private static final int RAIO_TERRA = 6371;

    /**
     * Calcula a distância em KM entre dois pontos (Latitude/Longitude)
     */
    public double calcularDistancia(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return 9999.0; // Retorna longe se não tiver dados
        }

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Retorna distância em KM com 1 casa decimal de precisão
        double dist = RAIO_TERRA * c;
        return Math.round(dist * 10.0) / 10.0;
    }
}