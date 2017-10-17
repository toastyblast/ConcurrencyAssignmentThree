package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.Stop;

public class SalesAgent extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    //Variables...

    private SalesAgent(/*Stuff a actors.SalesAgent needs*/) {
        //Constructor...
    }

    public static Props prop(/*The same stuff the constructor needs*/) {
        return Props.create(SalesAgent.class/*, the stuff the constructor needs*/);
    }

    public void preStart() {
        log.debug("SALES AGENT - Starting");
        //OPTIONAL - Replace the log with any code that should be done as the SalesAgent starts (This could include telling another actor about yourself)
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

    //Other methods..?
}
