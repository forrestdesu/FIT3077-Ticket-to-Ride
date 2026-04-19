package src.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class MainFrame extends JFrame {
    public MainFrame(List<String> playerNames) {
        setTitle("London Map - Game On");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e){
                int response = JOptionPane.showConfirmDialog(null, "Are you sure to exit the game?", "Confirm Game Exit", JOptionPane.YES_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }else{
                    e.getWindow().dispose();
                }
            }
        });
//        setSize(getMaximumSize());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 3;
        gbc.weightx = 0.9;
        gbc.weighty = 0.9;
        add(new BoardPanel(), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.weightx = 0.1;
        gbc.weighty = 0.4;
        add(new PlayerHandPanel(), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weighty = 0.05;
        add(new PlayerScorePanel(playerNames), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weighty = 0.15;
        add(new ActionsPanel(), gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridheight = 1;
        gbc.weighty = 0.2;
        add(new DeckPanel(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        add(new FaceUpPanel(), gbc);

        setLocationRelativeTo(null);
        setVisible(true);
    }
}
