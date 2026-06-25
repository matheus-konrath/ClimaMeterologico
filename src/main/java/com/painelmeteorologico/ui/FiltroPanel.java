package com.painelmeteorologico.ui;

import com.painelmeteorologico.model.Estacao;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Painel esquerdo: filtros de data (slider), estação, região, variável,
 * limiar de alerta, modo de visualização e janela de tendência —
 * exatamente os filtros listados no enunciado.
 */
public class FiltroPanel extends JPanel {

    private final JSlider sliderData = new JSlider();
    private final JLabel labelDataSelecionada = new JLabel();
    private final JComboBox<String> comboEstacao = new JComboBox<>();
    private final JComboBox<String> comboQualidade = new JComboBox<>(new String[]{"Todas", "Pass", "Fail", "No Data"});
    private final JComboBox<String> comboVariavel = new JComboBox<>(new String[]{"Temperatura", "Precipitação", "Umidade"});
    private final JComboBox<String> comboModoVisualizacao = new JComboBox<>(new String[]{"Marcadores", "Heatmap", "Zonas de alerta"});
    private final JComboBox<Integer> comboJanelaTendencia = new JComboBox<>(new Integer[]{7, 15, 30});
    private final JSpinner spinnerLimiarMm = new JSpinner(new SpinnerNumberModel(50, 0, 500, 5));

    private LocalDate dataInicio = LocalDate.of(2025, 1, 1);
    private LocalDate dataFim = LocalDate.of(2026, 5, 31);

    public FiltroPanel() {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Filtros"));
        setPreferredSize(new Dimension(260, 0));

        adicionarComLabel("Estação", comboEstacao);
        adicionarComLabel("Qualidade do dado (qc_status_label)", comboQualidade);
        adicionarComLabel("Variável climática", comboVariavel);
        adicionarComLabel("Modo de visualização", comboModoVisualizacao);
        adicionarComLabel("Janela de tendência (dias)", comboJanelaTendencia);
        adicionarComLabel("Limiar de alerta (mm)", spinnerLimiarMm);

        configurarSliderData();

        add(Box.createVerticalGlue());
    }

    private void configurarSliderData() {
        JPanel painelData = new JPanel(new BorderLayout());
        painelData.setBorder(BorderFactory.createTitledBorder("Período (jSlider)"));

        long totalDias = java.time.temporal.ChronoUnit.DAYS.between(dataInicio, dataFim);
        sliderData.setMinimum(0);
        sliderData.setMaximum((int) totalDias);
        sliderData.setValue((int) totalDias); // começa na data mais recente
        sliderData.addChangeListener(e -> atualizarLabelData());

        atualizarLabelData();

        painelData.add(sliderData, BorderLayout.CENTER);
        painelData.add(labelDataSelecionada, BorderLayout.SOUTH);
        add(painelData);
    }

    private void atualizarLabelData() {
        LocalDate selecionada = getDataSelecionada();
        labelDataSelecionada.setText("Data: " + selecionada);
    }

    public LocalDate getDataSelecionada() {
        return dataInicio.plusDays(sliderData.getValue());
    }

    public void definirPeriodo(LocalDate inicio, LocalDate fim) {
        this.dataInicio = inicio;
        this.dataFim = fim;
        long totalDias = java.time.temporal.ChronoUnit.DAYS.between(inicio, fim);
        sliderData.setMaximum((int) Math.max(totalDias, 1));
        sliderData.setValue(0);
    }

    public void preencherEstacoes(List<Estacao> estacoes) {
        comboEstacao.removeAllItems();
        comboEstacao.addItem("Todas");
        for (Estacao e : estacoes) comboEstacao.addItem(e.getNome());
    }

    private void adicionarComLabel(String texto, JComponent componente) {
        JPanel linha = new JPanel(new BorderLayout(4, 4));
        linha.add(new JLabel(texto), BorderLayout.NORTH);
        linha.add(componente, BorderLayout.CENTER);
        linha.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        add(linha);
        add(Box.createVerticalStrut(8));
    }

    public JSlider getSliderData() { return sliderData; }
    public JComboBox<String> getComboEstacao() { return comboEstacao; }
    public JComboBox<String> getComboQualidade() { return comboQualidade; }
    public JComboBox<String> getComboVariavel() { return comboVariavel; }
    public JComboBox<String> getComboModoVisualizacao() { return comboModoVisualizacao; }
    public JComboBox<Integer> getComboJanelaTendencia() { return comboJanelaTendencia; }
    public JSpinner getSpinnerLimiarMm() { return spinnerLimiarMm; }
}
