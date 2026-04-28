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

/**
 * Controller responsible for handling the "Draw Destination Tickets" action.
 *
 * <p>Two-step flow:
 * <ol>
 *   <li>Player clicks "Draw Tickets" in the Actions panel to enter draw mode.</li>
 *   <li>Player clicks the Destination Ticket deck button to draw and open the dialog.</li>
 * </ol>
 *
 * <p>In the dialog:
 * <ul>
 *   <li>Each drawn ticket is shown as a pre-checked checkbox.</li>
 *   <li>Player must keep at least 1 ticket before Confirm is accepted.</li>
 *   <li>Closing with X or Cancel returns all tickets to the bottom of the
 *       deck and does not end the turn.</li>
 * </ul>
 *
 * <p>This controller does NOT advance the turn itself. After confirming a valid
 * selection it calls {@link TurnController#onActionCompleted()} so the player
 * can manually end their turn via the End Turn button.
 */
public class DrawTicketController {

    private final MainFrame mainFrame;
    private final GameEngine gameEngine;
    private final TurnController turnController;

    /** Whether the player has clicked "Draw Tickets" and is waiting to click the deck. */
    private boolean drawModeActive;

    /**
     * Constructs the controller and wires all button listeners.
     *
     * @param mainFrame      the main game window
     * @param gameEngine     the central game state
     * @param turnController the turn controller to notify when the action is complete
     */
    public DrawTicketController(MainFrame mainFrame, GameEngine gameEngine,
                                TurnController turnController) {
        this.mainFrame = mainFrame;
        this.gameEngine = gameEngine;
        this.turnController = turnController;
        this.drawModeActive = false;
        wireListeners();
    }

    /** Attaches listeners to the "Draw Tickets" action button and the destination deck button. */
    private void wireListeners() {
        mainFrame.getDrawTicketsButton().addActionListener(e -> handleEnterDrawMode());
        mainFrame.getDestinationDeckButton().addActionListener(e -> handleDeckClick());
    }

    /**
     * Called when the player clicks "Draw Tickets" in the Actions panel.
     * Validates tickets are available and activates draw mode.
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

        // null means player cancelled — return all tickets, do not complete the action
        if (keptTickets == null) {
            gameEngine.getTicketDeck().returnToBottom(drawnTickets);
            drawModeActive = false;
            mainFrame.setTitle("London Map - Game On");
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
     *
     * @param player       the current player
     * @param drawnTickets the tickets drawn from the deck
     * @return kept tickets, or null if cancelled
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

        // Both Cancel button and X close with result staying null
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