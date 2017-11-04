package messages;

import akka.actor.ActorRef;

/**
 * Message that is sent by a SectionAgent to indicate there are tickets, but not the amount the Fan initially wanted.
 * ActorRef is included so that the SalesAgent knows who to send this message to once it's received by them from the
 * Section Agent.
 */
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
