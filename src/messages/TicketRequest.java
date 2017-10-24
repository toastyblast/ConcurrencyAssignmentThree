package messages;

public class TicketRequest {
    private int numberOfTickets;
    private int sectionDesired;

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
