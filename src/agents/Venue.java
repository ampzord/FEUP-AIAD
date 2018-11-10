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
import jade.proto.ContractNetResponder;
import utils.Utils;

public class Venue extends Agent {

    public enum VenueBehaviour {
        MOSTBANDS, MOSTPRESTIGE, MOSTPROFIT;
    }

    private int capacity;
    private int budget;
    private int min_genre_spectrum;
    private int max_genre_spectrum;
    private int min_acceptable_prestige;
    private int max_acceptable_prestige;
    private DFAgentDescription[] available_bands;
    private ArrayList<ACLMessage> possible_bands;
    private ArrayList<ACLMessage> venue_proposal;
    private ArrayList<ArrayList<Object>> shows;
    private int location;
    private int requests_done;
    private int band_responses;
    private VenueBehaviour behaviour;
    private boolean received_refusal;
    private boolean line_up_ready;
    private Behaviour band_getter, show_confirmations, hire_bands, request_contract;

    @Override
    public String toString() {
        return String.format("Venue - %1$-15s", this.getAID().getLocalName())
                + String.format(" Attendance=%s, Budget=%s, Min Genre Spectrum=%s, Max Genre Spectrum=%s, Min Accept Prestige=%s, Max Accept Prestige=%s, Location=%s, Behaviour=%s",
                this.capacity, this.budget, this.min_genre_spectrum, this.max_genre_spectrum, this.min_acceptable_prestige, this.max_acceptable_prestige, this.location, this.behaviour);
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    public int getCapacity() {
        return this.capacity;
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
    public ArrayList<ArrayList<Object>> getShows() {
        return shows;
    }
    public void setShows(ArrayList<ArrayList<Object>> shows) {
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
        if(Utils.DEBUG)
            printVenueInformation();
        registerToDFService();

        searchBands();

        startBehaviours();
    }

    private void startBehaviours() {
        band_getter = new BandGetter(this, new ACLMessage(ACLMessage.REQUEST));
        addBehaviour(band_getter);

        show_confirmations = new ShowConfirmations(this, MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        addBehaviour(show_confirmations);
    }

    private void retry () {
        band_getter.block();
        show_confirmations.block();
        hire_bands.block();
        request_contract.block();

        removeBehaviour(band_getter);
        removeBehaviour(show_confirmations);
        removeBehaviour(hire_bands);
        removeBehaviour(request_contract);

        startBehaviours();
    }

    private void setVenueInformation() {
        setCapacity((int)getArguments()[1]);
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
        received_refusal = false;
        line_up_ready = false;
        requests_done = 0;
        band_responses = 0;
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

            if(Utils.DEBUG){
                System.out.println("Venue " + getLocalName() + " found:");
                for(int i=0; i<result.length; ++i) {
                    System.out.println("    " + result[i].getName().getLocalName());
                }
            }

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
            for (int i = 0; i < available_bands.length; i++) {
                msg.addReceiver(new AID(available_bands[i].getName().getLocalName(), false));
                if(Utils.DEBUG)
                    System.out.println(getLocalName() + " - Sending Request to " + available_bands[i].getName().getLocalName());
            }



            msg.setOntology("Give_BusinessCard");
            String content = capacity + "::" + min_genre_spectrum + "::" + max_genre_spectrum;
            msg.setContent(content);

            v.add(msg);

            return v;
            }

        protected void handleAgree(ACLMessage agree) {
        }

        protected void handleRefuse(ACLMessage refuse) {
            if(Utils.DEBUG)
                System.out.println(getLocalName() + " received refuse from " + refuse.getSender().getLocalName());
            requests_done++;

            if (available_bands.length == requests_done) {
                System.out.println("VENUE: " + getLocalName() + " received " + possible_bands.size() + " business cards.");
                if (possible_bands.size() == 0)
                    return;

                /* compute the best bands to hire */
                hireBands();
            }
        }

        protected void handleInform(ACLMessage inform) {
            if (inform.getOntology().equals("Give_BusinessCard")) {
                if(Utils.DEBUG)
                    System.out.println(getLocalName() + " received \"Give_BusinessCard\" INFORM " + inform.getContent() + " from " + inform.getSender().getLocalName());

                String[] tokens = inform.getContent().split("::");
                int min_price = Integer.parseInt(tokens[2]);

                System.out.println("VENUE: " + getLocalName() + " current budget = " + budget);

                if (min_price < budget)
                    possible_bands.add(inform);

                requests_done++;

                if (available_bands.length == requests_done) {
                    System.out.println("VENUE: " + getLocalName() + " received " + possible_bands.size() + " business cards.");
                    if (possible_bands.size() == 0)
                        return;

                    /* compute the best bands to hire */
                    hireBands();
                }
            }
        }

        protected void handleFailure(ACLMessage failure) {
            // nothing to see here
        }

        private void hireBands() {
            hire_bands = new HireBands((Venue)getAgent());
            addBehaviour(hire_bands);
            requests_done = 0;
        }

        @Override
        public int onEnd() {
            if (possible_bands.size() == 0) {
                System.out.println("VENUE : " + getLocalName() + " - " + "No bands available. Exiting...");
                line_up_ready = true;
            }

            myAgent.removeBehaviour(this);

            return 0;
        }
    }


    /**
     *   Band Hirer
     */
    class HireBands extends Behaviour {

        Venue venue;
        boolean flag;

        public HireBands(Venue v) {
            flag = false;
        }

        @Override
        public void action() {

            switch(behaviour){
                case MOSTBANDS:
                    //get most bands
                    getMostBandsBehaviour();
                    break;

                case MOSTPRESTIGE:
                    //get mostprestige bands
                    getMostPrestigeBehaviour();
                    break;

                case MOSTPROFIT:
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
            request_contract = new RequestContract(venue, null);
            addBehaviour(request_contract);

            return 0;
        }

    }

    private void getMostBandsBehaviour() {

        ArrayList<ACLMessage> ordered_possible_bands = possible_bands;
        sortBandsByPrice(ordered_possible_bands);

        Collections.reverse(ordered_possible_bands);

        calculateBestBandsInMostBandsBehaviour(ordered_possible_bands);

        if(Utils.DEBUG)
            for(ACLMessage message : venue_proposal) {
                    System.out.println(message.getContent());
            }

    }

    private void calculateBestBandsInMostBandsBehaviour(ArrayList<ACLMessage> possible_bands) {
        int remainder_budget = this.budget;

        for (int i = 0; i < possible_bands.size(); i++) {
            String[] content = possible_bands.get(i).getContent().split("::");
            int min_price = Integer.parseInt(content[2]);

            if (remainder_budget >= min_price && isProfitable(possible_bands.get(i))) {
                remainder_budget -= min_price;
                possible_bands.get(i).setContent(Integer.toString(min_price));
            }
            else {
                possible_bands.get(i).setContent("0");
            }
            venue_proposal.add(possible_bands.get(i));
        }

    }

    private boolean isProfitable(ACLMessage aclMessage) {
        String[] content = aclMessage.getContent().split("::");
        int prestige = Integer.parseInt(content[1]);
        int min_price = Integer.parseInt(content[2]);
        int profit = getTicketPrice(prestige) * capacity * Utils.SPECTATORS_PER_SPECTATOR_AGENT - min_price;
        //System.out.println("profit = " + profit);
        if (profit >= 0) {
            return true;
        } else
            return false;
    }

    /**
     * Ticket price formula
     * */
    private int getTicketPrice (int p) {
        if (capacity < 10)
            return p*p*3+10- capacity;
        else
            return p*p*3+10;
    }

    private void getMostPrestigeBehaviour() {
        //buscar ao possible bands e guardo no venue proposals, no venue proposal ACL message mudo o min_price para preco qe quero pagar
        ArrayList<ACLMessage> possible_bands_ordered_by_prestige = new ArrayList<>();
        possible_bands_ordered_by_prestige = possible_bands;

        sortBandsByBestRating(possible_bands_ordered_by_prestige);

        calculateBestBandsInMostPrestigeBehaviour(possible_bands_ordered_by_prestige);
    }

    private void sortBandsByBestRating(ArrayList<ACLMessage> array) {
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

    private void sortBandsByPrice(ArrayList<ACLMessage> array) {
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

            if (remainder_budget >= min_price && isProfitable(array.get(i))) {
                remainder_budget -= min_price;
                total_prestige_score += prestigeScore(prestige);
            }
            else {
                array.get(i).setContent("0");
            }
            tmp.add(array.get(i));
        }

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

        if(Utils.DEBUG)
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
                if(Utils.DEBUG)
                    System.out.println(getLocalName() + " hiring " + venue_proposal.get(i).getSender().getLocalName() + " for " + venue_proposal.get(i).getContent());
                m.setOntology("Hiring");
                m.setContent(venue_proposal.get(i).getContent());
                v.add(m);
            }

            return v;
        }

        protected void handleAgree(ACLMessage agree) {
            // nothing to see here
        }

        protected void handleRefuse(ACLMessage refuse) {
            if (refuse.getOntology().equals("Hiring")) {
                requests_done++;
                if (venue_proposal.size() == requests_done) {
                    requests_done = 0;
                }
            }
        }

        protected void handleInform(ACLMessage inform) {
            if (inform.getOntology().equals("Hiring")) {
                requests_done++;

                if(Utils.DEBUG)
                    System.out.println(getLocalName() + " received INFORM " + inform.getContent() + " from " + inform.getSender().getLocalName());

                possible_bands.add(inform);
                if (venue_proposal.size() == requests_done) {
                    requests_done = 0;
                }
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
            band_responses++;
            //System.out.println(getLocalName() + " received " + request.getOntology() + " from " + request.getSender().getLocalName());
            ACLMessage reply = request.createReply();
            reply.setPerformative(ACLMessage.AGREE);

            switch (request.getOntology()) {
                case "Confirming_Presence":
                    reply.setOntology("Confirming_Presence");
                    reply.setContent("We will add you to our shows line-up");
                    break;
                case "Ignore_Message":
                    reply.setOntology("Ignore_Message");
                    reply.setContent("Thank you for considering");
                    break;
                case "Refusing_Show":
                    reply.setOntology("Refusing_Show");
                    reply.setContent("Thank you for considering");
                    received_refusal = true;
                    break;
            }

            return reply;
        }

        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
            ACLMessage result = request.createReply();

            switch (request.getOntology()) {
                case "Confirming_Presence":
                    result.setContent("Added to line-up");
                    result.setPerformative(ACLMessage.INFORM);

                    String[] content = request.getContent().split("::");
                    int hiring_price = Integer.parseInt(content[1]);
                    int prestige = Integer.parseInt(content[2]);
                    int genre = Integer.parseInt(content[3]);

                    ArrayList<Object> show = new ArrayList<>();
                    show.add(request.getSender().getLocalName());
                    show.add(getTicketPrice(prestige));
                    show.add(prestige);
                    show.add(genre);

                    shows.add(show);
                    budget = budget - hiring_price;

                    break;

                default:
                    result.setContent("Ignore this message");
                    result.setPerformative(ACLMessage.FAILURE);

                    break;
            }

            if (band_responses == venue_proposal.size() && !received_refusal) {
                System.out.println();
                System.out.println("VENUE : " + getLocalName() + " currently has " + shows.size() + " shows. \n" +
                        "VENUE : " + getLocalName() + " will now try to hire bands with leftover budget (" + budget + ").");

                if (shows.size() == 0)
                    widenSpectrums();

                resetVariables();
                retry();
            }

            return result;
        }

        private void widenSpectrums() {
            if (min_genre_spectrum > 10)
                min_genre_spectrum -= 10;
            else
                min_genre_spectrum = 1;

            if (max_genre_spectrum <= 90)
                max_genre_spectrum += 10;
            else
                max_genre_spectrum = 100;

            if (min_acceptable_prestige > 1)
                min_acceptable_prestige -= 1;

            if (max_acceptable_prestige < 5)
                max_acceptable_prestige += 1;

        }

        private void resetVariables() {
            possible_bands = new ArrayList<>();
            venue_proposal = new ArrayList<>();
            requests_done = 0;
            band_responses = 0;
            received_refusal = false;
        }

    }


    class ReceiveTicketRequest extends ContractNetResponder {

        public ReceiveTicketRequest(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        protected ACLMessage handleCfp(ACLMessage cfp) {
            System.out.println(getAID().getLocalName() + " received " + cfp.getContent() + " from " + cfp.getSender().getLocalName());


            /*
            ArrayList<Object> show = new ArrayList<>();
                    show.add(request.getSender().getLocalName());
                    show.add(getTicketPrice(prestige));
                    show.add(prestige);
                    show.add(genre);
             */
            ACLMessage reply = cfp.createReply();
            if (line_up_ready) {
                reply.setPerformative(ACLMessage.PROPOSE);
                //String content = getLocalName() + "::" + prestige + "::" + min_price;
                //reply.setContent(content);
            } else {
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("Venue " + getLocalName() + " not ready yet.");
            }

            return reply;
        }
        /*
        protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
            System.out.println(myAgent.getLocalName() + " got a reject from " + reject.getSender().getLocalName());
            business_cards_handed--;
        }

        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
            System.out.println(myAgent.getLocalName() + " got an accept from " + accept.getSender().getLocalName());
            ACLMessage result = accept.createReply();

            String[] tokens = accept.getContent().split("::");
            int price = Integer.parseInt(tokens[1]);

            Pair<String, Integer> pair = new Pair<>(accept.getSender().getLocalName() , price);
            all_proposals.add(pair);

            //wait for all proposals
            System.out.println(getAID().getLocalName() + " is waiting for " + business_cards_handed + " proposals, currently have " + all_proposals.size());
            while (business_cards_handed != all_proposals.size()) {
                System.out.println(getAID().getLocalName() + " is waiting for more proposals");
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (Exception e) {
                    System.out.println("band waiter is kaput");
                }
            }

            int max = min_price;
            int max_pos = 0;
            for (int i=0; i<all_proposals.size(); i++) {
                if (all_proposals.get(i).getValue() > max) {
                    max = all_proposals.get(i).getValue();
                    max_pos = i;
                }
            }

            if (accept.getSender().getLocalName().equals(all_proposals.get(max_pos).getKey())) {
                result.setPerformative(ACLMessage.INFORM);
                result.setContent("Gib moneys");
            } else {
                result.setPerformative(ACLMessage.FAILURE);
            }

            return result;
        }*/

    }
}
