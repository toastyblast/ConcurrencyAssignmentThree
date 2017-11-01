package messages;

import akka.actor.ActorRef;

public class TicketRequest {
    private int numberOfTickets;
    private int sectionDesired;
    private ActorRef actorRef;

    public TicketRequest(int numberOfTickets, int sectionDesired) {
        this.numberOfTickets = numberOfTickets;
        this.sectionDesired = sectionDesired;
    }

    public int getNumberOfTickets() {
        return numberOfTickets;
    }

    public int getSectionDesired() {
        return sectionDesired;
    }

    public void setActorRef(ActorRef actorRef) {
        this.actorRef = actorRef;
    }

    public ActorRef getActorRef() {
        return actorRef;
    }
}
