package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.*;

/**
 * A Fan is a complex being with only one blissful desire: Buying tickets. Once they got confirmation that they either
 * got the tickets, or didn't, they are stopped (Since their objective is done and this simulation does not simulate
 * them enjoying their time at the place they bough tickets for as well). The Fan is also bipolar, sometimes deciding
 * they didn't want the tickets they requested after all. In case that they get a message from the Sales Agent that
 * there are less tickets than they desired, they decided to take the offer of less tickets or not.
 * <p>
 * They only contact the Sales Agent. However, they do include references to Section agents in some messages, as the
 * Section Agent needs these to know who to call back.
 */
public class Fan extends AbstractActor {
    private static final int MAX_AMOUNT_OF_TICKETS = 4;
    private static final int MIN_AMOUNT_OF_TICKETS = 1;
    //This integer represents an percentage from 1-100. Making it higher will increase the chance the fan buys the
    // tickets. Increasing it past 100 guarantees that they buy them. Lowering this value will decrease the chance the
    // fan buys the tickets they desired. Values below 1 will guarantee that the fan never accepts the tickets they wanted.
    private static final int CHANCE_TO_BUY_TICKETS = 70;

    //These two integers have influence on the Fan buying an offer with less tickets than they desired, triggered when
    // the section agent doesn't have enough tickets to satisfy the request. These are both percentages.
    //Penalty is how much percentage the user's chance to NOT by the tickets increases per ticket less than what they
    // desired in the first place. Increase this number if you want to lower the chance that a Fan takes the offer.
    private static final int PENALTY_PER_MISSING_TICKET = 5;
    //This is the overall chance the Fan will take the offer of less tickets.
    private static final int MAX_CHANCE_TO_TAKE_OFFER = 50;

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final int desiredAmountOfTickets, desiredSection;
    private final ActorRef ticketAgency;

    private Fan(int desiredSection, ActorRef ticketAgency) {
        desiredAmountOfTickets = (int) (Math.random() * MAX_AMOUNT_OF_TICKETS) + MIN_AMOUNT_OF_TICKETS;
        this.desiredSection = desiredSection;
        this.ticketAgency = ticketAgency;
    }

    /**
     * Special type of constructor used by AKKA. Shows the user of the program what the Actor needs, discouraging them
     * from using normal constructors, including an empty one.
     *
     * @param desiredSection is the section that the Fan wants to get tickets from
     * @param ticketAgency is the "Phone number" (aka a reference) to the Ticket Agency, which the Fan contacts.
     * @return the creation of the normal constructor of the Fan Actor.
     */
    public static Props prop(int desiredSection, ActorRef ticketAgency) {
        return Props.create(Fan.class, desiredSection, ticketAgency);
    }

    /**
     * Method that is triggered right at the start of when the Actor is made. Can be used to do any kinds of preparation.
     * In this case, the Fan sends their request to the Sales Agent to start the process of getting tickets.
     */
    public void preStart() {
        log.debug("FAN - Starting");
        log.info("FAN - Section desired: " + desiredSection + ". Tickets desired: " + desiredAmountOfTickets);
        ticketAgency.tell(new TicketRequest(desiredAmountOfTickets, desiredSection), getSelf());
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
                .match(TicketReqResponse.class, message -> {
                    if (message.isReservationMade()) {
                        int decideToBuy = (int) (Math.random() * 100) + 1;
                        int myPurchaseID = message.getPurchaseID();

                        if (decideToBuy <= CHANCE_TO_BUY_TICKETS) {
                            //The user decides that they want to buy the ticket(s) (70% chance)
                            log.info("FAN - Requested tickets BOUGHT.");
                            getSender().tell(new PurchaseConfirmation(true, myPurchaseID, message.getActorRef()), getSelf());
                        } else {
                            //The user decides that they do NOT want to buy the ticket(s) (30% chance)
                            log.info("FAN - Requested tickets NOT BOUGHT.");
                            getSender().tell(new PurchaseConfirmation(false, myPurchaseID, message.getActorRef()), getSelf());
                        }
                    } else {
                        //The amount of tickets requested is not available, so stop, as you have nothing more to do.
                        log.info("FAN - No tickets available. STOP.");
                        getSelf().tell(new Stop(null), getSelf());
                    }
                })
                .match(TicketReqOffer.class, message -> {
//                    log.debug("FAN - GOT RESPONSE MESSAGE FROM: " + getSender(), message.toString());

                    int amountOfTicketsOffered = message.getAmountOfTickets();
                    //For every ticket less in the offer than what the fan desired, make the chance to accept the offer
                    // lower (i.e: 2 missing tickets times 5% P.T. penalty = 10% overall penalty).
                    int chancePenalty = (desiredAmountOfTickets - amountOfTicketsOffered) * PENALTY_PER_MISSING_TICKET;
                    //The higher this percentage is, the lower the chance of the fan buying the lesser amount of tickets is.
                    int decideToBuyOffer = (int) (Math.random() * 100) + 1;
                    //Add the penalty to the chance of the fan buying the ticket, further increasing the chance they
                    // won't buy the tickets offered to them.
                    decideToBuyOffer += chancePenalty;

                    int myPurchaseID = message.getPurchaseID();

                    if (decideToBuyOffer <= MAX_CHANCE_TO_TAKE_OFFER) {
                        //The user decides that they want to buy the smaller amount of tickets offered to them.
                        log.info("FAN - Offered tickets BOUGHT. Desired: " + desiredAmountOfTickets + ". Offered: " + amountOfTicketsOffered);
                        getSender().tell(new PurchaseConfirmation(true, myPurchaseID, message.getPersonSalesAgentNeedsToContact()), getSelf());
                    } else {
                        //The user decides that they do NOT want to buy the ticket(s) (30% chance)
                        log.info("FAN - Offered tickets NOT BOUGHT. Desired: " + desiredAmountOfTickets + ". Offered: " + amountOfTicketsOffered);
                        getSender().tell(new PurchaseConfirmation(false, myPurchaseID, message.getPersonSalesAgentNeedsToContact()), getSelf());
                    }
                })
                .match(Stop.class, message -> {
                    log.info("FAN - Told to STOP by " + getSender(), message.toString());

                    getContext().stop(getSelf());
                })
                .matchAny(object -> log.info("FAN - UNKNOWN message from " + getSender(), object.toString()))
                .build();
    }
}
