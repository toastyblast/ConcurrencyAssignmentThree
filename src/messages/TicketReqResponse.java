package messages;

public class TicketReqResponse {
    private boolean reservationMade;
    private int purchaseID;

    public TicketReqResponse(boolean reservationMade, int purchaseID) {
        this.reservationMade = reservationMade;
        this.purchaseID = purchaseID;
    }

    public boolean isReservationMade() {
        return reservationMade;
    }

    public int getPurchaseID() {
        return purchaseID;
    }
}
