package src.controller;

import src.model.DestinationTicket;
import src.model.GameEngine;
import src.model.Player;
import src.view.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class DrawTicketController {

    private final MainFrame mainFrame;
    private final GameEngine gameEngine;
    private final TurnController turnController;

    private boolean drawModeActive;

    public DrawTicketController(MainFrame mainFrame, GameEngine gameEngine,
                                TurnController turnController) {
        this.mainFrame = mainFrame;
        this.gameEngine = gameEngine;
        this.turnController = turnController;
        this.drawModeActive = false;
        wireListeners();
    }

    private void wireListeners() {
        mainFrame.getDrawTicketsButton().addActionListener(e -> handleEnterDrawMode());
        mainFrame.getDestinationDeckButton().addActionListener(e -> handleDeckClick());
    }

    /**
     * Called when the player clicks "Draw Tickets".
     * Locks all other action buttons immediately via onActionStarted().
     */
    private void handleEnterDrawMode() {
        if (drawModeActive) {
            return;
        }

        if (gameEngine.getTicketDeck().isTicketsDeckEmpty()) {
            JOptionPane.showMessageDialog(
                    mainFrame,
                    "The destination ticket deck is empty. Please choose a different action.",
                    "No Tickets Available",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        drawModeActive = true;

        // Lock all other action buttons immediately — player has committed to drawing tickets
        turnController.onActionStarted();
        mainFrame.setTitle("London Map - Click the Destination Ticket deck to draw tickets");
    }

    /**
     * Called when the player clicks the destination deck button.
     * Only acts if draw mode is active.
     */
    private void handleDeckClick() {
        if (!drawModeActive) {
            return;
        }

        Player currentPlayer = gameEngine.getCurrentPlayer();
        List<DestinationTicket> drawnTickets = gameEngine.getTicketDeck().drawThreeTickets();

        List<DestinationTicket> keptTickets = showTicketSelectionDialog(currentPlayer, drawnTickets);

        // null means player cancelled — return all tickets and re-enable action buttons
        if (keptTickets == null) {
            gameEngine.getTicketDeck().returnToBottom(drawnTickets);
            drawModeActive = false;
            mainFrame.setTitle("London Map - Game On");

            // Re-enable all action buttons so player can choose a different action
            mainFrame.getDrawCardsButton().setEnabled(true);
            mainFrame.getDrawTicketsButton().setEnabled(true);
            mainFrame.getClaimRouteButton().setEnabled(true);
            return;
        }

        List<DestinationTicket> returnedTickets = new ArrayList<>(drawnTickets);
        returnedTickets.removeAll(keptTickets);

        for (DestinationTicket ticket : keptTickets) {
            currentPlayer.addTickets(ticket);
        }

        if (!returnedTickets.isEmpty()) {
            gameEngine.getTicketDeck().returnToBottom(returnedTickets);
        }

        drawModeActive = false;
        mainFrame.setTitle("London Map - Game On");
        gameEngine.notifyObservers();
        turnController.onActionCompleted();
    }

    /**
     * Shows a modal dialog presenting the drawn tickets as checkboxes.
     * Returns the kept tickets on confirm, or null if the player cancels.
     */
    private List<DestinationTicket> showTicketSelectionDialog(
            Player player, List<DestinationTicket> drawnTickets) {

        JDialog dialog = new JDialog(mainFrame, "Draw Destination Tickets", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setResizable(false);

        JLabel header = new JLabel(
                "<html><b>" + player.getPlayerName() + "</b> — choose which tickets to keep"
                        + " (you must keep at least 1):</html>",
                JLabel.CENTER);
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 4, 16));
        dialog.add(header, BorderLayout.NORTH);

        JPanel checkboxPanel = new JPanel(new GridLayout(drawnTickets.size(), 1, 6, 6));
        checkboxPanel.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        List<JCheckBox> checkBoxes = new ArrayList<>();
        for (DestinationTicket ticket : drawnTickets) {
            String label = ticket.getCity1() + "  ↔  " + ticket.getCity2()
                    + "   [" + ticket.getPoints() + " pts]";
            JCheckBox cb = new JCheckBox(label, true);
            cb.setFont(new Font("Arial", Font.PLAIN, 14));
            checkboxPanel.add(cb);
            checkBoxes.add(cb);
        }
        dialog.add(checkboxPanel, BorderLayout.CENTER);

        JButton confirmButton = new JButton("Confirm");
        JButton cancelButton = new JButton("Cancel (return all)");
        confirmButton.setFont(new Font("Arial", Font.BOLD, 13));
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 13));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 12, 0));
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        final List<DestinationTicket>[] result = new List[]{null};

        confirmButton.addActionListener(e -> {
            List<DestinationTicket> kept = new ArrayList<>();
            for (int i = 0; i < checkBoxes.size(); i++) {
                if (checkBoxes.get(i).isSelected()) {
                    kept.add(drawnTickets.get(i));
                }
            }
            if (kept.isEmpty()) {
                JOptionPane.showMessageDialog(
                        dialog,
                        "You must keep at least 1 destination ticket.",
                        "Invalid Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            result[0] = kept;
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dialog.dispose();
            }
        });

        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(400, 200));
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setVisible(true);

        return result[0];
    }
}