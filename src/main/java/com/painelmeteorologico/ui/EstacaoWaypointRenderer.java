package com.painelmeteorologico.ui;

import com.painelmeteorologico.service.ChuvaTendenciaService;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.WaypointRenderer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * Renderiza cada estação como um círculo colorido (azul = frio, vermelho = quente,
 * conforme a variável climática selecionada), com:
 * - seta de tendência de chuva (↑ crescente / ↓ decrescente) acima do marcador;
 * - halo externo laranja (onda de calor) ou roxo (onda de frio) quando aplicável.
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

        desenharHaloOnda(g2, wp, x, y);

        Color cor = corPorValor(wp.getValor());
        g2.setColor(cor);
        g2.fill(new Ellipse2D.Double(x - RAIO, y - RAIO, RAIO * 2.0, RAIO * 2.0));

        g2.setColor(Color.BLACK);
        g2.draw(new Ellipse2D.Double(x - RAIO, y - RAIO, RAIO * 2.0, RAIO * 2.0));

        desenharSetaTendencia(g2, wp.getTendencia(), x, y);

        g2.dispose();
    }

    /** Halo externo: laranja = onda de calor em andamento; roxo = onda de frio em andamento. */
    private void desenharHaloOnda(Graphics2D g2, EstacaoWaypoint wp, int x, int y) {
        if (!wp.isOndaDeCalor() && !wp.isOndaDeFrio()) return;

        int raioHalo = RAIO + 6;
        Stroke strokeOriginal = g2.getStroke();
        g2.setStroke(new BasicStroke(2.5f));
        g2.setColor(wp.isOndaDeCalor() ? new Color(255, 100, 0) : new Color(120, 60, 200));
        g2.draw(new Ellipse2D.Double(x - raioHalo, y - raioHalo, raioHalo * 2.0, raioHalo * 2.0));
        g2.setStroke(strokeOriginal);
    }

    /** Seta de tendência de chuva: aponta para cima (crescente) ou para baixo (decrescente). */
    private void desenharSetaTendencia(Graphics2D g2, ChuvaTendenciaService.Tendencia tendencia, int x, int y) {
        if (tendencia == null || tendencia == ChuvaTendenciaService.Tendencia.ESTAVEL) return;

        int tam = 7;
        int yBase = y - RAIO - 3;
        Polygon seta = new Polygon();

        if (tendencia == ChuvaTendenciaService.Tendencia.CRESCENTE) {
            g2.setColor(new Color(0, 90, 200));
            seta.addPoint(x, yBase - tam);
            seta.addPoint(x - tam / 2, yBase);
            seta.addPoint(x + tam / 2, yBase);
        } else { // DECRESCENTE
            g2.setColor(new Color(180, 130, 0));
            seta.addPoint(x, yBase);
            seta.addPoint(x - tam / 2, yBase - tam);
            seta.addPoint(x + tam / 2, yBase - tam);
        }
        g2.fillPolygon(seta);
    }

    /** Interpola azul (valor baixo) -> vermelho (valor alto) conforme min/max do período filtrado. */
    private Color corPorValor(double valor) {
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