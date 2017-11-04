package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.*;

import java.util.ArrayList;

/**
 * The Sales Agent serves as a middle man for all communication between Fans and Section Agents. With every message
 * they include a reference on who to contact once he get a reply from the person he's contacting. The contacted person
 * always sends back this reference with their reply, as the Section agent needs it to find the original person to call back.
 */
public class SalesAgent extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private final ArrayList<ActorRef> sectionAgents;

    private SalesAgent(ArrayList<ActorRef> sectionAgents) {
        this.sectionAgents = sectionAgents;
    }

    /**
     * Special type of constructor used by AKKA. Shows the user of the program what the Actor needs, discouraging them
     * from using normal constructors, including an empty one.
     *
     * @param sectionAgents is a list of all the Section Agents, since the Sales Agent needs to contact one of these for
     *                      each ticket request from a Fan.
     * @return the creation of the normal constructor of the SalesAgent Actor.
     */
    public static Props prop(ArrayList<ActorRef> sectionAgents) {
        return Props.create(SalesAgent.class, sectionAgents);
    }

    /**
     * Method that is triggered right at the start of when the Actor is made. Can be used to do any kinds of preparation.
     */
    public void preStart() {
        log.debug("SALES AGENT - Starting");
        log.info("SALES AGENT - STARTED");
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
                    int requestedSection = message.getSectionDesired();

                    if (requestedSection >= 1 && requestedSection <= sectionAgents.size()) {
                        //Send the request for a ticket to the right section agent.

                        //Forward the message to the section agent, so that the SecA can put the fan in the response
                        // so that the sales agent then knows where to send the response back to.
                        sectionAgents.get((requestedSection - 1)).forward(message, getContext());
                    } else {
                        //This means the Fan is requesting a section that does not exist, so just stop them.
                        getSender().tell(new Stop(null), getSelf());
                    }
                })
                .match(TicketReqResponse.class, message -> {
                    //Get the actor reference and save it.
                    ActorRef fan = message.getActorRef();
                    //Get the information for the new message.
                    boolean isReservation = message.isReservationMade();
                    int purchaseID = message.getPurchaseID();
                    //Return the message to the actor.
                    fan.tell(new TicketReqResponse(isReservation, purchaseID, getSender()), getSelf());
                })
                .match(TicketReqOffer.class, message -> {
                    //Get which Fan to call back from the message.
                    ActorRef fan = message.getPersonSalesAgentNeedsToContact();
                    //Get the information to make a copy of this message with a new contact.
                    int amountOfTicketsOffered = message.getAmountOfTickets();
                    int purchaseID = message.getPurchaseID();
                    //Bring message to the Fan, as you now set the section agent to be the one to reconnect with later.
                    fan.tell(new TicketReqOffer(amountOfTicketsOffered, purchaseID, getSender()), getSelf());
                })
                .match(PurchaseConfirmation.class, message -> {
                    //This means the Fan wants to let us and the section agent know if they decided to purchase the
                    // tickets or not. Route this to the section agent so they can handle that.
                    //Get the actor reference and save it.
                    ActorRef sectionAgent = message.getPersonInvolvedWithPurchase();
                    //Get the information for the new message.
                    boolean isConfirmation = message.isFanWantsToBuy();
                    int purchaseID = message.getPurchaseID();
                    //Return the message to the actor.
                    sectionAgent.tell(new PurchaseConfirmation(isConfirmation, purchaseID, getSender()), getSelf());
                })
                .match(Stop.class, message -> {
                    //This means the section agent told us that the last request has been finished and that the Fan can
                    // be told to stop now, since they have their tickets.
                    message.getFan().tell(new Stop(null), getSelf());
                })
                .matchAny(object -> log.info("SALES AGENT - Unknown message from " + getSender(), object.toString()))
                .build();
    }

    /**
     * Method that is performed right when the Sales Agent is stopped.
     */
    @Override
    public void postStop() throws Exception {
        log.info("SALES AGENT - STOPPED");
    }
}
