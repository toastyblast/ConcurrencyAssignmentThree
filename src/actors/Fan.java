package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.TicketRequest;
import messages.Stop;

public class Fan extends AbstractActor {
    private static final int MAX_AMOUNT_OF_TICKETS = 4;

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private int desiredAmountOfTickets = 1;
    private int desiredSection;
    private ActorRef ticketAgency;
    //Variables...

    private Fan(int desiredSection, ActorRef ticketAgency) {
        desiredAmountOfTickets = (int) (Math.random() * MAX_AMOUNT_OF_TICKETS) + 1;
        this.desiredSection = desiredSection;
        this.ticketAgency = ticketAgency;
    }

    public static Props prop(int desiredSection, ActorRef ticketAgency) {
        return Props.create(Fan.class, desiredSection, ticketAgency);
    }

    public void preStart() {
        log.debug("FAN - Starting");
        ticketAgency.tell(new TicketRequest(desiredAmountOfTickets, desiredSection), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                //Do a .match(class, callback) here, for whatever message it could receive
                .match(Stop.class, message -> {
                    /*Handling for the stop message.*/
                })
                .matchAny(object -> log.info("SECTION AGENT - Received unknown message from " + getSender(), object.toString()))
                .build();
    }

    //Methods, instead of all the Lambda functions...
}
