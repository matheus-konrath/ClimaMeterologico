package com.painelmeteorologico.ui;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * Desenha as zonas de alerta de alagamento, conforme pedido no enunciado:
 * "Zona alagadiça: Polígono semitransparente vermelho/laranja ao redor das
 * estações em alerta. Raio proporcional ao nível (1/2/3)."
 */
public class ZonaAlagamentoPainter implements Painter<JXMapViewer> {

    /** nivel: 1=Atenção, 2=Alerta, 3=Emergência (0 ou negativo = não desenha). */
    public record ZonaInfo(GeoPosition posicao, String nomeEstacao, int nivel) {}

    private final List<ZonaInfo> zonas;

    public ZonaAlagamentoPainter(List<ZonaInfo> zonas) {
        this.zonas = zonas;
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        Graphics2D g2 = (Graphics2D) g.create();

        for (ZonaInfo zona : zonas) {
            if (zona.nivel() <= 0) continue;

            Point2D pt = map.getTileFactory().geoToPixel(zona.posicao(), map.getZoom());
            int raioPixels = 10 + zona.nivel() * 12; // raio proporcional ao nível (1/2/3)

            Color cor = switch (zona.nivel()) {
                case 1 -> new Color(255, 200, 0, 90);   // Atenção
                case 2 -> new Color(255, 120, 0, 120);  // Alerta
                default -> new Color(220, 0, 0, 140);   // Emergência
            };

            Ellipse2D circulo = new Ellipse2D.Double(
                    pt.getX() - raioPixels, pt.getY() - raioPixels, raioPixels * 2.0, raioPixels * 2.0);

            g2.setColor(cor);
            g2.fill(circulo);
            g2.setColor(cor.darker());
            g2.draw(circulo);
        }
        g2.dispose();
    }
}
