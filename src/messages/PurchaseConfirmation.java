package messages;

import akka.actor.ActorRef;

/**
 * Message sent by a Fan once they get a message that they can buy tickets.  The ActorRef is there for the SalesAgent to
 * know which SectionAgent to call back for this purchase.
 */
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
