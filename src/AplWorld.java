import actors.Fan;
import actors.SalesAgent;
import actors.SectionAgent;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.routing.RoundRobinPool;
import akka.routing.Router;

/**
 * Method that is used to run the simulation of Assignment 3 for Saxion's Concurrency - Method Passing with AKKA.
 */

public class AplWorld {
    private static final int AMOUNT_OF_SALES_AGENTS = 5;
    private static final int AMOUNT_OF_SECTIONS = 7;
    private static final int AMOUNT_OF_FANS = 25;

    //Variables...

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("ZiggoDome");

        //Makes a router for sales agents, which fans contact.
        ActorRef ticketAgency = system.actorOf(new RoundRobinPool(AMOUNT_OF_SALES_AGENTS).props(SalesAgent.prop()), "ticketAgency");

        //Create the section agents...
        for (int section = 1; section < AMOUNT_OF_SECTIONS; section++) {
            ActorRef sectionAgent = system.actorOf(SectionAgent.prop(section), "SecAg-" + section);
        }

        //Create the fans...
//        List<ActorRef> fans = new ArrayList<>();
        for (int i = 1; i < AMOUNT_OF_FANS; i++) {
            int sectionToDesire = (int) (Math.random() * AMOUNT_OF_SECTIONS) + 1;

            ActorRef fan = system.actorOf(Fan.prop(sectionToDesire, ticketAgency), "Fan-" + i);
//            fans.add(fan);
        }

        //TODO: Replace this trycatch with a future loop.
        try {
            Thread.sleep(60000);
        } catch (InterruptedException ie) {/* Don't throw anything or you won't reach system.terminate(). */}

        //Always terminate the system after it's done! Actors stay alive, otherwise.
        system.terminate();
    }

    //Misc. methods...
}
