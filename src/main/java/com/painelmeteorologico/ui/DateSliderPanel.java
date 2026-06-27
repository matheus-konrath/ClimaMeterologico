package com.painelmeteorologico.ui;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Slider de período posicionado sob o mapa, igual ao mockup do enunciado
 * ("Filtros: por data... jSlider 01/01/2026 -------- 01/05/2026").
 * Arraste para voltar/avançar no tempo e ver a leitura de dias anteriores.
 */
public class DateSliderPanel extends JPanel {

    private final JSlider slider = new JSlider();
    private final JLabel labelInicio = new JLabel();
    private final JLabel labelFim = new JLabel();
    private final JLabel labelAtual = new JLabel();

    private LocalDate dataInicio;
    private LocalDate dataFim;

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public DateSliderPanel(LocalDate dataInicio, LocalDate dataFim) {
        super(new BorderLayout(8, 2));
        setBorder(BorderFactory.createEmptyBorder(4, 12, 8, 12));

        labelAtual.setFont(labelAtual.getFont().deriveFont(Font.BOLD, 13f));
        labelAtual.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel extremos = new JPanel(new BorderLayout());
        extremos.add(labelInicio, BorderLayout.WEST);
        extremos.add(labelFim, BorderLayout.EAST);

        JPanel atalhos = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        atalhos.add(criarBotaoAtalho("-7 dias", -7));
        atalhos.add(criarBotaoAtalho("-3 dias", -3));
        atalhos.add(criarBotaoAtalho("-1 dia", -1));
        atalhos.add(criarBotaoAtalho("+1 dia", 1));
        atalhos.add(criarBotaoAtalho("+3 dias", 3));
        atalhos.add(criarBotaoAtalho("Mais recente", Integer.MIN_VALUE));

        JPanel topo = new JPanel(new BorderLayout());
        topo.add(labelAtual, BorderLayout.NORTH);
        topo.add(atalhos, BorderLayout.SOUTH);

        add(topo, BorderLayout.NORTH);
        add(slider, BorderLayout.CENTER);
        add(extremos, BorderLayout.SOUTH);

        definirPeriodo(dataInicio, dataFim);
        slider.addChangeListener(e -> atualizarLabel());
    }

    private JButton criarBotaoAtalho(String texto, int deltaDias) {
        JButton botao = new JButton(texto);
        botao.setMargin(new Insets(2, 6, 2, 6));
        botao.addActionListener(e -> {
            if (deltaDias == Integer.MIN_VALUE) {
                slider.setValue(slider.getMaximum()); // "Mais recente"
            } else {
                slider.setValue(Math.max(slider.getMinimum(),
                        Math.min(slider.getMaximum(), slider.getValue() + deltaDias)));
            }
        });
        return botao;
    }

    public void definirPeriodo(LocalDate inicio, LocalDate fim) {
        this.dataInicio = inicio;
        this.dataFim = fim;
        long totalDias = ChronoUnit.DAYS.between(inicio, fim);
        slider.setMinimum(0);
        slider.setMaximum((int) Math.max(totalDias, 1));
        slider.setValue((int) totalDias); // começa na data mais recente
        labelInicio.setText(inicio.format(FORMATO));
        labelFim.setText(fim.format(FORMATO));
        atualizarLabel();
    }

    private void atualizarLabel() {
        labelAtual.setText("Data selecionada: " + getDataSelecionada().format(FORMATO)
                + "  (arraste o slider ou use os botões para ver dias anteriores)");
    }

    public LocalDate getDataSelecionada() {
        return dataInicio.plusDays(slider.getValue());
    }

    public void addChangeListener(ChangeListener listener) {
        slider.addChangeListener(listener);
    }

    public JSlider getSlider() {
        return slider;
    }
}