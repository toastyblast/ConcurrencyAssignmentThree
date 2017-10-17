package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.Stop;

public class Fan extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    //Variables...

    private Fan(/*Stuff a actors.Fan needs*/) {
        //Constructor...
    }

    public static Props prop(/*The same stuff the constructor needs*/) {
        return Props.create(Fan.class/*, the stuff the constructor needs*/);
    }

    public void preStart() {
        log.debug("FAN - Starting");
        //OPTIONAL - Replace the log with any code that should be done as the Fan starts (This could include telling another actor about yourself)
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
