package src;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().showMainMenu());
    }

    public void showMainMenu() {
        JFrame frame = createFrame("Ticket to Ride - Sprint 1 JAR");
        JButton demoBtn = new JButton("Demo Prototype");
        demoBtn.setFont(new Font("Arial", Font.BOLD, 18));
        demoBtn.addActionListener(e -> {
            frame.dispose();
            showDemo();
        });
        frame.add(demoBtn);
        frame.setVisible(true);
    }

    private void showDemo() {
        JFrame frame = createFrame("Prototype Works!");
        JTextArea text = new JTextArea("Sprint 1 Tech Prototype\n\n✓ Java Swing UI\n✓ Standalone JAR\n✓ OOP ready\n✓ Non-console\n\nReady for game logic!");
        text.setEditable(false);
        JButton done = new JButton("Done");
        done.addActionListener(e -> {
            frame.dispose();
            new Main().showMainMenu();
        });
        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(text), BorderLayout.CENTER);
        frame.add(done, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private JFrame createFrame(String title) {
        JFrame f = new JFrame(title);
        f.setSize(400, 300);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        return f;
    }
}