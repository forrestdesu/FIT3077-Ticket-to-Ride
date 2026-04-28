package src.controller;

import src.model.DestinationTicket;
import src.model.GameEngine;
import src.model.Player;
import src.view.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller responsible for handling the "Draw Destination Tickets" action.
 *
 * <p>When the player clicks "Draw Tickets":
 * <ol>
 *   <li>3 tickets are drawn from the top of the deck (or fewer if fewer remain).</li>
 *   <li>A modal dialog shows each ticket as a checkbox, all pre-checked.</li>
 *   <li>The player unchecks any tickets they do not want to keep.</li>
 *   <li>At least 1 ticket must be kept — confirmed on the dialog.</li>
 *   <li>Kept tickets are added to the player's hand; unchosen tickets are
 *       returned to the bottom of the deck.</li>
 *   <li>The turn advances to the next player.</li>
 * </ol>
 */
public class DrawTicketController {

    private final MainFrame mainFrame;
    private final GameEngine gameEngine;

    /**
     * Constructs the controller and wires the Draw Tickets button listener.
     *
     * @param mainFrame  the main game window
     * @param gameEngine the central game state
     */
    public DrawTicketController(MainFrame mainFrame, GameEngine gameEngine) {
        this.mainFrame = mainFrame;
        this.gameEngine = gameEngine;
        wireListeners();
    }

    /** Attaches the action listener to the "Draw Tickets" button in ActionsPanel. */
    private void wireListeners() {
        mainFrame.getDrawTicketsButton().addActionListener(e -> handleDrawTickets());
    }

    /**
     * Handles the full draw destination tickets flow when the button is clicked.
     * Draws tickets, shows the selection dialog, processes the player's choices,
     * and advances the turn.
     */
    private void handleDrawTickets() {
        if (gameEngine.getTicketDeck().isTicketsDeckEmpty()) {
            JOptionPane.showMessageDialog(
                    mainFrame,
                    "The destination ticket deck is empty. Please choose a different action.",
                    "No Tickets Available",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Player currentPlayer = gameEngine.getCurrentPlayer();
        List<DestinationTicket> drawnTickets = gameEngine.getTicketDeck().drawThreeTickets();

        // Show the selection dialog and let player decide what to keep
        List<DestinationTicket> keptTickets = showTicketSelectionDialog(currentPlayer, drawnTickets);

        // keptTickets is null if the dialog was somehow closed without confirming —
        // return all drawn tickets to the bottom and do not end the turn
        if (keptTickets == null) {
            gameEngine.getTicketDeck().returnToBottom(drawnTickets);
            return;
        }

        // Work out which tickets to return
        List<DestinationTicket> returnedTickets = new ArrayList<>(drawnTickets);
        returnedTickets.removeAll(keptTickets);

        // Add kept tickets to the player's hand
        for (DestinationTicket ticket : keptTickets) {
            currentPlayer.addTickets(ticket);
        }

        // Return unchosen tickets to the bottom of the deck
        if (!returnedTickets.isEmpty()) {
            gameEngine.getTicketDeck().returnToBottom(returnedTickets);
        }

        // Advance turn — this calls notifyObservers() to refresh all panels
        gameEngine.nextPlayerTurn();
    }

    /**
     * Shows a modal dialog presenting the drawn tickets as checkboxes.
     * The player must keep at least 1. The dialog cannot be dismissed until
     * a valid selection is confirmed.
     *
     * @param player        the current player (used for display name)
     * @param drawnTickets  the tickets drawn from the deck
     * @return the list of tickets the player chose to keep, or null if dialog
     *         was closed unexpectedly (all tickets will be returned in that case)
     */
    private List<DestinationTicket> showTicketSelectionDialog(
            Player player, List<DestinationTicket> drawnTickets) {

        // Build the dialog
        JDialog dialog = new JDialog(mainFrame, "Draw Destination Tickets", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setResizable(false);

        // Header label
        JLabel header = new JLabel(
                "<html><b>" + player.getPlayerName() + "</b> — choose which tickets to keep "
                        + "(you must keep at least 1):</html>",
                JLabel.CENTER);
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 4, 16));
        dialog.add(header, BorderLayout.NORTH);

        // One checkbox per drawn ticket
        JPanel checkboxPanel = new JPanel(new GridLayout(drawnTickets.size(), 1, 6, 6));
        checkboxPanel.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

        List<JCheckBox> checkBoxes = new ArrayList<>();
        for (DestinationTicket ticket : drawnTickets) {
            String label = ticket.getCity1() + "  ↔  " + ticket.getCity2()
                    + "   [" + ticket.getPoints() + " pts]";
            JCheckBox cb = new JCheckBox(label, true); // pre-checked
            cb.setFont(new Font("Arial", Font.PLAIN, 14));
            checkboxPanel.add(cb);
            checkBoxes.add(cb);
        }
        dialog.add(checkboxPanel, BorderLayout.CENTER);

        // Confirm button
        JButton confirmButton = new JButton("Confirm");
        confirmButton.setFont(new Font("Arial", Font.BOLD, 13));
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 12, 0));
        buttonPanel.add(confirmButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // Result holder — will contain the kept tickets after confirmation
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
                return; // don't close the dialog
            }

            result[0] = kept;
            dialog.dispose();
        });

        // Prevent closing the dialog with the X button without confirming
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        dialog.pack();
        dialog.setMinimumSize(new Dimension(380, 200));
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setVisible(true); // blocks until dialog.dispose() is called

        return result[0];
    }
}