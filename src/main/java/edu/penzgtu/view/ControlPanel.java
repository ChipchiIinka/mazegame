package edu.penzgtu.view;

import javax.swing.*;

public class ControlPanel extends JPanel {
    private final JLabel stepLabel;
    private final JLabel goalLabel;
    private final JLabel generationLabel;
    private final JTextArea leaderboardArea;

    public ControlPanel(Runnable stepAction, Runnable toggleAction) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel buttonPanel = new JPanel();
        JButton stepButton = new JButton("Сделать шаг");
        JButton runButton = new JButton("Запустить/Остановить");
        stepLabel = new JLabel("Пройденных шагов: 0");
        goalLabel = new JLabel("Процент побед: 0.0%");
        generationLabel = new JLabel("Поколение: 1");

        stepButton.addActionListener(e -> stepAction.run());
        runButton.addActionListener(e -> toggleAction.run());

        buttonPanel.add(stepButton);
        buttonPanel.add(runButton);
        buttonPanel.add(stepLabel);
        buttonPanel.add(goalLabel);
        buttonPanel.add(generationLabel);

        leaderboardArea = new JTextArea(5, 30);
        leaderboardArea.setEditable(false);
        leaderboardArea.setText("Таблица лидеров:\nПока нет результатов");
        JScrollPane leaderboardScroll = new JScrollPane(leaderboardArea);

        add(buttonPanel);
        add(leaderboardScroll);
    }

    public JLabel getStepLabel() {
        return stepLabel;
    }

    public JLabel getGoalLabel() {
        return goalLabel;
    }

    public JLabel getGenerationLabel() {
        return generationLabel;
    }

    public JTextArea getLeaderboardArea() {
        return leaderboardArea;
    }
}