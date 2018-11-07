package agents;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Vector;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import javafx.util.Pair;

public class Venue extends Agent {
   
    private int attendance;
    private int budget;
    private int min_genre_spectrum;
    private int max_genre_spectrum;
    private int min_acceptable_prestige;
    private int max_acceptable_prestige;
    private DFAgentDescription[] available_bands;
    private ArrayList<ACLMessage> possible_bands;
    private ArrayList<String> venue_proposal;
    private ArrayList<Pair<String,Integer>> shows;
    private int location;

    @Override
    public String toString() {
        return String.format("Venue - %1$-15s", this.getAID().getLocalName())
                + String.format(" Attendance=%s, Budget=%s, Min Genre Spectrum=%s, Max Genre Spectrum=%s, Min Accept Prestige=%s, Max Accept Prestige=%s, Location=%s",
                this.attendance, this.budget, this.min_genre_spectrum, this.max_genre_spectrum, this.min_acceptable_prestige, this.max_acceptable_prestige, this.location);
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
    public ArrayList<String> getVenue_proposal() {
        return venue_proposal;
    }
    public void setVenue_proposal(ArrayList<String> venue_proposal) {
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

        //TODO: perguntar 'as bandas as cenas
        //TODO: calcular quais bandas queremos
        //TODO: contratar bandas

        addBehaviour(new BandContractInitiator(this, new ACLMessage(ACLMessage.CFP)));
        //System.out.println(getLocalName() + ": starting to work");

    }

    private void setVenueInformation() {
        setAttendance((int)getArguments()[1]);
        setBudget((int)getArguments()[2]);
        setMin_genre_spectrum((int)getArguments()[3]);
        setMax_genre_spectrum((int)getArguments()[4]);
        setMin_acceptable_prestige((int)getArguments()[5]);
        setMax_acceptable_prestige((int)getArguments()[6]);
        possible_bands = new ArrayList<>();
        venue_proposal = new ArrayList<>();
        shows = new ArrayList<>();
        setLocation((int)getArguments()[7]);
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

    /*
    *   Contact all bands to start decisions
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
                if (!responses.get(i).equals("Your proposal doesn't fit our requirements")) {
                    ACLMessage rsp = (ACLMessage) responses.get(0);

                    String string = rsp.getContent();
                    String[] tokens = string.split("::");
                    int min_price = Integer.parseInt(tokens[2]);

                    if (min_price <= budget)
                        possible_bands.add(rsp);
                }
            }

            // TODO: algoritmo para escolher melhores shows
            // placeholder price
            String string = "Iron Maiden::5000";
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
        }

        protected void handleAllResultNotifications(Vector resultNotifications) {
            System.out.println("got " + resultNotifications.size() + " result notifs!");
        }
    }
       
}