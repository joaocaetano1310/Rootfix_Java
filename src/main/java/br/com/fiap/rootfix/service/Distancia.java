package br.com.fiap.rootfix.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Distancia {
    private static final double RAIO_DA_TERRA_KM = 6371.0;
    private static final double KM_POR_GRAU_DE_LATITUDE = 111.32;

    private Distancia() {
    }

    public static double km(BigDecimal latitude1, BigDecimal longitude1, BigDecimal latitude2, BigDecimal longitude2) {
        double fi1 = Math.toRadians(latitude1.doubleValue());
        double fi2 = Math.toRadians(latitude2.doubleValue());
        double deltaFi = fi2 - fi1;
        double deltaLambda = Math.toRadians(longitude2.doubleValue() - longitude1.doubleValue());

        double a = Math.sin(deltaFi / 2) * Math.sin(deltaFi / 2)
                + Math.cos(fi1) * Math.cos(fi2) * Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
        return 2 * RAIO_DA_TERRA_KM * Math.asin(Math.sqrt(a));
    }

    public static BigDecimal grausDeLatitude(double km) {
        return BigDecimal.valueOf(km / KM_POR_GRAU_DE_LATITUDE);
    }

    public static BigDecimal grausDeLongitude(double km, BigDecimal latitude) {
        double cosseno = Math.max(Math.cos(Math.toRadians(latitude.doubleValue())), 0.01);
        return BigDecimal.valueOf(km / (KM_POR_GRAU_DE_LATITUDE * cosseno));
    }

    public static BigDecimal coordenada(BigDecimal valor) {
        return valor == null ? null : valor.setScale(7, RoundingMode.HALF_UP);
    }

    public static double arredondar(double km) {
        return Math.round(km * 100) / 100.0;
    }
}
