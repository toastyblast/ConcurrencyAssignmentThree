package messages;

import akka.actor.ActorRef;

/**
 * Serves as a message type for the main program to tell actors to stop.
 */
public class Stop {

    private ActorRef fan;

    public  Stop(ActorRef fan){
        this.fan = fan;
    }

    public ActorRef getFan() {
        return fan;
    }
}
