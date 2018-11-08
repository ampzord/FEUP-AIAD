package agents;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import javafx.util.Pair;

public class Venue extends Agent {

    public enum VenueBehaviour {
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
    private VenueBehaviour behaviour;



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

        addBehaviour(new ShowConfirmations(this, MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
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
                behaviour = VenueBehaviour.MOSTBANDS;
                break;

            case "MOSTPROFIT":
                behaviour = VenueBehaviour.MOSTPROFIT;
                break;

            case "MOSTPRESTIGE":
                behaviour = VenueBehaviour.MOSTPRESTIGE;
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
                System.out.println(getLocalName() + " - Sending Request to " + available_bands[i].getName().getLocalName());
            }

            msg.setOntology("Give_BusinessCard");
            String content = attendance + "::" + min_genre_spectrum + "::" + max_genre_spectrum;
            msg.setContent(content);

            v.add(msg);

            return v;
        }

        protected void handleAgree(ACLMessage agree) {

        }

        protected void handleRefuse(ACLMessage refuse) {
            //System.out.println(getLocalName() + " received refuse from " + refuse.getSender().getLocalName());
            requests_done++;

            if (available_bands.length == requests_done) {
                /* compute the best bands to hire */
                addBehaviour(new HireBands((Venue)getAgent()));
                requests_done = 0;
            }
        }

        protected void handleInform(ACLMessage inform) {
            if (inform.getOntology().equals("Give_BusinessCard")) {
                System.out.println(getLocalName() + " received \"Give_BusinessCard\" INFORM " + inform.getContent() + " from " + inform.getSender().getLocalName());
                possible_bands.add(inform);
                requests_done++;

                if (available_bands.length == requests_done) {
                    /* compute the best bands to hire */
                    addBehaviour(new HireBands((Venue)getAgent()));
                    requests_done = 0;
                }
            }
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
        boolean flag;

        public HireBands(Venue v) {
            flag = false;
        }

        @Override
        public void action() {
            //TODO: algoritmo para escolher as melhores bandas a contratar (e perceber como caralho funciona um behaviour)

            switch(behaviour){
                case MOSTBANDS:
                    //get most bands
                    getMostBandsBehaviour();
                    break;

                case MOSTPRESTIGE:
                    //get mostprestige bands
                    getMostPrestigeBehaviour();
                    break;

                default:
                    break;
            }

            flag = true;
        }

        @Override
        public boolean done() {
            return flag;
        }

        @Override
        public int onEnd() {
            addBehaviour(new RequestContract(venue, null));

            return 0;
        }

    }

    private void getMostBandsBehaviour() {

        ArrayList<ACLMessage> ordered_possible_bands = possible_bands;
        sortBandsByPrice(ordered_possible_bands);

        Collections.reverse(ordered_possible_bands);

        calculateBestBandsInMostBandsBehaviour(ordered_possible_bands);

        for(ACLMessage message : venue_proposal) {
            System.out.println(message.getContent());
        }

    }

    private void calculateBestBandsInMostBandsBehaviour(ArrayList<ACLMessage> possible_bands) {
        int remainder_budget = this.budget;

        for (int i = 0; i < possible_bands.size(); i++) {
            String[] content = possible_bands.get(i).getContent().split("::");
            int min_price = Integer.parseInt(content[2]);
            ACLMessage message = possible_bands.get(i);

            if (remainder_budget > min_price) {
                remainder_budget -= min_price;
                possible_bands.get(i).setContent(Integer.toString(min_price));
            }
            else {
                possible_bands.get(i).setContent("0");
            }
            venue_proposal.add(possible_bands.get(i));
        }

    }

    private void getMostPrestigeBehaviour() {
        //buscar ao possible bands e guardo no venue proposals, no venue proposal ACL message mudo o min_price para preco qe quero pagar
        ArrayList<ACLMessage> possible_bands_ordered_by_prestige = new ArrayList<>();
        possible_bands_ordered_by_prestige = possible_bands;

        sortBandsByBestRating(possible_bands_ordered_by_prestige);

        calculateBestBandsInMostPrestigeBehaviour(possible_bands_ordered_by_prestige);
    }

    private void sortBandsByBestRating(ArrayList<ACLMessage> array)
    {
        int n = array.size();
        for (int i = 0; i < n-1; i++)
            for (int j = 0; j < n-i-1; j++) {
                String[] content1 = array.get(j).getContent().split("::");
                String[] content2 = array.get(j+1).getContent().split("::");

                int rating1 = Integer.parseInt(content1[1]);
                int rating2 = Integer.parseInt(content2[1]);
                int min_preco1 = Integer.parseInt(content1[2]);
                int min_preco2 = Integer.parseInt(content2[2]);

                if (rating1 < rating2)
                {
                    ACLMessage temp = array.get(j);
                    array.set(j, array.get(j+1));
                    array.set(j+1, temp);
                }
                else if (rating1 == rating2) {
                    if (min_preco1 > min_preco2)
                    {
                        ACLMessage temp = array.get(j);
                        array.set(j, array.get(j+1));
                        array.set(j+1, temp);
                    }
                }
            }
    }

