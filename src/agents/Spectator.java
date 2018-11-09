package agents;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;

public class Spectator extends Agent {

    private int budget;
    private int min_genre_spectrum;
    private int max_genre_spectrum;
    private int location;
    private DFAgentDescrpition[] available_spectators;

    @Override
    public String toString() {
        return  this.getAID().getLocalName()
                + " Budget=" + budget
                + ", Min Genre Spectrum=" + min_genre_spectrum
                + ", Max Genre Spectrum=" + max_genre_spectrum
                + ", Location=" + location;
    }

    public int getBudget() {
        return budget;
    }
    public void setBudget(int budget) {
        this.budget = budget;
    }
    public int getMin_genre_spectrum() {
        return min_genre_spectrum;
    }
    public void setMin_genre_spectrum(int min_genre_spectrum) {
        this.min_genre_spectrum = min_genre_spectrum;
    }
    public int getMax_genre_spectrum() {
        return max_genre_spectrum;
    }
    public void setMax_genre_spectrum(int max_genre_spectrum) {
        this.max_genre_spectrum = max_genre_spectrum;
    }
    public int getLocation() {
        return location;
    }
    public void setLocation(int location) {
        this.location = location;
    }
    public DFAgentDescription[] getAvailable_venues() {
        return setAvailable_venues;
    }
    public void setAvailable_venues(DFAgentDescription[] getAvailable_venues){
        this.available_venues = available_venues;
    }

    public void setup() {
        setSpectatorInformation();
        printSpectatorInformation();

        searchVenues();

        //get venues of interes based on the bands they have playing
        addBehaviour(new VenueGetter(this, new ACLMessage(ACLMessage.REQUEST)));
    }

    private void setSpectatorInformation() {
        setBudget((int)getArguments()[0]);
        setMin_genre_spectrum((int)getArguments()[1]);
        setMax_genre_spectrum((int)getArguments()[2]);
        setLocation((int)getArguments()[3]);
    }

    private void printSpectatorInformation() {
        System.out.println(this.toString());
    }


    public void takeDown() {
        System.out.println(getLocalName() + ": done working");
    }

    private void searchVenues() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("spectator");
        template.addServices(sd);
        try{
            DFAgentDescription[] result = DFService.search(this, template);

            if(Utils.DEBUG) {
                System.out.println("Spectator " + getLocalName() + " found:");
                for(int i = 0; i < result.length; i++){
                    System.out.println("    " + result[i].getName().getLocalName());
                }
            }

            setAvailable_venues(result);
        } catch(FIPAException fe){
            fe.printStackTrace();
            }
    }

    class VenueGetter extends Behaviour {
        public void action() {
        }
        public boolean done() {
            return true;
        }
    }

}

