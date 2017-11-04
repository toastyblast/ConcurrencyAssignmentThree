package messages;

import akka.actor.ActorRef;

/**
 * Message that is sent by a Section Agent if it has the amount of tickets requested from it. The message is first
 * received by a Sales Agent, who sends it back to the fan that did the request bound to this purchase ID. ActorRef is
 * included so that the SalesAgent knows which Fan to send the message back to.
 */
public class TicketReqResponse {
    private final boolean reservationMade;
    private final int purchaseID;
    private final ActorRef actorRef;

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
}
