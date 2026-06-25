package com.painelmeteorologico.ui;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.WaypointRenderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * Renderiza cada estação como um círculo colorido (azul = frio, vermelho = quente),
 * conforme pedido no enunciado ("Marcador de estação: círculo colorido azul->vermelho por temperatura").
 */
public class EstacaoWaypointRenderer implements WaypointRenderer<EstacaoWaypoint> {

    private static final int RAIO = 8;

    private final double valorMin;
    private final double valorMax;

    public EstacaoWaypointRenderer(double valorMin, double valorMax) {
        this.valorMin = valorMin;
        this.valorMax = valorMax;
    }

    @Override
    public void paintWaypoint(Graphics2D g, JXMapViewer map, EstacaoWaypoint wp) {
        GeoPosition pos = wp.getPosition();
        Point2D pt = map.getTileFactory().geoToPixel(pos, map.getZoom());

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = (int) pt.getX();
        int y = (int) pt.getY();

        Color cor = corPorTemperatura(wp.getValor());
        g2.setColor(cor);
        g2.fill(new Ellipse2D.Double(x - RAIO, y - RAIO, RAIO * 2.0, RAIO * 2.0));

        g2.setColor(Color.BLACK);
        g2.draw(new Ellipse2D.Double(x - RAIO, y - RAIO, RAIO * 2.0, RAIO * 2.0));

        g2.dispose();
    }

    /** Interpola azul (frio) -> vermelho (quente) conforme min/max do período filtrado. */
    private Color corPorTemperatura(double valor) {
        double t = valorMax - valorMin == 0 ? 0.5 : (valor - valorMin) / (valorMax - valorMin);
        t = Math.max(0, Math.min(1, t));
        int r = (int) (255 * t);
        int b = (int) (255 * (1 - t));
        return new Color(r, 60, b);
    }

    /** Converte coordenada de tela de volta para detectar clique sobre um waypoint (usado no popup). */
    public boolean contemPonto(JXMapViewer map, EstacaoWaypoint wp, Point clique) {
        Point2D pt = map.getTileFactory().geoToPixel(wp.getPosition(), map.getZoom());
        Point2D pontoNoMapa = new Point2D.Double(
                pt.getX() - map.getViewportBounds().getX(),
                pt.getY() - map.getViewportBounds().getY()
        );
        double dx = pontoNoMapa.getX() - clique.getX();
        double dy = pontoNoMapa.getY() - clique.getY();
        return Math.sqrt(dx * dx + dy * dy) <= RAIO + 2;
    }
}
