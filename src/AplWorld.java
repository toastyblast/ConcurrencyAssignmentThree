import actors.Fan;
import actors.SalesAgent;
import actors.SectionAgent;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.routing.RoundRobinPool;

import java.util.ArrayList;

/**
 * Method that is used to run the simulation of Assignment 3 for Saxion's Concurrency - Method Passing with AKKA.
 */

public class AplWorld {
    private static final int AMOUNT_OF_SALES_AGENTS = 5;
    private static final int AMOUNT_OF_SECTIONS = 7;
    private static final int AMOUNT_OF_SPACES = 10;
    private static final int AMOUNT_OF_FANS = 30;

    //Variables go here...

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("ZiggoDome");

        //Create the section agents...
        ArrayList<ActorRef> sectionAgents = new ArrayList<>();
        for (int section = 1; section < AMOUNT_OF_SECTIONS; section++) {
            ActorRef sectionAgent = system.actorOf(SectionAgent.prop(section, AMOUNT_OF_SPACES), "SecAg-" + section);

            sectionAgents.add(sectionAgent);
        }

        //Makes a ROUTER for sales agents, which fans contact. TODO: replace with one sales agent as soon as become/unbecome is in the program.
        ActorRef ticketAgency = system.actorOf(new RoundRobinPool(AMOUNT_OF_SALES_AGENTS).props(SalesAgent.prop(sectionAgents)), "ticketAgency");

        //Create the fans...
//        List<ActorRef> fans = new ArrayList<>();
        for (int i = 1; i < AMOUNT_OF_FANS; i++) {
            int sectionToDesire = (int) (Math.random() * AMOUNT_OF_SECTIONS) + 1;

            ActorRef fan = system.actorOf(Fan.prop(sectionToDesire, ticketAgency), "Fan-" + i);
//            fans.add(fan);
        }

        //TODO: Replace this try/catch with a future loop.
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ie) {/* Don't throw anything or you won't reach system.terminate(). */}

        //Always terminate the system after it's done! Actors stay alive, otherwise.
        system.terminate();
    }

    //Misc. methods...
}
