package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.PurchaseConfirmation;
import messages.TicketReqResponse;
import messages.TicketRequest;
import messages.Stop;

public class Fan extends AbstractActor {
    private static final int MAX_AMOUNT_OF_TICKETS = 4;
    private static final int MIN_AMOUNT_OF_TICKETS = 1;

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private int desiredAmountOfTickets = MIN_AMOUNT_OF_TICKETS;
    private int desiredSection;
    private ActorRef ticketAgency;
    //Variables...

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

                        if (decideToBuy <= 70) {
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
                //Do a .match(class, callback) here, for whatever message it could receive
                .match(Stop.class, message -> {
                    log.info("FAN - I was told to stop by " + getSender(), message.toString());

                    getContext().stop(getSelf());
                })
                .matchAny(object -> log.info("FAN - Received unknown message from " + getSender(), object.toString()))
                .build();
    }

    //Methods, instead of all the Lambda functions...
}
