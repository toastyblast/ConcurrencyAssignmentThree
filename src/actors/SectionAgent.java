package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.PurchaseConfirmation;
import messages.Stop;
import messages.TicketReqResponse;
import messages.TicketRequest;

import java.util.ArrayList;

public class SectionAgent extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private int sectionToManage, amountOfSpaces, amountOfSpacesAfterConfirmation;
    private ArrayList<Integer> sectionPurchaseHistory = new ArrayList<>();

    private Receive blockRequests;

    private SectionAgent(int sectionToManage, int amountOfSpaces) {
        this.sectionToManage = sectionToManage;
        this.amountOfSpaces = amountOfSpaces;
        this.amountOfSpacesAfterConfirmation = amountOfSpaces;

        //This behaviour is triggered when all the spaces of this agent's section has been taken. It matches any messages and tells it that it can't finish the task.
        blockRequests = receiveBuilder()
                //Do a .match(class, callback) here, for whatever message it could receive
                .match(TicketRequest.class, message -> {
                    log.info("TR SECTION AGENT BLOCK- I told to stop living to " + getSender() + " Tickets left: " + amountOfSpacesAfterConfirmation);
                    getSender().tell(new Stop(getSender()),getSelf());
                })
                .match(PurchaseConfirmation.class, message -> {
                    log.info("PC SECTION AGENT BLOCK- I told to stop living to " + message.getSectionAgent());
                    getSender().tell(new Stop(message.getSectionAgent()), getSelf());
                })
                .match(Stop.class, message -> {
                    /*TODO?: Handling for the stop message.*/
                })
                .matchAny(o -> log.info("I don't have tickets... leave me alone."))
                .build();
    }

    public static Props prop(int sectionToManage, int amountOfSpaces) {
        return Props.create(SectionAgent.class, sectionToManage, amountOfSpaces);
    }

    @Override
    public void postStop() throws Exception {
        log.info(getSelf() + " has " + amountOfSpaces);
    }

    public void preStart() {
        log.debug("SECTION AGENT - Starting");
        //OPTIONAL - Replace the log with any code that should be done as the SectionAgent starts (This could include telling another actor about yourself)
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TicketRequest.class, message -> {
                    int requestedTickets = message.getNumberOfTickets();
                    //The reference to the fan, keep it so the sales agent can know where to return the message.
                    ActorRef fanForSalesToSendBackTo = getSender();

//                    log.debug("SECTION AGENT - GOT FORWARDED MESSAGE FROM: " + getSender(), message.toString());

                    log.info("SECTION AGENT - Got request from " + fanForSalesToSendBackTo + " and I have so many seats left (before latest purchase request): " + amountOfSpaces);

                    if (amountOfSpaces >= requestedTickets) {
                        //Set a "Reservation" (Basically already bought).
                        amountOfSpaces -= requestedTickets;

                        sectionPurchaseHistory.add(requestedTickets);
                        //Get the index (or "purchase ID") of the reservation just made, aka get the index of the last item in the ArrayList.
                        int purchaseID = sectionPurchaseHistory.indexOf(sectionPurchaseHistory.get((sectionPurchaseHistory.size() - 1)));

                        getSender().tell(new TicketReqResponse(true, purchaseID, fanForSalesToSendBackTo), getSelf());
                    } else {
                        //Send message that this is not possible.
                        getSender().tell(new TicketReqResponse(false, -1, fanForSalesToSendBackTo), getSelf());
                    }

                    log.info("SECTION AGENT - I have so many seats left (after latest purchase request): " + amountOfSpaces + " RESERVED BY: " + fanForSalesToSendBackTo);
                })
                .match(PurchaseConfirmation.class, message -> {
                    int amountOfTicketsReserved = sectionPurchaseHistory.get(message.getPurchaseID());

                    if (!message.isFanWantsToBuy()) {
                        //The fan has decided not to buy the tickets after all, so remove their reservation.

                        log.info("Amount of tickets: " + amountOfTicketsReserved + " NOT RESERVED BY: " + message.getSectionAgent());

                        amountOfSpaces += amountOfTicketsReserved;
                    } else {
                        amountOfSpacesAfterConfirmation -= amountOfTicketsReserved;
                        log.info("Amount of tickets: " + amountOfTicketsReserved + " RESERVED BY: " + message.getSectionAgent() + " Tickets left: " + amountOfSpacesAfterConfirmation);

                        //After all of the RESERVATIONS are confirmed and there are no more spaces left the section
                        //agent becomes, so he can block all of the coming requests.
                        if (amountOfSpacesAfterConfirmation == 0){
                            getContext().become(blockRequests);
                        }
                    }

                    //And as the fan now have their tickets, we can tell them to stop living.
                    getSender().tell(new Stop(message.getSectionAgent()), getSelf());
                })
                //Do a .match(class, callback) here, for whatever message it could receive
                .match(Stop.class, message -> {
                    /*TODO?: Handling for the stop message.*/
                })
                .matchAny(object -> log.info("SECTION AGENT - Received unknown message from " + getSender(), object.toString()))
                .build();
    }

    public int getSectionToManage() {
        return sectionToManage;
    }

    //Methods, instead of all the Lambda functions...
}
