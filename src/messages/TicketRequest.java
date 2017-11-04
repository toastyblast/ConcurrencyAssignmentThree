package messages;

/**
 * First message sent that starts the chain, by a Fan. This message is then received by a Sales Agent who sends it to
 * the correct Section Agent to see if the tickets desired are available.
 */
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
