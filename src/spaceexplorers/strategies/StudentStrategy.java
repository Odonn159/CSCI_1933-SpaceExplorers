package spaceexplorers.strategies;

import spaceexplorers.publicapi.*;

import java.util.*;

class Eplanet{//my own class because it was easier to implement. Ended up being somewhat superfluous, Because I wanted to store more values in it but ended up simplifying later on.
    public long occupied, popvalue;
    double value, nearbyvalue;
    public IVisiblePlanet planet;
    public int planetID;
    public Set<IEdge> edges;
    public Eplanet(IVisiblePlanet planet, int Occupation, double value, long popvalue, Set<IEdge> edges) {
        this.value = value; //priority I have assigned it, Settled on it just being Habitibility, but in my original plans I was going to weight it. Didnt work out.
        this.occupied = Occupation; //1 is player 1, 2 is player 2 , 0 is unopccupied and 3 is joint occupied.
        this.popvalue = popvalue; //popvalue is population player1 -population player2.
        this.planet = planet;
        this.edges = edges;
    }
    public void setvalue(double input){
        this.value = input;
    }
}
public class StudentStrategy implements IStrategy {
    public int playernum=0;
    /**
     * Method where students can observe the state of the system and schedule events to be executed.
     *
     * @param planets          The current state of the system.
     * @param planetOperations Helper methods students can use to interact with the system.
     * @param eventsToExecute  Queue students will add to in order to schedule events.
     */
    @Override
    public void takeTurn(List<IPlanet> planets, IPlanetOperations planetOperations, Queue<IEvent> eventsToExecute) {
        List<Eplanet> VisiblePlanets = new ArrayList<>();
        Queue<Eplanet> PotentialDonors = new LinkedList<>(); //long is the number of available extra populations (More than 1/2 of max population+enemy population right now)
        if(playernum ==0){// determine which player I am.
            for(IPlanet planet : planets) {
                if (planet instanceof IVisiblePlanet) {
                    if (((IVisiblePlanet) planet).isHomeworld()) {
                        if (((IVisiblePlanet) planet).getP1Population() > 0) {
                            playernum = 1;
                        } else {
                            playernum = 2;
                        }
                    }
                }
            }
        }
        if(playernum ==1){//player 1
            for (IPlanet planet : planets) {
                if (planet instanceof IVisiblePlanet) {
                    int Occupation = 0; //1 I own, 2 they own 3 joing 0 unoccupied
                    long popvalue = 0; //my pop-their pop
                    if (((IVisiblePlanet) planet).getP1Population()>0){
                        Occupation++;
                        popvalue +=((IVisiblePlanet) planet).getP1Population();
                    }
                    if(((IVisiblePlanet) planet).getP2Population()>0){
                        Occupation +=2;
                        popvalue -=((IVisiblePlanet) planet).getP2Population();
                    }
                    double value = ((IVisiblePlanet) planet).getHabitability(); //This is the priority I am assigning to a given planet. In the process of doing this I fiddled with many different ways of calculating it, but found habitibility worked best
                    if(Occupation ==0){ //more important if unoccupied
                        value *=1000;
                    }
                    else if (Occupation==1){ //less important if I already control it
                        value = value/3;
                    }
                    Set<IEdge> edges = ((IVisiblePlanet) planet).getEdges();
                    Eplanet currentplanet = new Eplanet(((IVisiblePlanet) planet), Occupation, value, popvalue, edges);
                    if(popvalue>0){
                        PotentialDonors.add(currentplanet); //adds to Queue of donors
                    }
                    VisiblePlanets.add(currentplanet); //adds to list of all visible planets
                }
            }
        }
        else if(playernum ==2){//player 2
            for (IPlanet planet : planets) {
                if (planet instanceof IVisiblePlanet) {
                    int Occupation = 0; //1 I own, 2 they own 3 joing 0 unoccupied
                    long popvalue = 0; //my pop-their pop
                    if (((IVisiblePlanet) planet).getP2Population()>0){
                        Occupation++;
                        popvalue +=((IVisiblePlanet) planet).getP2Population();
                    }
                    if(((IVisiblePlanet) planet).getP1Population()>0){
                        Occupation +=2;
                        popvalue -=((IVisiblePlanet) planet).getP1Population();
                    }
                    double value = ((IVisiblePlanet) planet).getHabitability(); //This is the priority I am assigning to a given planet. In the process of doing this I fiddled with many different ways of calculating it, but found habitibility worked best

                    if(Occupation ==0){ //more important if unoccupied
                        value *=3;
                    }
                    else if (Occupation==1){ //less important if I already control it uncontested
                        value = value/3;
                    }
                    Set<IEdge> edges = ((IVisiblePlanet) planet).getEdges();
                    Eplanet currentplanet = new Eplanet(((IVisiblePlanet) planet), Occupation, value, popvalue, edges);
                    if(popvalue>0){
                        PotentialDonors.add(currentplanet); //adds to Queue of donors
                    }
                    VisiblePlanets.add(currentplanet); //adds to list of all visible planets
                }

            }
        }
        //All of the above works, I am now setting up a new function to modify value based on adjacent planets.
        for(Eplanet planet1: VisiblePlanets) { //sets up nearby value. Goes to every edge and for each adjacent planet, if it is contested or onuccupied, add it to nearby value If I own it,  it gets ignored.
            double nearbyvalue = 0;
            for (IEdge currentedge : planet1.edges) {
                int edgeid = currentedge.getDestinationPlanetId();
                for (Eplanet planet2 : VisiblePlanets) { //find the destination.
                    if (planet2.planet.getId() == edgeid) {
                        if(planet2.occupied ==2 && planet2.occupied == 0){
                            nearbyvalue+=1;
                        }
                        else if (planet2.occupied ==3){
                            if(planet2.popvalue<((IVisiblePlanet) planet2.planet).getSize()/4){ //if I am winning by a landslide, ignore, otherwise send reinforcements.
                                nearbyvalue+=1;
                            }
                        }
                    }
                }
            }
            planet1.nearbyvalue=nearbyvalue;
        }
        for (Eplanet entry : PotentialDonors) { //for all donor planets (excess population)
            long sparepop = entry.popvalue;
            double totalvalue = entry.value; //starts at the planets value, so it isnt left with no population.
            Set<IEdge> edges = entry.edges; //finds all of the edges around it.
            Map<Eplanet, Double> maps= new HashMap<>(); //destination, value
            double negvalue = 0;
            for(IEdge currentedge: edges ){ //iterates through all edges
                int edgeid =currentedge.getDestinationPlanetId();
                for(Eplanet planet: VisiblePlanets){ //find the destination.
                    if(planet.planet.getId()==edgeid){
                        if(planet.occupied ==0){
                            maps.put(planet, 0.0);//if unocupied value =0 (overwritten)
                        }
                        else if(planet.occupied==1||(planet.occupied == 3 && (planet.popvalue>10))){ //if not contested at all, don't add to options
                            maps.put(planet, -planet.nearbyvalue); //This way it does not keep sending like a million people to a planet in that I already control
                            negvalue+=planet.nearbyvalue;  //If people remain, it will send them to occupied planets based on nearby value
                        }
                        else{//otherwise store value
                            maps.put(planet, planet.value);
                            totalvalue+=planet.value;
                        }
                    }
                }
            }
            long remainingpop=sparepop;
            for(Map.Entry<Eplanet, Double> mapentry:maps.entrySet()){ //iterate through the map we made, grabbing the destination planet and the value
                long sending = (long)Math.floor(mapentry.getValue()/totalvalue*sparepop);
                int test =0;
                if(mapentry.getValue()==0.0){ //if unoccupied
                    List<IShuttle> shuttles = mapentry.getKey().planet.getIncomingShuttles();
                    for(IShuttle shuttle: shuttles){
                        if(shuttle.getOwner()==Owner.SELF){
                            test=1;
                            break;
                        }
                    }
                    if(test ==0){ //if i already sent a shuttle, ignore, otherwise send 1.
                        sending =1;
                    }
                    else{
                        sending =0;
                    }
                }
                if(sending>0 && remainingpop>sending){ //never sends too many, or a negative number.
                    IEvent move = planetOperations.transferPeople(entry.planet, mapentry.getKey().planet, sending);
                    eventsToExecute.add(move);
                    remainingpop-=sending;
                    maps.remove(mapentry);
                }
            }
            if(remainingpop>0){ //IF there is still spare pop remaining, will send extra to already owned planets based on nearby value
                sparepop = remainingpop;
                for(Map.Entry<Eplanet, Double> mapentry:maps.entrySet()) {
                    long sending = (long) Math.floor(mapentry.getValue() / negvalue * sparepop); //evenly divides among remaining planets.
                    if (sending > 0 && remainingpop > sending) {
                        IEvent move = planetOperations.transferPeople(entry.planet, mapentry.getKey().planet, sending);
                        eventsToExecute.add(move);
                        remainingpop -= sending;
                    }
                }
            }
        }
    }

    @Override
    public String getName() {
        return "Mystrategy1"; //I don't want to compete
    }

    @Override
    public boolean compete() {
        return false;
    }
}
