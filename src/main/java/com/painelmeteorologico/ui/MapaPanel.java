package com.painelmeteorologico.ui;

import com.painelmeteorologico.service.HeatmapRenderer;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
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
import java.util.List;
import java.util.Set;

/**
 * Painel do mapa. Usa exatamente a lógica do seu código original de TileFactoryInfo
 * customizado, com https:// explícito — necessário porque tile.openstreetmap.org
 * não aceita mais http:// (que é o padrão da classe pronta OSMTileFactoryInfo da lib).
 *
 * Suporta os 3 modos de visualização do enunciado: Marcadores, Heatmap, Zonas de alerta.
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
        exibirMarcadores();

        add(mapViewer, BorderLayout.CENTER);
    }

    private void configurarTileFactory() {
        int max = 17;
        String url = "https://tile.openstreetmap.org/";
        TileFactoryInfo info = new TileFactoryInfo(1, max - 2, max, 256, true, true, url, "z", "x", "y") {
            @Override
            public String getTileUrl(int x, int y, int zoom) {
                int z = max - zoom;
                return url + z + "/" + x + "/" + y + ".png";
            }
        };
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

    /** Modo "Marcadores": círculos coloridos por temperatura + seta de tendência. */
    public void exibirMarcadores(Set<EstacaoWaypoint> novos, double min, double max) {
        this.waypoints.clear();
        this.waypoints.addAll(novos);
        this.renderer = new EstacaoWaypointRenderer(min, max);
        exibirMarcadores();
    }

    private void exibirMarcadores() {
        WaypointPainter<EstacaoWaypoint> waypointPainter = new WaypointPainter<>();
        waypointPainter.setWaypoints(waypoints);
        waypointPainter.setRenderer(renderer);

        CompoundPainter<JXMapViewer> compound = new CompoundPainter<>();
        compound.setPainters(waypointPainter);
        mapViewer.setOverlayPainter(compound);
    }

    /** Modo "Heatmap": grade IDW semitransparente (sem marcadores). */
    public void exibirHeatmap(HeatmapRenderer.Celula[][] grade, double valorMin, double valorMax) {
        CompoundPainter<JXMapViewer> compound = new CompoundPainter<>();
        compound.setPainters(new HeatMapPainter(grade, valorMin, valorMax));
        mapViewer.setOverlayPainter(compound);
    }

    /** Modo "Zonas de alerta": polígonos de zona alagadiça (sem marcadores). */
    public void exibirZonasAlerta(List<ZonaAlagamentoPainter.ZonaInfo> zonas) {
        CompoundPainter<JXMapViewer> compound = new CompoundPainter<>();
        compound.setPainters(new ZonaAlagamentoPainter(zonas));
        mapViewer.setOverlayPainter(compound);
    }

    private void exibirPopupEstacao(EstacaoWaypoint wp, Point onde) {
        JPopupMenu popup = new JPopupMenu();
        popup.add("Estação: " + wp.getEstacao().getNome());
        popup.add(String.format("Valor (variável selecionada): %.1f", wp.getValor()));
        if (wp.getTendencia() != null) {
            popup.add("Tendência de chuva: " + wp.getTendencia());
        }
        if (wp.isOndaDeCalor()) {
            popup.add("⚠ Onda de calor em andamento");
        }
        if (wp.isOndaDeFrio()) {
            popup.add("⚠ Onda de frio em andamento");
        }
        popup.addSeparator();
        popup.add("(sparkline de 7 dias - plugar gráfico aqui)");
        popup.show(mapViewer, (int) onde.getX(), (int) onde.getY());
    }

    public JXMapViewer getMapViewer() {
        return mapViewer;
    }
}