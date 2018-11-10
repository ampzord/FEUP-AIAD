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
        return  this.getAID().getLocalName()
                + " Budget=" + budget
                + ", Min Genre Spectrum=" + min_genre_spectrum
                + ", Max Genre Spectrum=" + max_genre_spectrum
                + ", Location=" + location
                + ", Behaviour=" + behaviour;
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
        if(Utils.DEBUG)
            printSpectatorInformation();

        searchVenues();

        //get venues of interest based on the bands they have playing
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

            if(Utils.DEBUG) {
                System.out.println("Spectator " + getLocalName() + " found:");
                for(int i = 0; i < result.length; i++){
                    System.out.println("    " + result[i].getName().getLocalName());
                }
            }

            setExistent_venues(result);
        } catch(FIPAException fe){
            fe.printStackTrace();
        }
    }

    class InitiateNegotiationWithVenue extends ContractNetInitiator {

        public InitiateNegotiationWithVenue(Agent a, ACLMessage msg) {
            super(a, msg);
        }

        protected Vector prepareCfps(ACLMessage cfp) {
            Vector v = new Vector();
            System.out.println();

            for (int i = 0; i < existent_venues.length; i++) {
                cfp.addReceiver(new AID(existent_venues[i].getName().getLocalName(), false));
                System.out.println(getLocalName() + " - Sending Call For Proposal (CFP) to " + existent_venues[i].getName().getLocalName());
            }
            cfp.setContent(getLocalName() + " is the Venue ready?");
            v.add(cfp);
            return v;
        }


        protected void handleAllResponses(Vector responses, Vector acceptances) {
            System.out.println("\n" + getLocalName() + " got " + responses.size() + " responses!");
/*
            for(int i=0; i<responses.size(); i++) {
                ACLMessage msg = ((ACLMessage) responses.get(i)).createReply();
                //msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL); // OR NOT!
                System.out.println("qwerty" + msg.getContent());
            }
*/

            /*
            for (int i = 0; i < responses.size(); i++) {
                ACLMessage rsp = (ACLMessage) responses.get(i);


                try {
                    System.out.println("objecto desta merda e' : " + rsp.getContentObject().getClass());
                    Pair<Integer, ArrayList<ArrayList<Object>>> string = (Pair<Integer, ArrayList<ArrayList<Object>>>) rsp.getContentObject();
                    //System.out.println("TYPE OF RSP: " + rsp.getClass().toString());
                    System.out.println("OLA: " + string.getKey());
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
            }
*/


            /*
            if (!rsp.getContent().equals("Venue not ready yet.")) {
                System.out.println("ENTROU CARALHO");
                try {
                    Pair<Integer, ArrayList<ArrayList<Object>>> string = (Pair<Integer, ArrayList<ArrayList<Object>>>) rsp.getContentObject();
                    System.out.println("OLA: " + string.getValue().get(0).get(0).toString());
                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
            }
            else {
                System.out.println(rsp.getContent());
            }*/
/*

            ACLMessage rsp = (ACLMessage) responses.get(0);
            String string = rsp.getContent();
            if (!string.equals("Venue not ready yet.")) {
                System.out.println("ENTROU");
                Pair<Integer, ArrayList<ArrayList<Object>>> content = responses.get(0).getContentObject();
                for (int i = 0; i < content.getValue().size(); i++) {
                    System.out.println("RESPONSE CONTENT - " + content.getValue().get(i).get(0).toString());
                }
            }
            */


            //Pair<Integer, ArrayList<ArrayList<Object>>> content = (Pair<Integer, ArrayList<ArrayList<Object>>>) responses.get(0);


            }
            /*
            for (int i=0; i<responses.size(); i++) {
                ACLMessage rsp = (ACLMessage) responses.get(0);
                String string = rsp.getContent();
                if (!string.equals("Your proposal doesn't fit our requirements")) {
                    String[] tokens = string.split("::");
                    int min_price = Integer.parseInt(tokens[2]);

                    if (min_price <= budget)
                        possible_bands.add(rsp);
                }
            }*/

            // TODO: algoritmo para escolher melhores shows
            // placeholder price

        /*String string = "Iron Maiden::5000";
            venue_proposal.add(string);
            String[] tokens = string.split("::");
            String bandName = tokens[0];
            int price = Integer.parseInt(tokens[1]);

            //reply
            for(int i=0; i<responses.size(); i++) {
                ACLMessage rsp = (ACLMessage) responses.get(i);
                if (rsp.getSender().getLocalName().equals(bandName)) {
                    ACLMessage msg = ((ACLMessage) responses.get(i)).createReply();
                    msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL); // OR NOT!
                    msg.setContent(venue_proposal.get(0));
                    acceptances.add(msg);
                } else {
                    ACLMessage msg = ((ACLMessage) responses.get(i)).createReply();
                    msg.setPerformative(ACLMessage.REJECT_PROPOSAL); // OR NOT!
                    acceptances.add(msg);
                }
            }
        }*/

        protected void handleAllResultNotifications(Vector resultNotifications) {
            System.out.println("got " + resultNotifications.size() + " result notifs!");
        }
    }
}

