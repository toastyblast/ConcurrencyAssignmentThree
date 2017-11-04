package messages;

import akka.actor.ActorRef;

public class TicketReqOffer {
    private final int purchaseID, amountOfTickets;
    private ActorRef actorRef;

    public TicketReqOffer (int purchaseID, int amountOfTickets, ActorRef actorRef) {
        this.purchaseID = purchaseID;
        this.amountOfTickets = amountOfTickets;
        this.actorRef = actorRef;
    }

    public int getPurchaseID() {
        return purchaseID;
    }

    public int getAmountOfTickets() {
        return amountOfTickets;
    }

    public ActorRef getActorRef() {
        return actorRef;
    }
}
