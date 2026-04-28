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
 *   <li>Player clicks the Destination Ticket deck button to draw tickets and
 *       open the selection dialog.</li>
 * </ol>
 *
 * <p>In the dialog:
 * <ul>
 *   <li>Each drawn ticket is shown as a pre-checked checkbox.</li>
 *   <li>Player unchecks any tickets they do not want to keep.</li>
 *   <li>At least 1 ticket must be kept before Confirm is accepted.</li>
 *   <li>Closing with the X button cancels the action — all drawn tickets
 *       are returned to the bottom of the deck and the turn does not advance.</li>
 * </ul>
 */
public class DrawTicketController {

    private final MainFrame mainFrame;
    private final GameEngine gameEngine;

    /** Whether the player has clicked "Draw Tickets" and is waiting to click the deck. */
    private boolean drawModeActive;

    /**
     * Constructs the controller and wires all button listeners.
     *
     * @param mainFrame  the main game window
     * @param gameEngine the central game state
     */
    public DrawTicketController(MainFrame mainFrame, GameEngine gameEngine) {
        this.mainFrame = mainFrame;
        this.gameEngine = gameEngine;
        this.drawModeActive = false;
        wireListeners();
    }

    /**
     * Attaches listeners to the "Draw Tickets" action button and the
     * destination deck button in DeckPanel.
     */
    private void wireListeners() {
        // Step 1: enter draw mode
        mainFrame.getDrawTicketsButton().addActionListener(e -> handleEnterDrawMode());

        // Step 2: click the destination deck to draw and open dialog
        mainFrame.getDestinationDeckButton().addActionListener(e -> handleDeckClick());
    }

    /**
     * Called when the player clicks "Draw Tickets" in the Actions panel.
     * Validates that tickets are available and activates draw mode.
     */
    private void handleEnterDrawMode() {
        if (drawModeActive) {
            return; // already in draw mode, ignore double-click
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
     * Only acts if draw mode is active. Draws tickets, shows the selection
     * dialog, and processes the player's choices.
     */
    private void handleDeckClick() {
        if (!drawModeActive) {
            return; // ignore clicks outside of draw mode
        }

        Player currentPlayer = gameEngine.getCurrentPlayer();
        List<DestinationTicket> drawnTickets = gameEngine.getTicketDeck().drawThreeTickets();

        // Show the selection dialog
        List<DestinationTicket> keptTickets = showTicketSelectionDialog(currentPlayer, drawnTickets);

        // null means the player closed the dialog with X — cancel the action
        if (keptTickets == null) {
            gameEngine.getTicketDeck().returnToBottom(drawnTickets);
            drawModeActive = false;
            mainFrame.setTitle("London Map - Game On");
            return;
        }

        // Return unchosen tickets to the bottom of the deck
        List<DestinationTicket> returnedTickets = new ArrayList<>(drawnTickets);
        returnedTickets.removeAll(keptTickets);

        for (DestinationTicket ticket : keptTickets) {
            currentPlayer.addTickets(ticket);
        }

        if (!returnedTickets.isEmpty()) {
            gameEngine.getTicketDeck().returnToBottom(returnedTickets);
        }

        endDrawPhase();
    }

    /**
     * Resets draw mode state, restores the title, and advances the turn.
     */
    private void endDrawPhase() {
        drawModeActive = false;
        mainFrame.setTitle("London Map - Game On");
        gameEngine.nextPlayerTurn();
    }

    /**
     * Shows a modal dialog presenting the drawn tickets as checkboxes.
     * The player must keep at least 1. Closing with X returns null so the
     * caller can cancel the action and return all tickets to the deck.
     *
     * @param player       the current player (used for the dialog header)
     * @param drawnTickets the tickets drawn from the deck
     * @return the list of tickets the player chose to keep, or null if cancelled
     */
    private List<DestinationTicket> showTicketSelectionDialog(
            Player player, List<DestinationTicket> drawnTickets) {

        JDialog dialog = new JDialog(mainFrame, "Draw Destination Tickets", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setResizable(false);

        // Header
        JLabel header = new JLabel(
                "<html><b>" + player.getPlayerName() + "</b> — choose which tickets to keep"
                        + " (you must keep at least 1):</html>",
                JLabel.CENTER);
        header.setBorder(BorderFactory.createEmptyBorder(12, 16, 4, 16));
        dialog.add(header, BorderLayout.NORTH);

        // One checkbox per drawn ticket, all pre-checked
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

        // Confirm and Cancel buttons
        JButton confirmButton = new JButton("Confirm");
        JButton cancelButton = new JButton("Cancel (return all)");
        confirmButton.setFont(new Font("Arial", Font.BOLD, 13));
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 13));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 12, 0));
        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        // result[0] stays null if cancelled; set to kept list on confirm
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

        // Cancel button and X button both close with result staying null
        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dialog.dispose(); // result[0] remains null → caller cancels the action
            }
        });

        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(400, 200));
        dialog.setLocationRelativeTo(mainFrame);
        dialog.setVisible(true); // blocks until disposed

        return result[0];
    }
}