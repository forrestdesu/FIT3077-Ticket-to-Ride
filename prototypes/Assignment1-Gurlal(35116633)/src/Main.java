package src;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().showMainMenu());
    }

    public void showMainMenu() {
        JFrame frame = createFrame("Main Menu");

        JButton playButton = new JButton("Play");
        // playButton.setFont(new Font("Arial", Font.BOLD, 20));

        playButton.addActionListener(e -> {
            frame.dispose();
            showPlayerCountScreen();
        });

        frame.setLayout(new GridBagLayout());
        frame.add(playButton);
        frame.setVisible(true);
    }

    private void showPlayerCountScreen() {
        JFrame frame = createFrame("Select Players");

        JSlider slider = new JSlider(2, 5, 2);
        slider.setMajorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        JButton nextBtn = new JButton("Next");

        nextBtn.addActionListener(e -> {
            frame.dispose();
            showNameInputScreen(slider.getValue());
        });

        frame.setLayout(new BorderLayout());
        frame.add(new JLabel("Select number of players", SwingConstants.CENTER), BorderLayout.NORTH);
        frame.add(slider, BorderLayout.CENTER);
        frame.add(nextBtn, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void showNameInputScreen(int count) {
        JFrame frame = createFrame("Enter Names");

        JPanel panel = new JPanel(new BorderLayout());
        JPanel fieldsPanel = new JPanel(new GridLayout(count, 1));

        JTextField[] fields = new JTextField[count];

        for (int i = 0; i < count; i++) {
            fields[i] = new JTextField("Player " + (i + 1));
            fieldsPanel.add(fields[i]);
        }

        JPanel buttons = new JPanel();
        JButton backBtn = new JButton("Back");
        JButton startBtn = new JButton("Start");

        backBtn.addActionListener(e -> {
            frame.dispose();
            showPlayerCountScreen();
        });

        startBtn.addActionListener(e -> {
            String[] names = new String[count];
            for (int i = 0; i < count; i++) {
                names[i] = fields[i].getText().trim();
                if (names[i].isEmpty()) {
                    names[i] = "Player " + (i + 1);
                }
            }

            frame.dispose();
            showLoadingScreen(names);
        });

        buttons.add(backBtn);
        buttons.add(startBtn);

        panel.add(fieldsPanel, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);
    }

    private void showLoadingScreen(String[] names) {
        JFrame frame = createFrame("Loading");

        JTextArea area = new JTextArea();
        area.setEditable(false);

        area.append("Starting game...\n\nPlayers:\n");
        for (String name : names) {
            area.append("- " + name + "\n");
        }

        frame.add(new JScrollPane(area));
        frame.setVisible(true);

        javax.swing.Timer timer = new javax.swing.Timer(5000, null);

        timer.addActionListener(e -> {
            timer.stop();
            frame.dispose();
            showResults(names);
        });

        timer.setRepeats(false);
        timer.start();
    }

    private void showResults(String[] names) {
        JFrame frame = createFrame("Results");

        JTextArea area = new JTextArea();
        area.setEditable(false);

        Random rand = new Random();

        area.append("Results:\n\n");

        for (String name : names) {
            boolean win = rand.nextBoolean();
            area.append(name + " : " + (win ? "WINNER 🎉" : "LOST") + "\n");
        }

        JButton newGameBtn = new JButton("New Game");

        newGameBtn.addActionListener(e -> {
            frame.dispose();
            showMainMenu();
        });

        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(area), BorderLayout.CENTER);
        frame.add(newGameBtn, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private JFrame createFrame(String title) {
        JFrame frame = new JFrame(title);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);

        if (title.equals("Main Menu")) {
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        } else {
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }

        return frame;
    }
}