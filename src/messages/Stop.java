package messages;

import akka.actor.ActorRef;

/**
 * Serves as a message type for the main program to tell actors to stop. In the case of this program, only used to stop
 * fans. Reference for a fan can be included so the SalesAgent knows who to send this message to. Can also be null if
 * they know the direct receiver.
 */
public class Stop {
    private final ActorRef fan;

    public Stop(ActorRef fan){
        this.fan = fan;
    }

    public ActorRef getFan() {
        return fan;
    }
}
