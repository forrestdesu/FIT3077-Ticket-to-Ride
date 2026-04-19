package src.view;

import javax.swing.*;
import java.awt.*;

public class FaceUpPanel extends JPanel {
    public FaceUpPanel() {
        setLayout(new GridLayout(1, 5, 5, 5));
        setBorder(BorderFactory.createTitledBorder("Face-Up Cards"));
        for (int i = 1; i <= 5; i++) {
            add(new JButton("Card " + i));
        }
    }
}
