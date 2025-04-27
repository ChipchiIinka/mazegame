package edu.penzgtu;

import edu.penzgtu.evolution.*;
import edu.penzgtu.model.Obstacle;
import edu.penzgtu.util.MazeGenerator;
import edu.penzgtu.view.MazePanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class MazeGame {
    private static final int POP_SIZE = 50;

    private JFrame frame;
    private MazeGenerator generator;
    private MazePanel panel;
    private EvolutionaryAlgorithm ea;
    private Strategy bestStrategy;
    private int generation;
    private char[][] currentMaze;
    private List<Obstacle> currentObstacles;
    private Timer uiTimer;
    private boolean isFirstRun = true;

    private JComboBox<String> algoBox;
    private JLabel genLabel;
    private JLabel fitnessLabel;
    private JLabel stepsLabel;
    private JLabel successLabel;
    private JPanel chartPanelContainer;
    private final List<Double> fitnessHistory = new ArrayList<>();
    private final List<Integer> successHistory = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MazeGame().createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("Игра-лабиринт с эволюцией");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        generator = new MazeGenerator();
        currentMaze = generator.generateValidMaze();
        currentObstacles = generator.getObstacles();
        panel = new MazePanel(currentMaze);
        panel.setObstacles(currentObstacles);
        frame.add(panel, BorderLayout.CENTER);

        chartPanelContainer = new JPanel();
        chartPanelContainer.setLayout(new BorderLayout());
        frame.add(chartPanelContainer, BorderLayout.EAST);

        JPanel control = new JPanel();
        algoBox = new JComboBox<>(new String[]{"Генетический алгоритм", "Эволюционная стратегия", "Метод отжига"});
        JButton initButton = new JButton("Инициализировать");
        JButton nextGenButton = new JButton("Следующее поколение");
        JButton newMazeButton = new JButton("Новый лабиринт");
        JButton stepButton = new JButton("Шаг");
        JButton runPauseButton = new JButton("Запуск/Пауза");
        JButton instantRunButton = new JButton("Мгновенное прохождение");
        control.add(new JLabel("Алгоритм:"));
        control.add(algoBox);
        control.add(initButton);
        control.add(nextGenButton);
        control.add(newMazeButton);
        control.add(stepButton);
        control.add(runPauseButton);
        control.add(instantRunButton);
        frame.add(control, BorderLayout.NORTH);

        JPanel status = new JPanel();
        genLabel = new JLabel("Поколение: 0");
        fitnessLabel = new JLabel("Фитнес: N/A");
        stepsLabel = new JLabel("Шагов: 0");
        successLabel = new JLabel("Статус: Ожидание");
        status.add(genLabel);
        status.add(fitnessLabel);
        status.add(stepsLabel);
        status.add(successLabel);
        frame.add(status, BorderLayout.SOUTH);

        initButton.addActionListener(this::onInitialize);
        nextGenButton.addActionListener(this::onNextGeneration);
        newMazeButton.addActionListener(e -> onNewMaze());
        stepButton.addActionListener(e -> onStep());
        runPauseButton.addActionListener(e -> onRunPause());
        instantRunButton.addActionListener(e -> onInstantRun());

        onNewMaze();

        frame.pack();
        frame.setVisible(true);
    }

    private void onInitialize(ActionEvent e) {
        switch (algoBox.getSelectedIndex()) {
            case 0: ea = new GeneticAlgorithm(); break;
            case 1: ea = new EvolutionStrategy(); break;
            default: ea = new SimulatedAnnealing();
        }
        ea.initialize(POP_SIZE);
        generation = 0;
        bestStrategy = null;
        genLabel.setText("Поколение: 0");
        fitnessLabel.setText("Фитнес: N/A");
        stepsLabel.setText("Шагов: 0");
        successLabel.setText("Статус: Ожидание");
        panel.resetSimulation();
        resetChart();
    }

    private void resetChart() {
        fitnessHistory.clear();
        successHistory.clear();
        updateChart();
    }

    private void onNextGeneration(ActionEvent e) {
        if (ea == null) {
            JOptionPane.showMessageDialog(frame, "Сначала инициализируйте алгоритм");
            return;
        }
        stopUiTimer();
        ea.evaluate(generator);
        bestStrategy = ea.getBest().getStrategy();
        double fitness = ea.getBest().getFitness();

        panel.setMaze(currentMaze);
        panel.setObstacles(currentObstacles);
        panel.resetSimulation();
        stepsLabel.setText("Шагов: 0");

        fitnessHistory.add(fitness);
        successHistory.add(0);

        fitnessLabel.setText(String.format("Фитнес: %.2f", fitness));
        updateChart();

        ea.evolve();
        generation++;
        genLabel.setText("Поколение: " + generation);
        successLabel.setText("Статус: Ожидание");

        startUiTimer();
    }

    private void onNewMaze() {
        stopUiTimer();
        currentMaze = generator.generateValidMaze();
        currentObstacles = generator.getObstacles();
        panel.setMaze(currentMaze);
        panel.setObstacles(currentObstacles);
        panel.resetSimulation();
        stepsLabel.setText("Шагов: 0");
        successLabel.setText("Статус: Ожидание");
        fitnessHistory.clear();
        successHistory.clear();
        updateChart();
    }

    private void onStep() {
        if (bestStrategy == null) {
            JOptionPane.showMessageDialog(frame, "Сначала выполните следующее поколение");
            return;
        }
        if (panel.isDead() || panel.isGoalReached()) {
            JOptionPane.showMessageDialog(frame, panel.isDead() ? "Агент погиб на красном препятствии" : "Цель достигнута");
            return;
        }
        panel.stepStrategy(bestStrategy);
        stepsLabel.setText("Шагов: " + panel.getSteps());
        checkSuccess();
        updateSuccessHistory();
    }

    private void onRunPause() {
        if (bestStrategy == null) {
            JOptionPane.showMessageDialog(frame, "Сначала выполните следующее поколение");
            return;
        }
        if (panel.isDead() || panel.isGoalReached()) {
            return;
        }
        if (uiTimer != null && uiTimer.isRunning()) {
            stopUiTimer();
        } else {
            startUiTimer();
        }
    }

    private void onInstantRun() {
        if (bestStrategy == null) {
            JOptionPane.showMessageDialog(frame, "Сначала выполните следующее поколение");
            return;
        }
        if (panel.isDead() || panel.isGoalReached()) {
            JOptionPane.showMessageDialog(frame, panel.isDead() ? "Агент погиб на красном препятствии" : "Цель достигнута");
            return;
        }
        panel.simulateStrategy(bestStrategy);
        stepsLabel.setText("Шагов: " + panel.getSteps());
        checkSuccess();
        updateSuccessHistory();
    }

    private void startUiTimer() {
        if (uiTimer != null) uiTimer.stop();
        uiTimer = new Timer(100, ev -> {
            if (panel.isDead() || panel.isGoalReached()) {
                stopUiTimer();
            } else {
                panel.stepStrategy(bestStrategy);
                stepsLabel.setText("Шагов: " + panel.getSteps());
            }
            checkSuccess();
            updateSuccessHistory();
        });
        uiTimer.start();
    }

    private void stopUiTimer() {
        if (uiTimer != null) uiTimer.stop();
    }

    private void checkSuccess() {
        if (panel.isGoalReached()) {
            successLabel.setText("Статус: Успех!");
            stopUiTimer();
        } else if (panel.isDead()) {
            successLabel.setText("Статус: Неудача (агент погиб)");
            stopUiTimer();
        } else {
            successLabel.setText("Статус: Ожидание");
        }
    }

    private void updateSuccessHistory() {
        if (!successHistory.isEmpty()) {
            successHistory.set(successHistory.size() - 1, panel.isGoalReached() ? 1 : 0);
            updateChart();
        }
    }

    private void updateChart() {
        XYSeriesCollection dataset = getSeriesCollection();

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Зависимость побед от фитнеса",
                "Поколение",
                "Фитнес",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        chart.getXYPlot().getRenderer().setSeriesPaint(0, Color.BLUE);
        chart.getXYPlot().getRenderer().setSeriesPaint(1, Color.RED);

        if (chartPanelContainer.getComponentCount() > 0) {
            chartPanelContainer.removeAll();
        }

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 300));
        chartPanelContainer.add(chartPanel, BorderLayout.CENTER);
        chartPanelContainer.revalidate();
        chartPanelContainer.repaint();

        if (isFirstRun) {
            frame.setSize(frame.getWidth() + 250, frame.getHeight());
            isFirstRun = false;
        }
    }

    private XYSeriesCollection getSeriesCollection() {
        XYSeries fitnessSuccessSeries = new XYSeries("Фитнес - Победа");
        XYSeries fitnessFailureSeries = new XYSeries("Фитнес - Поражение");

        for (int i = 0; i < fitnessHistory.size(); i++) {
            if (successHistory.get(i) == 1) {
                fitnessSuccessSeries.add(i, fitnessHistory.get(i));
            } else {
                fitnessFailureSeries.add(i, fitnessHistory.get(i));
            }
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(fitnessSuccessSeries);
        dataset.addSeries(fitnessFailureSeries);
        return dataset;
    }
}