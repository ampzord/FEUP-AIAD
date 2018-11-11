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

    private int capacity, budget, min_genre_spectrum, max_genre_spectrum, min_acceptable_prestige, max_acceptable_prestige, location, requests_done, band_responses;
    private DFAgentDescription[] existent_bands;
    private ArrayList<ACLMessage> possible_bands, venue_proposal;
    private ArrayList<ArrayList<Object>> shows;
    private VenueBehaviour behaviour;
    private boolean received_refusal, line_up_ready;
    private Behaviour getInterestingBands, show_confirmations, hire_bands, request_contract;

    @Override
    public String toString() {
        return String.format("Venue - %1$-17s", this.getAID().getLocalName())
                + String.format(" Capacity=%2s, Budget=%6s, Min Genre Spectrum=%s, Max Genre Spectrum=%s, Min Accept Prestige=%s, Max Accept Prestige=%s, Location=%s, Behaviour=%s",
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
    public DFAgentDescription[] getExistent_bands() {
        return existent_bands;
    }
    public void setExistent_bands(DFAgentDescription[] existent_bands) {
        this.existent_bands = existent_bands;
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
        printVenueInformation();
        registerToDFService();
        searchBands();
        startBehaviours();
    }

    private void startBehaviours() {
        getInterestingBands = new GetInterestingBands(this, new ACLMessage(ACLMessage.REQUEST));
        addBehaviour(getInterestingBands);
        show_confirmations = new ConfirmsBandShow(this, MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        addBehaviour(show_confirmations);
    }

    private void retry () {
        System.out.println("VENUE: " + getLocalName() + " retrying...");

        getInterestingBands.block();
        show_confirmations.block();
        hire_bands.block();
        request_contract.block();
        removeBehaviour(getInterestingBands);
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
                System.out.println("VENUE: " + getLocalName() + " found:");
                for(int i=0; i<result.length; ++i) {
                    System.out.println("Band: " + "    " + result[i].getName().getLocalName());
                }
            }

            setExistent_bands(result);
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

        if(Utils.DEBUG)
            System.out.println("VENUE : " + getLocalName() + " done working");
    }

    private void unregisterFromDFService() {
        try {
            DFService.deregister(this);
        } catch(FIPAException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ticket price formula
     * */
    private int getTicketPrice (int p) {
        if (capacity < 10)
            return p*p*3+10-capacity;
        else
            return p*p*3+10;
    }

    /**
     *  Gets bands that are interesting to the venue
     */
    class GetInterestingBands extends AchieveREInitiator {

        public GetInterestingBands(Agent a, ACLMessage msg) {
            super(a, msg);
        }

        protected Vector<ACLMessage> prepareRequests(ACLMessage msg) {
            Vector<ACLMessage> v = new Vector<>();

            if(Utils.DEBUG)
                System.out.println();
            for (int i = 0; i < existent_bands.length; i++) {
                msg.addReceiver(new AID(existent_bands[i].getName().getLocalName(), false));
                if(Utils.DEBUG)
                    System.out.println("VENUE: " + getLocalName() + " - [GetInterestingBands] Sending Request to " + existent_bands[i].getName().getLocalName());
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
            if (refuse.getOntology().equals("Give_BusinessCard")) {
                if (Utils.DEBUG)
                    System.out.println("VENUE: " + getLocalName() + " [GetInterestingBands] received refuse from " + refuse.getSender().getLocalName());
                requests_done++;

                if (existent_bands.length == requests_done) {
                    if (Utils.DEBUG)
                        System.out.println("VENUE: " + getLocalName() + " [GetInterestingBands] received " + possible_bands.size() + " business cards.");

                    if (possible_bands.size() == 0) {
                        //System.out.println("Possible Bands Size is 0");
                        return;
                    }

                    else
                        hireBands();
                }
            }
        }

        protected void handleInform(ACLMessage inform) {
            if (inform.getOntology().equals("Give_BusinessCard")) {
                if(Utils.DEBUG)
                    System.out.println("VENUE: " + getLocalName() + " [GetInterestingBands] received \"Give_BusinessCard\" INFORM " + inform.getContent() + " from " + inform.getSender().getLocalName());

                String[] tokens = inform.getContent().split("::");
                int min_price = Integer.parseInt(tokens[2]);

                if(Utils.DEBUG)
                    System.out.println("VENUE: " + getLocalName() + " current budget = " + budget);

                if (min_price < budget)
                    possible_bands.add(inform);

                requests_done++;

                if (existent_bands.length == requests_done) {
                    if(Utils.DEBUG)
                        System.out.println("VENUE: " + getLocalName() + " [GetInterestingBands] received " + possible_bands.size() + " business cards.");

                    if (possible_bands.size() == 0) {
                        System.out.println("Possible Bands Size is 0");
                        return;
                    }
                    else
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

                System.out.println("VENUE: " + getLocalName() + " - " + "No more bands available. Exiting...");
                //System.out.println("VENUE: " + getLocalName() + " has " + shows.size() + " shows.");

                line_up_ready = true;
                addBehaviour(new ReceiveTicketRequest(myAgent, MessageTemplate.MatchPerformative(ACLMessage.CFP)));
            }

            System.out.println("VENUE: finished STEP 1 by " + getLocalName() + " @ [GetInterestingBands]");
            System.out.println("VENUE: finished STEP 1 by " + getLocalName() + " @ [GetInterestingBands]");

            return 0;
        }
    }

    /**
     *   Venue Behaviour to hire Bands
     */
    class HireBands extends Behaviour {

        Venue venue;
        boolean flag;

        public HireBands(Venue v) {
            venue = v;
            flag = false;
        }

        @Override
        public void action() {

            switch(behaviour){
                case MOSTBANDS:
                    getMostBandsBehaviour();
                    break;

                case MOSTPRESTIGE:
                    getMostPrestigeBehaviour();
                    break;

                case MOSTPROFIT:
                    getMostProfitBehaviour();
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

            System.out.println("VENUE: finished STEP 2 by " + getLocalName() + " @ [HireBands]");

            request_contract = new RequestContractToBand(venue, null);
            addBehaviour(request_contract);
            return 0;
        }

        /* ------------- */

        private void getMostBandsBehaviour() {
            ArrayList<ACLMessage> ordered_possible_bands = possible_bands;
            sortBands(ordered_possible_bands);
            Collections.reverse(ordered_possible_bands);
            calculateBestBands(ordered_possible_bands);
        }

        //TODO
        private void getMostProfitBehaviour() {
            ArrayList<ACLMessage> ordered_possible_bands = possible_bands;
            sortBands(ordered_possible_bands);
        }

        private void getMostPrestigeBehaviour() {
            ArrayList<ACLMessage> possible_bands_ordered_by_prestige = possible_bands;
            sortBandsByMostPrestige(possible_bands_ordered_by_prestige);
            calculateBestBands(possible_bands_ordered_by_prestige);
        }

        private void calculateBestBands(ArrayList<ACLMessage> possible_bands) {
            switch(behaviour) {
                case MOSTBANDS:
                    calculateBestBandsMostBandsBehaviour(possible_bands);
                    break;
                case MOSTPROFIT:
                    calculateBestBandsMostProfitBehaviour(possible_bands);
                    break;
                case MOSTPRESTIGE:
                    calculateBestBandsMostPrestigeBehaviour(possible_bands);
                    break;

            }
        }

        private void calculateBestBandsMostBandsBehaviour(ArrayList<ACLMessage> possible_bands) {
            int remainder_budget = budget;
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

        //TODO MostProfitBehaviour
        private void calculateBestBandsMostProfitBehaviour(ArrayList<ACLMessage> possible_bands) {

        }

        private void calculateBestBandsMostPrestigeBehaviour(ArrayList<ACLMessage> possible_bands) {
            int remainder_budget = budget;
            int total_prestige_score = 0;

            ArrayList<ACLMessage> tmp = new ArrayList<>();

            for (int i = 0; i < possible_bands.size(); i++) {
                String[] content = possible_bands.get(i).getContent().split("::");
                int prestige = Integer.parseInt(content[1]);
                int min_price = Integer.parseInt(content[2]);

                if (remainder_budget >= min_price && isProfitable(possible_bands.get(i))) {
                    remainder_budget -= min_price;
                    total_prestige_score += prestigeScore(prestige);
                }
                else {
                    possible_bands.get(i).setContent("0");
                }
                tmp.add(possible_bands.get(i));
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
        }

        private void sortBands(ArrayList<ACLMessage> bands) {
            switch(behaviour) {
                case MOSTBANDS:
                    sortBandsByLowestPrice(bands);
                    break;
                case MOSTPROFIT:
                    sortBandsbyMostProfit(bands);
                    break;
                case MOSTPRESTIGE:
                    sortBandsByMostPrestige(bands);
                    break;

            }
        }

        private void sortBandsByLowestPrice(ArrayList<ACLMessage> bands) {
            int n = bands.size();
            for (int i = 0; i < n-1; i++)
                for (int j = 0; j < n-i-1; j++) {
                    String[] content1 = bands.get(j).getContent().split("::");
                    String[] content2 = bands.get(j+1).getContent().split("::");
                    int rating1 = Integer.parseInt(content1[1]);
                    int rating2 = Integer.parseInt(content2[1]);
                    int min_preco1 = Integer.parseInt(content1[2]);
                    int min_preco2 = Integer.parseInt(content2[2]);

                    if (min_preco1 < min_preco2)
                    {
                        ACLMessage temp = bands.get(j);
                        bands.set(j, bands.get(j+1));
                        bands.set(j+1, temp);
                    }
                    else if (min_preco1 == min_preco2) {
                        if (rating1 < rating2)
                        {
                            ACLMessage temp = bands.get(j);
                            bands.set(j, bands.get(j+1));
                            bands.set(j+1, temp);
                        }
                    }
                }
        }

        //TODO MostProfitSort
        private void sortBandsbyMostProfit(ArrayList<ACLMessage> ordered_possible_bands) {

        }

        private void sortBandsByMostPrestige(ArrayList<ACLMessage> array) {
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

        private boolean isProfitable(ACLMessage aclMessage) {
            if (getProfit(aclMessage) >= 0) {
                return true;
            } else
                return false;
        }

        private int getProfit (ACLMessage aclMessage) {
            String[] content = aclMessage.getContent().split("::");
            int prestige = Integer.parseInt(content[1]);
            int min_price = Integer.parseInt(content[2]);
            return getTicketPrice(prestige) * capacity * Utils.SPECTATORS_PER_SPECTATOR_AGENT - min_price;
        }

        private int prestigeScore(int prestige) {
            return prestige*prestige;
        }

    }

    /**
     *  Request Contract to a Band
     */
    class RequestContractToBand extends AchieveREInitiator {

        boolean someBandRefused;

        public RequestContractToBand(Agent a, ACLMessage msg) {
            super(a, msg);
            someBandRefused = false;
        }

        protected Vector<ACLMessage> prepareRequests(ACLMessage msg) {
            Vector<ACLMessage> v = new Vector<>();

            if(Utils.DEBUG)
                System.out.println();
            for(int i=0; i<venue_proposal.size(); i++) {
                ACLMessage m = new ACLMessage(ACLMessage.REQUEST);
                m.addReceiver(new AID(venue_proposal.get(i).getSender().getLocalName(), false));
                if(Utils.DEBUG)
                    System.out.println("VENUE: " + getLocalName() + " [RequestContract] hiring " + venue_proposal.get(i).getSender().getLocalName() + " for " + venue_proposal.get(i).getContent());
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
                    System.out.println("VENUE: " + getLocalName() + " received [RequestContract] INFORM " + inform.getContent() + " from " + inform.getSender().getLocalName());

                //possible_bands.add(inform);
                if (venue_proposal.size() == requests_done) {
                    requests_done = 0;
                }
            }
        }

        protected void handleFailure(ACLMessage failure) {
            // nothing to see here
        }

        @Override
        public int onEnd() {
            System.out.println("VENUE: finished STEP 4 by " + getLocalName() + " @ [RequestContractToBand]\n" +
                    "       venue_proposal.size = " + venue_proposal.size());
            return 0;
        }

    }

    /**
     *  Confirms presence of a band on the venue
     */
    class ConfirmsBandShow extends AchieveREResponder {

        public ConfirmsBandShow(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        protected ACLMessage handleRequest(ACLMessage request) throws RefuseException {
            ACLMessage reply = request.createReply();
            if (request.getOntology().equals("Confirming_Presence") || request.getOntology().equals("Ignore_Message") || request.getOntology().equals("Refusing_Show")) {
                band_responses++;
                if (Utils.DEBUG)
                    System.out.println("VENUE: " + getLocalName() + " received [ConfirmsBandShow] " + request.getOntology() + " from " + request.getSender().getLocalName());
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

            }
            return reply;
        }

        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
            ACLMessage result = request.createReply();

            if (venue_proposal.size() == 0) {
                System.out.println("---------------------------------------------------- venue_proposal.size() = 0 ---- message content = " + request.getOntology());
            }

            if (request.getOntology().equals("Confirming_Presence") || request.getOntology().equals("Ignore_Message") || request.getOntology().equals("Refusing_Show")) {

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

                System.out.println("VENUE: band responses =  " + band_responses + "  &&  venue_proposal.size = " + venue_proposal.size() + "    ----    " + getLocalName());

                if (band_responses == venue_proposal.size()) {
                    if (Utils.DEBUG) {
                        System.out.println();
                        System.out.println("VENUE : " + getLocalName() + " [ConfirmsBandShow] currently has " + shows.size() + " shows. \n" +
                                "VENUE : " + getLocalName() + " will now try to hire bands with leftover budget (" + budget + ").");
                    }

                    System.out.println("VENUE: finished STEP 3 by " + getLocalName() + " @ [ConfirmsBandShow]");

                    if (shows.size() == 0)
                        widenSpectrums();

                    resetVariables();
                    retry();
                }
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

    /**
     *  Handles the communication of Spectator about the shows
     */
    class ReceiveTicketRequest extends ContractNetResponder {

        public ReceiveTicketRequest(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        protected ACLMessage handleCfp(ACLMessage cfp) {
            if (Utils.DEBUG)
                System.out.println("VENUE: " + getAID().getLocalName() + " [ReceiveTicketRequest] received " + cfp.getContent() + " from " + cfp.getSender().getLocalName());

            ACLMessage reply = cfp.createReply();

            System.out.println("Entered handleCfp1");

            if (line_up_ready) {
                System.out.println("Lineup is ready!");
                reply.setPerformative(ACLMessage.PROPOSE);
                String message = "";

                message += location;
                for (ArrayList<Object> show : shows) {
                    message += "//" + show.get(0) + "::" + show.get(1) + "::" + show.get(2) + "::" + show.get(3);
                }
                reply.setContent(message);
                System.out.println("VENUE: " + getLocalName() + " [ReceiveTicketRequest] sends " + message);
            }
            else {
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent("Venue not ready yet.");
                if (Utils.DEBUG)
                    System.out.println("Venue " + getLocalName() + " not ready yet!");
            }

            return reply;
        }


        protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
            System.out.println("handleRejectProposal");
        }

        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
            System.out.println("handleAcceptProposal");
            return accept;
        }

    }
}
