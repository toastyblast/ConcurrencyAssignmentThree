package messages;

public class TicketRequest {
    private final int numberOfTickets, sectionDesired;

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
}
