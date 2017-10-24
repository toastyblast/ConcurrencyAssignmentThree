package messages;

public class PurchaseConfirmation {
    private boolean fanWantsToBuy;
    private int purchaseID;

    public PurchaseConfirmation(boolean fanWantsToBuy, int purchaseID) {
        this.fanWantsToBuy = fanWantsToBuy;
        this.purchaseID = purchaseID;
    }

    public boolean isFanWantsToBuy() {
        return fanWantsToBuy;
    }

    public int getPurchaseID() {
        return purchaseID;
    }
}
