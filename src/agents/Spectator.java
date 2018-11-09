package agents;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import utils.Utils;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.proto.AchieveREInitiator;

import java.util.Vector;

public class Spectator extends Agent {

    private int budget;
    private int min_genre_spectrum;
    private int max_genre_spectrum;
    private int location;
    private DFAgentDescription[] available_venues;

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
        return available_venues;
    }
    public void setAvailable_venues(DFAgentDescription[] getAvailable_venues){
        this.available_venues = available_venues;
    }

    public void setup() {
        setSpectatorInformation();
        if(Utils.DEBUG)
            printSpectatorInformation();

        searchVenues();

        //get venues of interest based on the bands they have playing
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

    class VenueGetter extends AchieveREInitiator {

        public VenueGetter(Agent a, ACLMessage msg) {
            super(a, msg);
        }

        protected Vector<ACLMessage> prepareRequests(ACLMessage msg) {
            Vector<ACLMessage> v = new Vector<>();

            if(Utils.DEBUG) {
                System.out.println();
                for (int i = 0; i < available_venues.length; i++) {
                    msg.addReceiver(new AID(available_venues[i].getName().getLocalName(), false));
                    System.out.println(getLocalName() + " - Sending Request to " + available_venues[i].getName().getLocalName());
                }
            }

            msg.setOntology("Give_Preferences");
            String content = budget + "::" + min_genre_spectrum + "::" + max_genre_spectrum;
            msg.setContent(content);

            v.add(msg);

            return v;
        }

        protected void handleAgree(ACLMessage agree) {
        }

        protected void handleRefuse(ACLMessage refuse) {
        }

        protected void handleInform(ACLMessage inform) {
        }

        protected void handleFailure(ACLMessage failure) {
            // nothing to see here
        }

    }

}

