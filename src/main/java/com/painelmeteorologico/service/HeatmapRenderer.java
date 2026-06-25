package com.painelmeteorologico.service;

import java.awt.Color;
import java.util.List;

public class HeatmapRenderer {

    public record PontoValor(double lat, double lon, double valor) {}

    /** Célula da grade já com o valor interpolado, pronta para ser pintada pelo AbstractPainter. */
    public record Celula(double lat, double lon, double valor) {}

    /**
     * Gera uma grade lat/lon cobrindo o bounding box dos pontos, com `resolucao` células
     * por lado, e interpola o valor em cada célula por IDW (Inverse Distance Weighting).
     *
     * @param pontos     valores conhecidos (ex.: precipitação ou temperatura por estação)
     * @param resolucao  número de células por linha/coluna da grade
     * @param potencia   expoente do IDW (2 é o valor clássico)
     */
    public Celula[][] interpolar(List<PontoValor> pontos, int resolucao, double potencia) {
        if (pontos.isEmpty()) return new Celula[0][0];

        double minLat = pontos.stream().mapToDouble(PontoValor::lat).min().orElseThrow();
        double maxLat = pontos.stream().mapToDouble(PontoValor::lat).max().orElseThrow();
        double minLon = pontos.stream().mapToDouble(PontoValor::lon).min().orElseThrow();
        double maxLon = pontos.stream().mapToDouble(PontoValor::lon).max().orElseThrow();

        Celula[][] grade = new Celula[resolucao][resolucao];

        for (int i = 0; i < resolucao; i++) {
            double lat = minLat + (maxLat - minLat) * i / (resolucao - 1.0);
            for (int j = 0; j < resolucao; j++) {
                double lon = minLon + (maxLon - minLon) * j / (resolucao - 1.0);
                grade[i][j] = new Celula(lat, lon, idw(pontos, lat, lon, potencia));
            }
        }
        return grade;
    }

    private double idw(List<PontoValor> pontos, double lat, double lon, double potencia) {
        double somaPesos = 0, somaPesoValor = 0;

        for (PontoValor p : pontos) {
            double d = distancia(lat, lon, p.lat(), p.lon());
            if (d < 1e-6) return p.valor(); // ponto exatamente sobre uma estação

            double peso = 1.0 / Math.pow(d, potencia);
            somaPesos += peso;
            somaPesoValor += peso * p.valor();
        }
        return somaPesos == 0 ? 0 : somaPesoValor / somaPesos;
    }

    private double distancia(double lat1, double lon1, double lat2, double lon2) {
        double dLat = lat1 - lat2;
        double dLon = lon1 - lon2;
        return Math.sqrt(dLat * dLat + dLon * dLon);
    }

    /** Mapeia um valor normalizado (0..1) para uma cor de gradiente azul -> vermelho. */
    public Color corPorIntensidade(double valor, double min, double max) {
        double t = max - min == 0 ? 0 : (valor - min) / (max - min);
        t = Math.max(0, Math.min(1, t));
        int r = (int) (255 * t);
        int b = (int) (255 * (1 - t));
        return new Color(r, 0, b, 140); // alpha 140 para semitransparência
    }
}
