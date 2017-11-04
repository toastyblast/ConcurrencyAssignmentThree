package messages;

import akka.actor.ActorRef;

public class TicketReqResponse {
    private final boolean reservationMade;
    private final int purchaseID;
    private ActorRef actorRef;

    public TicketReqResponse(boolean reservationMade, int purchaseID, ActorRef actorRef) {
        this.reservationMade = reservationMade;
        this.purchaseID = purchaseID;
        this.actorRef = actorRef;
    }

    public boolean isReservationMade() {
        return reservationMade;
    }

    public int getPurchaseID() {
        return purchaseID;
    }

    public ActorRef getActorRef() {
        return actorRef;
    }

    public void setActorRef(ActorRef actorRef) {
        this.actorRef = actorRef;
    }
}
