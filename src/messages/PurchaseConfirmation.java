package messages;

import akka.actor.ActorRef;

public class PurchaseConfirmation {
    private final boolean fanWantsToBuy;
    private final int purchaseID;
    private final ActorRef personInvolvedWithPurchase;

    public PurchaseConfirmation(boolean fanWantsToBuy, int purchaseID, ActorRef salesAgentReminder) {
        this.fanWantsToBuy = fanWantsToBuy;
        this.purchaseID = purchaseID;
        this.personInvolvedWithPurchase = salesAgentReminder;
    }

    public boolean isFanWantsToBuy() {
        return fanWantsToBuy;
    }

    public int getPurchaseID() {
        return purchaseID;
    }

    public ActorRef getPersonInvolvedWithPurchase() {
        return personInvolvedWithPurchase;
    }
}
