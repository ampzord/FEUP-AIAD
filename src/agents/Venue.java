package agents;
import java.util.ArrayList;
import java.util.Vector;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.proto.ContractNetInitiator;
import javafx.util.Pair;
import utils.Utils;

import java.util.Random;

public class Venue extends Agent {

    public enum VenueBehaviours {
        MOSTBANDS, MOSTPRESTIGE, MOSTPROFIT;
    }
   
    private int attendance;
    private int budget;
    private int min_genre_spectrum;
    private int max_genre_spectrum;
    private int min_acceptable_prestige;
    private int max_acceptable_prestige;
    private DFAgentDescription[] available_bands;
    private ArrayList<ACLMessage> possible_bands;
    private ArrayList<ACLMessage> venue_proposal;
    private ArrayList<Pair<String,Integer>> shows;
    private int location;
    private int requests_done;
    private VenueBehaviours behaviour;



    @Override
    public String toString() {
        return String.format("Venue - %1$-15s", this.getAID().getLocalName())
                + String.format(" Attendance=%s, Budget=%s, Min Genre Spectrum=%s, Max Genre Spectrum=%s, Min Accept Prestige=%s, Max Accept Prestige=%s, Location=%s, Behaviour=%s",
                this.attendance, this.budget, this.min_genre_spectrum, this.max_genre_spectrum, this.min_acceptable_prestige, this.max_acceptable_prestige, this.location, this.behaviour);
    }