    private void sortBandsByPrice(ArrayList<ACLMessage> array)
    {
        int n = array.size();
        for (int i = 0; i < n-1; i++)
            for (int j = 0; j < n-i-1; j++) {
                String[] content1 = array.get(j).getContent().split("::");
                String[] content2 = array.get(j+1).getContent().split("::");
                int rating1 = Integer.parseInt(content1[1]);
                int rating2 = Integer.parseInt(content2[1]);
                int min_preco1 = Integer.parseInt(content1[2]);
                int min_preco2 = Integer.parseInt(content2[2]);


                if (min_preco1 < min_preco2)
                {
                    ACLMessage temp = array.get(j);
                    array.set(j, array.get(j+1));
                    array.set(j+1, temp);
                }
                else if (min_preco1 == min_preco2) {
                    if (rating1 < rating2)
                    {
                        ACLMessage temp = array.get(j);
                        array.set(j, array.get(j+1));
                        array.set(j+1, temp);
                    }
                }
            }
    }

    private void calculateBestBandsInMostPrestigeBehaviour(ArrayList<ACLMessage> array) {
        int remainder_budget = this.budget;
        int total_prestige_score = 0;

        ArrayList<ACLMessage> tmp = new ArrayList<>();

        for (int i = 0; i < array.size(); i++) {
            String[] content = array.get(i).getContent().split("::");
            int prestige = Integer.parseInt(content[1]);
            int min_price = Integer.parseInt(content[2]);

            if (remainder_budget > min_price) {
                remainder_budget -= min_price;
                total_prestige_score += prestigeScore(prestige);
            }
            else {
                array.get(i).setContent("0");
            }
            tmp.add(array.get(i));
        }

        //System.out.println("Remainder Budget after: " + remainder_budget);

        for (int i = 0; i < tmp.size(); i++) {

            if (tmp.get(i).getContent().equals("0")) {
                ACLMessage message = tmp.get(i);
                venue_proposal.add(message);
                continue;
            }

            double bonus_money_per_band = 0;
            String[] content = tmp.get(i).getContent().split("::");
            int prestige_score = prestigeScore(Integer.parseInt(content[1]));
            int min_price = Integer.parseInt(content[2]);
            double payment_money = min_price;

            if (remainder_budget > 0 && total_prestige_score > 0) {
                bonus_money_per_band = Math.floor((prestige_score / (double) total_prestige_score) * remainder_budget);
                payment_money = Math.floor(bonus_money_per_band + min_price);
            }

            int final_value = (int) Math.round(payment_money);
            ACLMessage message = tmp.get(i);
            message.setContent(Integer.toString(final_value));
            venue_proposal.add(message);
        }

        for (ACLMessage message : venue_proposal) {
            System.out.println("Message sent:" + message.getContent());
        }
    }

    private int prestigeScore(int prestige) {
        return prestige*prestige;
    }


    /**
     *  Request Contract to a Band
     */
    class RequestContract extends AchieveREInitiator {

        boolean someBandRefused;

        public RequestContract(Agent a, ACLMessage msg) {
            super(a, msg);
            someBandRefused = false;
        }

        protected Vector<ACLMessage> prepareRequests(ACLMessage msg) {
            Vector<ACLMessage> v = new Vector<>();

            System.out.println();
            for(int i=0; i<venue_proposal.size(); i++) {
                ACLMessage m = new ACLMessage(ACLMessage.REQUEST);
                m.addReceiver(new AID(venue_proposal.get(i).getSender().getLocalName(), false));
                //System.out.println(getLocalName() + " hiring " + venue_proposal.get(i).getSender().getLocalName() + " for " + venue_proposal.get(i).getContent());
                m.setOntology("Hiring");
                m.setContent(venue_proposal.get(i).getContent());
                v.add(m);
            }

            return v;
        }

        protected void handleAgree(ACLMessage agree) {
        }

        protected void handleRefuse(ACLMessage refuse) {
            if (refuse.getOntology().equals("Hiring")) {
                requests_done++;
                if (venue_proposal.size() == requests_done) {
                    /* DESPERATION BEHAVIOUR */
                    //addBehaviour(new BandGetter((Venue)getAgent(), new ACLMessage(ACLMessage.REQUEST)));
                    requests_done = 0;
                }
            }
        }

        protected void handleInform(ACLMessage inform) {
            if (inform.getOntology().equals("Hiring")) {
                requests_done++;

                System.out.println(getLocalName() + " received INFORM " + inform.getContent() + " from " + inform.getSender().getLocalName());
                possible_bands.add(inform);
            }
        }

        protected void handleFailure(ACLMessage failure) {
            // nothing to see here
        }

    }


    /**
     *  Show request responder
     */
    class ShowConfirmations extends AchieveREResponder {

        public ShowConfirmations(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        protected ACLMessage handleRequest(ACLMessage request) throws RefuseException {

            //System.out.println(getLocalName() + " received " + request.getContent() + " from " + request.getSender().getLocalName());
            ACLMessage reply = request.createReply();

            switch (request.getOntology()) {
                case "Give_BusinessCard":
                    break;
            }

            return reply;
        }

    }
}