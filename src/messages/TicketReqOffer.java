package messages;

import akka.actor.ActorRef;

public class TicketReqOffer {
    private final int purchaseID, amountOfTickets;
    private final ActorRef personSalesAgentNeedsToContact;

    public TicketReqOffer (int purchaseID, int amountOfTickets, ActorRef personSalesAgentNeedsToContact) {
        this.purchaseID = purchaseID;
        this.amountOfTickets = amountOfTickets;
        this.personSalesAgentNeedsToContact = personSalesAgentNeedsToContact;
    }

    public int getPurchaseID() {
        return purchaseID;
    }

    public int getAmountOfTickets() {
        return amountOfTickets;
    }

    public ActorRef getPersonSalesAgentNeedsToContact() {
        return personSalesAgentNeedsToContact;
    }
}
