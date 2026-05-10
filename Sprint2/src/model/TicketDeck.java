package src.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TicketDeck {
    public final List<DestinationTicket> tickets;

    public TicketDeck(List<DestinationTicket> tickets){
        this.tickets = new ArrayList<>(tickets);
        Collections.shuffle(tickets);
    }

    public List<DestinationTicket> drawThreeTickets(){
        List<DestinationTicket> drawnTickets = new ArrayList<>();
        int count = Math.min(3, tickets.size());
        for (int i = 0; i < count; i++){
            drawnTickets.add(tickets.remove(tickets.size() - 1));
        }
        return drawnTickets;
    }

    public void returnToBottom(List<DestinationTicket> DiscardedTickets){
        tickets.addAll(0, DiscardedTickets);
    }

    public int ticketDeckSize(){
        return tickets.size();
    }

    public boolean isTicketsDeckEmpty(){
        return tickets.isEmpty();
    }
}
