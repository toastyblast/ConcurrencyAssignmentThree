package actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.Stop;

public class SectionAgent extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private int sectionToManage;

    private SectionAgent(int sectionToManage) {
        this.sectionToManage = sectionToManage;
    }

    public static Props prop(int sectionToManage) {
        return Props.create(SectionAgent.class, sectionToManage);
    }

    public void preStart() {
        log.debug("SECTION AGENT - Starting");
        //OPTIONAL - Replace the log with any code that should be done as the SectionAgent starts (This could include telling another actor about yourself)
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
