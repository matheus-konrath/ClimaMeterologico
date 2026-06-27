package com.painelmeteorologico.ui;

import com.painelmeteorologico.service.HeatmapRenderer;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Desenha a grade de células semitransparentes interpoladas por IDW
 * (gerada por HeatmapRenderer.interpolar), conforme pedido no enunciado:
 * "Heatmaps: Grade de células semitransparentes interpoladas (IDW) sobre as
 * estações. Gradiente orientado por intensidade."
 */
public class HeatMapPainter implements Painter<JXMapViewer> {

    private final HeatmapRenderer.Celula[][] grade;
    private final double valorMin;
    private final double valorMax;
    private final HeatmapRenderer heatmapRenderer = new HeatmapRenderer();

    public HeatMapPainter(HeatmapRenderer.Celula[][] grade, double valorMin, double valorMax) {
        this.grade = grade;
        this.valorMin = valorMin;
        this.valorMax = valorMax;
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        if (grade.length < 2) return;
        Graphics2D g2 = (Graphics2D) g.create();

        for (int i = 0; i < grade.length - 1; i++) {
            for (int j = 0; j < grade[i].length - 1; j++) {
                HeatmapRenderer.Celula celula = grade[i][j];
                HeatmapRenderer.Celula vizinha = grade[i + 1][j + 1];

                Point2D pt1 = map.getTileFactory().geoToPixel(
                        new GeoPosition(celula.lat(), celula.lon()), map.getZoom());
                Point2D pt2 = map.getTileFactory().geoToPixel(
                        new GeoPosition(vizinha.lat(), vizinha.lon()), map.getZoom());

                double x = Math.min(pt1.getX(), pt2.getX());
                double y = Math.min(pt1.getY(), pt2.getY());
                double largura = Math.max(Math.abs(pt2.getX() - pt1.getX()), 1);
                double altura = Math.max(Math.abs(pt2.getY() - pt1.getY()), 1);

                g2.setColor(heatmapRenderer.corPorIntensidade(celula.valor(), valorMin, valorMax));
                g2.fill(new Rectangle2D.Double(x, y, largura, altura));
            }
        }
        g2.dispose();
    }
}
