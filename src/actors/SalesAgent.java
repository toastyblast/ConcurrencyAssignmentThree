package actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import messages.PurchaseConfirmation;
import messages.TicketReqResponse;
import messages.TicketRequest;
import messages.Stop;

import java.util.ArrayList;

public class SalesAgent extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private Receive routingBehaviour;

    private ArrayList<ActorRef> sectionAgents;
    //Variables...

    private SalesAgent(ArrayList<ActorRef> sectionAgents) {
        this.sectionAgents = sectionAgents;

        routingBehaviour = receiveBuilder()
                //TODO: Do matches for all proper messages here, but instead, send them to the router you created.
                //Do a .match(class, callback) here, for whatever message it could receive
                .match(Stop.class, message -> {
                    /*TODO: Handling for the stop message.*/
                })
                .matchAny(object -> log.info("SECTION AGENT (ROUTER MODE) - Received unknown message from " + getSender(), object.toString()))
                .build();
    }

    public static Props prop(ArrayList<ActorRef> sectionAgents) {
        return Props.create(SalesAgent.class, sectionAgents);
    }

    public void preStart() {
        log.debug("SALES AGENT - Starting");
        //OPTIONAL - Replace the log with any code that should be done as the SalesAgent starts (This could include telling another actor about yourself)
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TicketRequest.class, message -> {
                    int requestedSection = message.getSectionDesired();

                    for (int i = 0; i < sectionAgents.size(); i++) {
                        //Check for the section manager to contact
                        if (requestedSection == (i + 1)) {
                            //Send the request for a ticket to the right section agent.

                            //Put reference to the fan, so to sales agent can remember where to return the message.
                            message.setActorRef(getSender());
                            sectionAgents.get(i).tell(message, getSelf());

                            break;
                        }
                    }
                })
                .match(TicketReqResponse.class, message ->{
                    //Get the actor reference and save it.
                    ActorRef fan = message.getActorRef();
                    //Put reference to the section agent, so the sales agent can remember where to return the message.
                    message.setActorRef(getSender());
                    //Return the message to the actor.
                    fan.tell(message, getSelf());
                })
                .match(PurchaseConfirmation.class, message ->{
                    ActorRef sectionAgent = message.getSectionAgent();
                    message.setSectionAgent(getSender());
                    sectionAgent.tell(message, getSelf());
                })
                //Do a .match(class, callback) here, for whatever message it could receive
                .match(Stop.class, message -> {
//                    log.info("Person who should stop " + message.getFan() + " Stopped by: " +getSender());
                    message.getFan().tell(new Stop(null), getSelf());
                })
                .matchAny(object -> log.info("SECTION AGENT - Received unknown message from " + getSender(), object.toString()))
                .build();
    }

    //Methods, instead of all the Lambda functions...
}