    public void setAttendance(int attendance) {
        this.attendance = attendance;
    }
    public int getAttendance() {
        return this.attendance;
    }
    public void setBudget(int budget) {
        this.budget = budget;
    }
    public int getBudget() {
        return this.budget;
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
    public int getMin_acceptable_prestige() {
        return min_acceptable_prestige;
    }
    public void setMin_acceptable_prestige(int min_acceptable_prestige) {
        this.min_acceptable_prestige = min_acceptable_prestige;
    }
    public int getMax_acceptable_prestige() {
        return max_acceptable_prestige;
    }
    public void setMax_acceptable_prestige(int max_acceptable_prestige) {
        this.max_acceptable_prestige = max_acceptable_prestige;
    }
    public DFAgentDescription[] getAvailable_bands() {
        return available_bands;
    }
    public void setAvailable_bands(DFAgentDescription[] available_bands) {
        this.available_bands = available_bands;
    }
    public ArrayList<ACLMessage> getPossible_bands() {
        return possible_bands;
    }
    public void setPossible_bands(ArrayList<ACLMessage> possible_bands) {
        this.possible_bands = possible_bands;
    }
    public ArrayList<ACLMessage> getVenue_proposal() {
        return venue_proposal;
    }
    public void setVenue_proposal(ArrayList<ACLMessage> venue_proposal) {
        this.venue_proposal = venue_proposal;
    }
    public ArrayList<Pair<String, Integer>> getShows() {
        return shows;
    }
    public void setShows(ArrayList<Pair<String, Integer>> shows) {
        this.shows = shows;
    }
    public int getLocation() {
        return location;
    }
    public void setLocation(int location) {
        this.location = location;
    }


    public void setup() {
        setVenueInformation();
        printVenueInformation();
        registerToDFService();

        searchBands();

        //get interested bands
        addBehaviour(new BandGetter(this, new ACLMessage(ACLMessage.REQUEST)));

        /*
        //hire bands
        addBehaviour(new BandContractInitiator(this, new ACLMessage(ACLMessage.CFP)));
        //System.out.println(getLocalName() + ": starting to work");
        */
    }

    private void setVenueInformation() {
        setAttendance((int)getArguments()[1]);
        setBudget((int)getArguments()[2]);
        setMin_genre_spectrum((int)getArguments()[3]);
        setMax_genre_spectrum((int)getArguments()[4]);
        setMin_acceptable_prestige((int)getArguments()[5]);
        setMax_acceptable_prestige((int)getArguments()[6]);
        setLocation((int)getArguments()[7]);
        setBehaviour((String) getArguments()[8]);
        possible_bands = new ArrayList<>();
        venue_proposal = new ArrayList<>();
        shows = new ArrayList<>();

        requests_done = 0;
    }

    private void setBehaviour(String name) {
        switch (name) {
            case "MOSTBANDS":
                behaviour = VenueBehaviours.MOSTBANDS;
                break;

            case "MOSTPROFIT":
                behaviour = VenueBehaviours.MOSTPROFIT;
                break;

            case "MOSTPRESTIGE":
                behaviour = VenueBehaviours.MOSTPRESTIGE;
                break;
        }
    }

    private void printVenueInformation() {
        System.out.println(this.toString());
    }

    private void searchBands() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("band");
        template.addServices(sd);
        try {
            DFAgentDescription[] result = DFService.search(this, template);

            /*
            System.out.println("Venue " + getLocalName() + " found:");
            for(int i=0; i<result.length; ++i) {
                System.out.println("    " + result[i].getName().getLocalName());
            }
            */

            setAvailable_bands(result);
        } catch(FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private void registerToDFService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("venue");
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch(FIPAException fe) {
            fe.printStackTrace();
        }
    }
   
    public void takeDown() {
        unregisterFromDFService();

        System.out.println(getLocalName() + ": done working");
    }

    private void unregisterFromDFService() {
        try {
            DFService.deregister(this);
        } catch(FIPAException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Band Getter
     */
    class BandGetter extends AchieveREInitiator {

        public BandGetter(Agent a, ACLMessage msg) {
            super(a, msg);
        }

        protected Vector<ACLMessage> prepareRequests(ACLMessage msg) {
            Vector<ACLMessage> v = new Vector<>();

            System.out.println();
            for(int i=0; i<available_bands.length; i++) {
                msg.addReceiver(new AID(available_bands[i].getName().getLocalName(), false));
                //System.out.println(getLocalName() + " - Sending Request to " + available_bands[i].getName().getLocalName());
            }

            String content = attendance + "::" + min_genre_spectrum + "::" + max_genre_spectrum;
            msg.setContent(content);

            v.add(msg);

            return v;
        }

        protected void handleAgree(ACLMessage agree) {
            //System.out.println(getLocalName() + " received agree from " + agree.getSender().getLocalName());
            requests_done++;

            if (available_bands.length == requests_done) {
                /* compute the best bands to hire */
                addBehaviour(new HireBands((Venue)getAgent()));
            }
        }

        protected void handleRefuse(ACLMessage refuse) {
            //System.out.println(getLocalName() + " received refuse from " + refuse.getSender().getLocalName());
            requests_done++;

            if (available_bands.length == requests_done) {
                /* compute the best bands to hire */
                addBehaviour(new HireBands((Venue)getAgent()));
            }
        }

        protected void handleInform(ACLMessage inform) {
            System.out.println(getLocalName() + " received INFORM " + inform.getContent() + " from " + inform.getSender().getLocalName());
            possible_bands.add(inform);
        }

        protected void handleFailure(ACLMessage failure) {
            // nothing to see here
        }

    }


    /*
     *   Band Hirer
     * */
    class HireBands extends Behaviour {

        Venue venue;

        public HireBands(Venue v) {
            venue = v;
            Random rand = new Random();

        }

        @Override
        public void action() {
            //TODO: algoritmo para escolher as melhores bandas a contratar (e perceber como caralho funciona um behaviour)

            switch(behaviour){
                case MOSTBANDS:
                    //get most bands
                    //getMostBandsBehaviour(this);
                    break;

                case MOSTPRESTIGE:
                    /*
                    //get bands with most prestige
                    while(venue.getBudget() > 0){

                        int max = 0;
                        for(int i = 0; i < venue.getPossible_bands().size(); i++){

                            String tokens[] = venue.getPossible_bands().get(i).getContent().split("::");

                            if(venue.getPossible_bands().get(i).getContent() > max){
                                max = venue.getPossible_bands().get(i).getContent();
                            }
                        }
                        return max;

                    }
                    */
                    break;

                default:
                    break;
            }
        }

        @Override
        public boolean done() {
            addBehaviour(new RequestContract(venue, new ACLMessage(ACLMessage.REQUEST)));

            return true;
        }

    }

    /**
     *  Request Contract to a Band
     */
    class RequestContract extends AchieveREInitiator {

        public RequestContract(Agent a, ACLMessage msg) {
            super(a, msg);
        }

        protected Vector<ACLMessage> prepareRequests(ACLMessage msg) {
            Vector<ACLMessage> v = new Vector<>();

            System.out.println();
            for(int i=0; i<available_bands.length; i++) {
                msg.addReceiver(new AID(available_bands[i].getName().getLocalName(), false));
                //System.out.println(getLocalName() + " - Sending Request to " + available_bands[i].getName().getLocalName());
            }

            String content = attendance + "::" + min_genre_spectrum + "::" + max_genre_spectrum;
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

        }

    }


    /*
    *   Contract Net
    * */
    class BandContractInitiator extends ContractNetInitiator {

        public BandContractInitiator(Agent a, ACLMessage msg) {
            super(a, msg);
        }

        protected Vector prepareCfps(ACLMessage cfp) {
            Vector v = new Vector();
            System.out.println();
            for(int i=0; i<available_bands.length; i++) {
                cfp.addReceiver(new AID(available_bands[i].getName().getLocalName(), false));
                System.out.println(getLocalName() + " - Sending Call For Proposal (CFP) to " + available_bands[i].getName().getLocalName());
            }

            String content = attendance + "::" + min_genre_spectrum + "::" + max_genre_spectrum;
            cfp.setContent(content);

            v.add(cfp);

            return v;
        }

        protected void handleAllResponses(Vector responses, Vector acceptances) {

            System.out.println("\n" + getLocalName() + " got " + responses.size() + " responses!");

            for (int i=0; i<responses.size(); i++) {
                ACLMessage rsp = (ACLMessage) responses.get(0);
                String string = rsp.getContent();
                if (!string.equals("Your proposal doesn't fit our requirements")) {
                    String[] tokens = string.split("::");
                    int min_price = Integer.parseInt(tokens[2]);

                    if (min_price <= budget)
                        possible_bands.add(rsp);
                }
            }

            // fazer aqui algoritmo para escolher melhores shows
            // placeholder price
            String string = "Iron Maiden::5000";
            //venue_proposal.add(string);
            String[] tokens = string.split("::");
            String bandName = tokens[0];
            int price = Integer.parseInt(tokens[1]);

            //reply
            for(int i=0; i<responses.size(); i++) {
                ACLMessage rsp = (ACLMessage) responses.get(i);
                if (rsp.getSender().getLocalName().equals(bandName)) {
                    ACLMessage msg = ((ACLMessage) responses.get(i)).createReply();
                    msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL); // OR NOT!
                    //msg.setContent(venue_proposal.get(0));
                    acceptances.add(msg);
                } else {
                    ACLMessage msg = ((ACLMessage) responses.get(i)).createReply();
                    msg.setPerformative(ACLMessage.REJECT_PROPOSAL); // OR NOT!
                    acceptances.add(msg);
                }
            }
        }

        protected void handleAllResultNotifications(Vector resultNotifications) {
            System.out.println("got " + resultNotifications.size() + " result notifs!");
        }
    }
       
}