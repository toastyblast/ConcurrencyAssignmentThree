package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.*;

import java.util.ArrayList;

/**
 * Section agent that keeps track of their own section and the seats taken from it, or returned if a purchase
 * has been cancelled.
 * <p>
 * If the Section Agent has no seats left anymore after the purchase that took the last seats has been confirmed,
 * it goes into a blocked mode. Here it blocks all further messages sent to it, as there's nothing left to do for them
 * anymore.
 */
public class SectionAgent extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final int sectionToManage;
    private int amountOfSpaces, amountOfSpacesAfterConfirmation;
    private ArrayList<Integer> sectionPurchaseHistory = new ArrayList<>();

    //Special kind of behaviour the Section Agent can become when they do not have any spaces left.
    private Receive blockRequests;

    private SectionAgent(int sectionToManage, int amountOfSpaces) {
        this.sectionToManage = sectionToManage;
        this.amountOfSpaces = amountOfSpaces;
        this.amountOfSpacesAfterConfirmation = amountOfSpaces;

        //This behaviour is triggered when all the spaces of this agent's section has been taken.
        // It matches any messages and tells it that it can't finish the task.
        blockRequests = receiveBuilder()
                .match(TicketRequest.class, message -> {
                    log.info("SECTION AGENT BLOCK - TR - I told " + getSender() + " to STOP. Tickets left: " + amountOfSpacesAfterConfirmation);
                    getSender().tell(new Stop(getSender()), getSelf());
                })
                .match(PurchaseConfirmation.class, message -> {
                    log.info("SECTION AGENT BLOCK - PC - I told " + message.getPersonInvolvedWithPurchase() + " to STOP.");
                    getSender().tell(new Stop(message.getPersonInvolvedWithPurchase()), getSelf());
                })
                .matchAny(o -> log.info("I don't have any tickets left... Leave me alone."))
                .build();
    }

    /**
     * Special type of constructor used by AKKA. Shows the user of the program what the Actor needs, discouraging them
     * from using normal constructors, including an empty one.
     *
     * @param sectionToManage is the section this Section Agent is responsible for.
     * @param amountOfSpaces is the amount of tickets available for this section.
     * @return the creation of the normal constructor of the SectionAgent Actor.
     */
    public static Props prop(int sectionToManage, int amountOfSpaces) {
        return Props.create(SectionAgent.class, sectionToManage, amountOfSpaces);
    }

    public void preStart() {
        log.debug("SECTION " + sectionToManage + " AGENT - Starting");
        //OPTIONAL - Replace the log with any code that should be done as the SectionAgent starts (This could include telling another actor about yourself)
    }

    /**
     * Method that is triggered once the Actor receives a mail from their mailbox. It handles all messages known to it
     * in the appropriate way.
     *
     * @return the match responses from this Actor to the message received by it.
     */
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TicketRequest.class, message -> {
                    int requestedTickets = message.getNumberOfTickets();
                    //The reference to the fan, keep it so the sales agent can know where to return the message.
                    ActorRef fanForSalesToSendBackTo = getSender();

                    log.info("SECTION AGENT - Got request from " + fanForSalesToSendBackTo + ". Seats left before REQUEST: " + amountOfSpaces);

                    if (amountOfSpaces >= requestedTickets) {
                        //Set a "Reservation" (Basically already bought).
                        amountOfSpaces -= requestedTickets;

                        sectionPurchaseHistory.add(requestedTickets);
                        //Get the index (or "purchase ID") of the reservation just made, aka get the index of the last item in the ArrayList.
                        int purchaseID = sectionPurchaseHistory.indexOf(sectionPurchaseHistory.get((sectionPurchaseHistory.size() - 1)));

                        getSender().tell(new TicketReqResponse(true, purchaseID, fanForSalesToSendBackTo), getSelf());
                    } else if (amountOfSpaces < requestedTickets && amountOfSpaces > 0) {
                        //I still have tickets left, just not the amount the fan wants. Offer them this smaller amount.
                        sectionPurchaseHistory.add(amountOfSpaces);
                        //Since you're offering your last tickets, set your amount of spaces to zero.
                        int tempAmountOfSpaces = amountOfSpaces;
                        amountOfSpaces = 0;
                        //Get the index (or "purchase ID") of the reservation just made, aka get the index of the last item in the ArrayList.
                        int purchaseID = sectionPurchaseHistory.indexOf(sectionPurchaseHistory.get((sectionPurchaseHistory.size() - 1)));

                        getSender().tell(new TicketReqOffer(purchaseID, tempAmountOfSpaces, fanForSalesToSendBackTo), getSelf());
                    } else {
                        //Send message that this is not possible, as I have no tickets left.
                        getSender().tell(new TicketReqResponse(false, -1, fanForSalesToSendBackTo), getSelf());
                    }

                    log.info("SECTION AGENT - Seats left after REQUEST: " + amountOfSpaces + ". Reserved by: " + fanForSalesToSendBackTo);
                })
                .match(PurchaseConfirmation.class, message -> {
                    int amountOfTicketsReserved = sectionPurchaseHistory.get(message.getPurchaseID());

                    if (!message.isFanWantsToBuy()) {
                        //The fan has decided not to buy the tickets after all, so remove their reservation.

                        log.info("SECTION AGENT - Amount of tickets: " + amountOfTicketsReserved + ". NOT BOUGHT by: " + message.getPersonInvolvedWithPurchase());

                        amountOfSpaces += amountOfTicketsReserved;
                    } else {
                        amountOfSpacesAfterConfirmation -= amountOfTicketsReserved;
                        log.info("SECTION AGENT - Amount of tickets: " + amountOfTicketsReserved + ". BOUGHT by: " + message.getPersonInvolvedWithPurchase() + ". Tickets left: " + amountOfSpacesAfterConfirmation);

                        //After all of the RESERVATIONS are confirmed and there are no more spaces left the section
                        //agent becomes, so he can block all of the coming requests.
                        if (amountOfSpacesAfterConfirmation <= 0) {
                            getContext().become(blockRequests);
                        }
                    }

                    //And as the fan now has confirmation on their tickets, we can tell them to stop living.
                    getSender().tell(new Stop(message.getPersonInvolvedWithPurchase()), getSelf());
                })
                .matchAny(object -> log.info("SECTION AGENT - Received unknown message from " + getSender(), object.toString()))
                .build();
    }

    /**
     * Method that is performed right when the Section Agent is stopped.
     */
    @Override
    public void postStop() throws Exception {
        log.info("SECTION AGENT - ON STOP. Has " + amountOfSpaces + " seats left.");
    }
}
