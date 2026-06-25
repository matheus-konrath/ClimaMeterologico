package com.painelmeteorologico.ui;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.WaypointPainter;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * Painel do mapa. Baseado no seu código original de TileFactoryInfo customizado
 * (mantido por compatibilidade), mas você também pode trocar por
 * `new OSMTileFactoryInfo()` do próprio jxmapviewer2, que já faz a mesma coisa.
 */
public class MapaPanel extends JPanel {

    private final JXMapViewer mapViewer = new JXMapViewer();
    private final Set<EstacaoWaypoint> waypoints = new HashSet<>();
    private EstacaoWaypointRenderer renderer = new EstacaoWaypointRenderer(0, 40);

    public MapaPanel() {
        super(new BorderLayout());
        System.setProperty("http.agent", "PainelMeteorologico/1.0");

        configurarTileFactory();

        GeoPosition centroRS = new GeoPosition(-30.0346, -51.2177); // Porto Alegre
        mapViewer.setZoom(5);
        mapViewer.setAddressLocation(centroRS);

        configurarInteratividade();
        configurarCamadaDeWaypoints();

        add(mapViewer, BorderLayout.CENTER);
    }

    private void configurarTileFactory() {
        // Opção simples (recomendada): usar a factory pronta do jxmapviewer2.
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        tileFactory.setThreadPoolSize(8);
        mapViewer.setTileFactory(tileFactory);
    }

    /** Pan com arraste do mouse + zoom com a roda do mouse (mapa interativo). */
    private void configurarInteratividade() {
        org.jxmapviewer.input.PanMouseInputListener panListener =
                new org.jxmapviewer.input.PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(panListener);
        mapViewer.addMouseMotionListener(panListener);

        mapViewer.addMouseListener(new org.jxmapviewer.input.CenterMapListener(mapViewer));
        mapViewer.addMouseWheelListener(new org.jxmapviewer.input.ZoomMouseWheelListenerCursor(mapViewer));
        mapViewer.addKeyListener(new org.jxmapviewer.input.PanKeyListener(mapViewer));

        // Clique sobre uma estação -> popup com min/max/média e sparkline
        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) return;
                Point clique = e.getPoint();
                for (EstacaoWaypoint wp : waypoints) {
                    if (renderer.contemPonto(mapViewer, wp, clique)) {
                        exibirPopupEstacao(wp, e.getPoint());
                        break;
                    }
                }
            }
        });
    }

    private void configurarCamadaDeWaypoints() {
        WaypointPainter<EstacaoWaypoint> waypointPainter = new WaypointPainter<>();
        waypointPainter.setWaypoints(waypoints);
        waypointPainter.setRenderer(renderer);

        CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter<>();
        compoundPainter.setPainters(waypointPainter);
        mapViewer.setOverlayPainter(compoundPainter);
    }

    /** Substitui as camadas extras (heatmap, zonas de alerta, isócronas) por outras Painters aqui. */
    public void definirPaintersExtras(Painter<JXMapViewer>... painters) {
        CompoundPainter<JXMapViewer> compound = new CompoundPainter<>();
        java.util.List<Painter<JXMapViewer>> lista = new java.util.ArrayList<>();
        WaypointPainter<EstacaoWaypoint> waypointPainter = new WaypointPainter<>();
        waypointPainter.setWaypoints(waypoints);
        waypointPainter.setRenderer(renderer);
        lista.add(waypointPainter);
        for (Painter<JXMapViewer> p : painters) lista.add(p);
        compound.setPainters(lista);
        mapViewer.setOverlayPainter(compound);
    }

    public void atualizarWaypoints(Set<EstacaoWaypoint> novos, double min, double max) {
        waypoints.clear();
        waypoints.addAll(novos);
        renderer = new EstacaoWaypointRenderer(min, max);
        configurarCamadaDeWaypoints();
        mapViewer.repaint();
    }

    private void exibirPopupEstacao(EstacaoWaypoint wp, Point onde) {
        JPopupMenu popup = new JPopupMenu();
        popup.add("Estação: " + wp.getEstacao().getNome());
        popup.add(String.format("Valor (período filtrado): %.1f°C", wp.getValor()));
        popup.addSeparator();
        popup.add("(sparkline de 7 dias - plugar gráfico aqui)");
        popup.show(mapViewer, (int) onde.getX(), (int) onde.getY());
    }

    public JXMapViewer getMapViewer() {
        return mapViewer;
    }
}
