package agents;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.UnreadableException;
import jade.proto.ContractNetInitiator;
import javafx.util.Pair;
import utils.Utils;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.ArrayList;
import java.util.Vector;

public class Spectator extends Agent {

    public enum SpectatorBehaviour {
        MOSTBANDS, MOSTPRESTIGE, LEASTDISTANCE, LEASTCOST;
    }

    private int budget;
    private int min_genre_spectrum;
    private int max_genre_spectrum;
    private int location;
    private DFAgentDescription[] existent_venues;
    private SpectatorBehaviour behaviour;

    @Override
    public String toString() {
        return String.format("Spectator - %s, Budget=%4s, Min Genre Spectrum=%s, Max Genre Spectrum=%s, Location=%s,Behaviour=%s",
                this.getLocalName(), this.budget, this.min_genre_spectrum, this.max_genre_spectrum, this.location, this.behaviour);
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
    public DFAgentDescription[] getExistent_venues() {
        return existent_venues;
    }
    public void setExistent_venues(DFAgentDescription[] existent_venues){
        this.existent_venues = existent_venues;
    }

    private void setBehaviour(String name) {
        switch (name) {
            case "MOSTBANDS":
                behaviour = SpectatorBehaviour.MOSTBANDS;
                break;

            case "MOSTPRESTIGE":
                behaviour = SpectatorBehaviour.MOSTPRESTIGE;

            case "LEASTDISTANCE":
                behaviour = SpectatorBehaviour.LEASTDISTANCE;
                break;

            case "LEASTCOST":
                behaviour = SpectatorBehaviour.LEASTCOST;
                break;
        }
    }

    public void setup() {
        setSpectatorInformation();
        printSpectatorInformation();
        searchVenues();
        addBehaviour(new InitiateNegotiationWithVenue(this, new ACLMessage(ACLMessage.CFP)));
    }

    private void setSpectatorInformation() {
        setBudget((int)getArguments()[0]);
        setMin_genre_spectrum((int)getArguments()[1]);
        setMax_genre_spectrum((int)getArguments()[2]);
        setLocation((int)getArguments()[3]);
        setBehaviour((String) getArguments()[4]);
    }

    private void printSpectatorInformation() {
        System.out.println(this.toString());
    }

    private void registerToDFService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("spectator");
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch(FIPAException fe) {
            fe.printStackTrace();
        }
    }

    public void takeDown() {
        System.out.println(getLocalName() + ": done working");
    }

    private void searchVenues() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("venue");
        template.addServices(sd);
        try{
            DFAgentDescription[] result = DFService.search(this, template);
            setExistent_venues(result);
        } catch(FIPAException fe){
            fe.printStackTrace();
        }
    }

    /**
     * Start communication with Venue to decide which shows to view
     */
    class InitiateNegotiationWithVenue extends ContractNetInitiator {

        public InitiateNegotiationWithVenue(Agent a, ACLMessage msg) {
            super(a, msg);
        }

        protected Vector prepareCfps(ACLMessage cfp) {
            Vector v = new Vector();
            System.out.println();

            for (int i = 0; i < existent_venues.length; i++) {
                cfp.addReceiver(new AID(existent_venues[i].getName().getLocalName(), false));
                //System.out.println(getLocalName() + " - Sending Call For Proposal (CFP) to " + existent_venues[i].getName().getLocalName());
            }
            cfp.setContent(getLocalName() + " is the Venue ready?");
            v.add(cfp);
            return v;
        }

        protected void handleAllResponses(Vector responses, Vector acceptances) {
            //System.out.println("\n" + getLocalName() + " got " + responses.size() + " responses!");

            for (int i = 0; i < responses.size(); i++) {
                ACLMessage msg = ((ACLMessage) responses.get(i));

                switch (msg.getPerformative()) {

                    case ACLMessage.REFUSE:
                        if (Utils.DEBUG) {
                            System.out.println("SPECTATOR: " + getLocalName() + " got refuse from " + msg.getSender().getLocalName());
                            System.out.println("SPECTATOR: Restarting.");
                        }
                        break;

                    case ACLMessage.PROPOSE:
                        break;
                        /*
                        String[] show = msg.getContent().split("//");
                        for (int j = 0; j < show.length; j++) {
                            String[] tokens = show[i].split("::");
                        }
                        */
                }
            }
        }

        protected void handleAllResultNotifications(Vector resultNotifications) {
            if (Utils.DEBUG)
                System.out.println("Spectator: " + getLocalName() + " got " + resultNotifications.size() + " result notifications!");
        }
    }
}

