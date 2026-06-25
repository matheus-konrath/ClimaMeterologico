package com.painelmeteorologico.ui;

import com.painelmeteorologico.model.Estacao;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;

/**
 * Waypoint customizado que carrega a estação e o valor atual (ex.: temperatura média
 * do período filtrado) para que o renderer possa colorir o marcador.
 */
public class EstacaoWaypoint implements Waypoint {

    private final Estacao estacao;
    private final double valor; // valor usado para colorir (ex: temp média)
    private final GeoPosition posicao;

    public EstacaoWaypoint(Estacao estacao, double valor) {
        this.estacao = estacao;
        this.valor = valor;
        this.posicao = new GeoPosition(estacao.getLatitude(), estacao.getLongitude());
    }

    @Override
    public GeoPosition getPosition() {
        return posicao;
    }

    public Estacao getEstacao() {
        return estacao;
    }

    public double getValor() {
        return valor;
    }
}
