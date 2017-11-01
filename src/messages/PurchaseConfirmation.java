package messages;

import akka.actor.ActorRef;

public class PurchaseConfirmation {
    private boolean fanWantsToBuy;
    private int purchaseID;
    private ActorRef sectionAgent;

    public PurchaseConfirmation(boolean fanWantsToBuy, int purchaseID, ActorRef sectionAgent) {
        this.fanWantsToBuy = fanWantsToBuy;
        this.purchaseID = purchaseID;
        this.sectionAgent = sectionAgent;
    }

    public boolean isFanWantsToBuy() {
        return fanWantsToBuy;
    }

    public int getPurchaseID() {
        return purchaseID;
    }

    public ActorRef getSectionAgent() {
        return sectionAgent;
    }

    public void setSectionAgent(ActorRef sectionAgent) {
        this.sectionAgent = sectionAgent;
    }
}
