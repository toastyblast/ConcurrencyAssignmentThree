import akka.actor.ActorSystem;

/**
 * Method that is used to run the simulation of Assignment 3 for Saxion's Concurrency - Method Passing with AKKA.
 */
public class AplWorld {
    //Variables...

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("ZiggoDome");

        //Code...

        //Always terminate the system after it's done! Actors stay alive, otherwise.
        system.terminate();
    }

    //Misc. methods...
}
