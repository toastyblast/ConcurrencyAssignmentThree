package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.*;

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

    public static Props prop(int desiredSection, ActorRef ticketAgency) {
        return Props.create(Fan.class, desiredSection, ticketAgency);
    }

    public void preStart() {
        log.debug("FAN - Starting");
        log.info("FAN - I want to get seats in section " + desiredSection + " and I want to buy " + desiredAmountOfTickets + " tickets.");
        ticketAgency.tell(new TicketRequest(desiredAmountOfTickets, desiredSection), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TicketReqResponse.class, message -> {
//                    log.debug("FAN - GOT RESPONSE MESSAGE FROM: " + getSender(), message.toString());

                    if (message.isReservationMade()) {
                        int decideToBuy = (int) (Math.random() * 100) + 1;
                        int myPurchaseID = message.getPurchaseID();

                        if (decideToBuy <= CHANCE_TO_BUY_TICKETS) {
                            //The user decides that they want to buy the ticket(s) (70% chance)
                            log.info("FAN - I decided to buy the tickets I requested.");
                            getSender().tell(new PurchaseConfirmation(true, myPurchaseID, message.getActorRef()), getSelf());
                        } else {
                            //The user decides that they do NOT want to buy the ticket(s) (30% chance)
                            log.info("FAN - I decided NOT to buy the tickets I requested.");
                            getSender().tell(new PurchaseConfirmation(false, myPurchaseID, message.getActorRef()), getSelf());
                        }
                    } else {
                        //The amount of tickets requested is not available, so stop, as you have nothing more to do.
                        log.info("FAN - The amount of tickets I requested are not available, so I stop living.");
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
                        log.info("FAN - I decided to buy the tickets I was offered. Amount of tickets I was offered: " + amountOfTicketsOffered);
                        getSender().tell(new PurchaseConfirmation(true, myPurchaseID, getSender()), getSelf());
                    } else {
                        //The user decides that they do NOT want to buy the ticket(s) (30% chance)
                        log.info("FAN - I decided NOT to buy the tickets I was offered. Amount of tickets I was offered: " + amountOfTicketsOffered);
                        getSender().tell(new PurchaseConfirmation(false, myPurchaseID, getSender()), getSelf());
                    }
                })
                .match(Stop.class, message -> {
                    log.info("FAN - I was told to stop by " + getSender(), message.toString());

                    getContext().stop(getSelf());
                })
                .matchAny(object -> log.info("FAN - Received unknown message from " + getSender(), object.toString()))
                .build();
    }
}
