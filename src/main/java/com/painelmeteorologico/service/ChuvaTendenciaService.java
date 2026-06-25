package com.painelmeteorologico.service;

import com.painelmeteorologico.model.Medicao;

import java.util.List;

public class ChuvaTendenciaService {

    public enum Tendencia { CRESCENTE, DECRESCENTE, ESTAVEL }

    /**
     * Regressão linear simples (mínimos quadrados) sobre a precipitação acumulada
     * dos últimos N dias. x = índice do dia (0..n-1), y = precipitação acumulada até o dia x.
     * Retorna a inclinação (slope) e a tendência correspondente, usada para desenhar a seta no marcador.
     */
    public double calcularSlope(List<Medicao> ultimosNDias) {
        int n = ultimosNDias.size();
        if (n < 2) return 0.0;

        double acumulado = 0;
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            acumulado += ultimosNDias.get(i).getPrecipitacaoMm();
            y[i] = acumulado;
        }

        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        for (int x = 0; x < n; x++) {
            sumX += x;
            sumY += y[x];
            sumXY += x * y[x];
            sumXX += x * (double) x;
        }

        double denom = (n * sumXX - sumX * sumX);
        if (denom == 0) return 0.0;

        return (n * sumXY - sumX * sumY) / denom;
    }

    public Tendencia classificar(double slope) {
        double limiarEstabilidade = 0.05; // ajuste fino conforme sensibilidade desejada
        if (slope > limiarEstabilidade) return Tendencia.CRESCENTE;
        if (slope < -limiarEstabilidade) return Tendencia.DECRESCENTE;
        return Tendencia.ESTAVEL;
    }
}
