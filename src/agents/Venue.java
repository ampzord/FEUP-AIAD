package agents;
import java.util.ArrayList;
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
    private ArrayList<String> wanted_bands;
    private ArrayList<Pair<String,Integer>> shows;
    private int location;

    @Override
    public String toString() {
        return "Venue" + this.getAID().getLocalName()
                + " Attendance=" + this.attendance
                + ", Budget=" + this.budget
                + ", Minimum Genre Spectrum=" + this.min_genre_spectrum
                + ", Maximum Genre Spectrum=" + this.max_genre_spectrum
                + ", Minimum Acceptable Prestige=" + this.min_acceptable_prestige
                + ", Maximum Attendance Prestige=" + this.max_acceptable_prestige
                + ", Location=" + this.location;
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
    }    public ArrayList<String> getWanted_bands() {
        return wanted_bands;
    }
    public void setWanted_bands(ArrayList<String> wanted_bands) {
        this.wanted_bands = wanted_bands;
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

        registerToDFService();
        searchBands();

        //TODO: perguntar 'as bandas as cenas
        //TODO: calcular quais bandas queremos
        //TODO: contratar bandas

        addBehaviour(new BandContractInitiator(this, new ACLMessage(ACLMessage.CFP)));
        System.out.println(getLocalName() + ": starting to work");
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

    class BandContractInitiator extends ContractNetInitiator {

        public BandContractInitiator(Agent a, ACLMessage msg) {
            super(a, msg);
        }

        protected Vector prepareCfps(ACLMessage cfp) {
            Vector v = new Vector();

            for(int i=0; i<available_bands.length; i++) {
                cfp.addReceiver(new AID(available_bands[i].getName().getLocalName(), false));
            }
            //attendance & min_genre_spectrum & max_genre_spectrum

            String content = attendance + "_" + min_genre_spectrum + "_" + max_genre_spectrum;
            cfp.setContent(content);

            v.add(cfp);

            return v;
        }

        protected void handleAllResponses(Vector responses, Vector acceptances) {

            System.out.println(getLocalName() + " got " + responses.size() + " responses!");

            //System.out.println("Resposta0: " + responses.get(0));

            try {
                ACLMessage a = (ACLMessage) responses.get(0);
                ACLMessage a1 = (ACLMessage) responses.get(1);
                System.out.println(a.getContent());
                System.out.println(a1.getContent());
            } catch (Exception e) {
                System.out.println("fuck");
            }
            //System.out.println("Resposta1: " + responses.get(1));
            //System.out.println("Resposta2: " + responses.get(2));

            for(int i=0; i<responses.size(); i++) {
                ACLMessage msg = ((ACLMessage) responses.get(i)).createReply();
                msg.setPerformative(ACLMessage.ACCEPT_PROPOSAL); // OR NOT!
                acceptances.add(msg);
            }
        }

        protected void handleAllResultNotifications(Vector resultNotifications) {
            System.out.println("got " + resultNotifications.size() + " result notifs!");
        }
    }
       
}