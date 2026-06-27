package com.painelmeteorologico.ui;

import com.painelmeteorologico.model.Estacao;
import com.painelmeteorologico.service.ChuvaTendenciaService;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;

/**
 * Waypoint customizado que carrega a estação, o valor atual (conforme a variável
 * climática selecionada: temperatura, precipitação ou umidade), a tendência de
 * chuva e os sinalizadores de onda de calor/frio, para o renderer desenhar tudo.
 */
public class EstacaoWaypoint implements Waypoint {

    private final Estacao estacao;
    private final double valor;
    private final GeoPosition posicao;
    private final ChuvaTendenciaService.Tendencia tendencia; // pode ser null
    private final boolean ondaDeCalor;
    private final boolean ondaDeFrio;

    public EstacaoWaypoint(Estacao estacao, double valor) {
        this(estacao, valor, null, false, false);
    }

    public EstacaoWaypoint(Estacao estacao, double valor, ChuvaTendenciaService.Tendencia tendencia) {
        this(estacao, valor, tendencia, false, false);
    }

    public EstacaoWaypoint(Estacao estacao, double valor, ChuvaTendenciaService.Tendencia tendencia,
                           boolean ondaDeCalor, boolean ondaDeFrio) {
        this.estacao = estacao;
        this.valor = valor;
        this.tendencia = tendencia;
        this.ondaDeCalor = ondaDeCalor;
        this.ondaDeFrio = ondaDeFrio;
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

    public ChuvaTendenciaService.Tendencia getTendencia() {
        return tendencia;
    }

    public boolean isOndaDeCalor() {
        return ondaDeCalor;
    }

    public boolean isOndaDeFrio() {
        return ondaDeFrio;
    }
}