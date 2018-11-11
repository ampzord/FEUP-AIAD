package agents;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
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
    //private ArrayList<ACLMessage> all_shows;
    private ArrayList<ACLMessage> wanted_shows;
    private DFAgentDescription[] existent_venues;
    private SpectatorBehaviour behaviour;
    private Behaviour viewMostBands;
    private int venue_has_shows;

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

    public void setExistent_venues(DFAgentDescription[] existent_venues) {
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
        startBehaviours();
        //addBehaviour(new InitiateNegotiationWithVenue(this, new ACLMessage(ACLMessage.CFP)));
    }

    private void startBehaviours() {
        viewMostBands = new InitiateNegotiationWithVenue(this, new ACLMessage(ACLMessage.CFP));
        addBehaviour(viewMostBands);
    }

    private void retry() {
        //System.out.println("SPECTATOR: " + getLocalName() + " retrying...");
        viewMostBands.block();
        removeBehaviour(viewMostBands);
        startBehaviours();
    }

    private void setSpectatorInformation() {
        setBudget((int) getArguments()[1]);
        setMin_genre_spectrum((int) getArguments()[2]);
        setMax_genre_spectrum((int) getArguments()[3]);
        setLocation((int) getArguments()[4]);
        setBehaviour((String) getArguments()[5]);
        //wanted_shows = new ArrayList<>();
        wanted_shows = new ArrayList<>();
        venue_has_shows = 0;
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
        } catch (FIPAException fe) {
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
        try {
            DFAgentDescription[] result = DFService.search(this, template);
            setExistent_venues(result);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    /**
     * Holds the decision making of a spectator on the show to watch
     */
    class ViewShow extends Behaviour {

        Spectator spec;
        boolean flag;

        public ViewShow(Spectator spec) {
            spec = spec;
            flag = false;
        }

        @Override
        public void action() {

            switch (behaviour) {
                case MOSTBANDS:
                    System.out.println("Starting mostBands Spectator Behaviour");
                    getMostBandsBehaviour();
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
            System.out.println("Entered SpectatorBehaviour onEnd()");
            return 0;
        }

        private void getMostBandsBehaviour() {
            /*
            ArrayList<ACLMessage> ordered_possible_bands = possible_bands;
            sortBands(ordered_possible_bands);
            Collections.reverse(ordered_possible_bands);
            calculateBestBands(ordered_possible_bands);*/

            ArrayList<ACLMessage> ordered_possible_bands = wanted_shows;

            for (ACLMessage show : ordered_possible_bands)
                System.out.println(show.getContent());

            sortShows(ordered_possible_bands);

            for (ACLMessage show : ordered_possible_bands)
                System.out.println(show.getContent());

            //calculateBestShows(ordered_possible_bands);
        }

        private void calculateBestShows(ArrayList<ACLMessage> shows) {
            switch(behaviour) {
                case MOSTBANDS:
                    calculateMostBandsBehaviour(shows);
                    break;
            }
        }

        void calculateMostBandsBehaviour(ArrayList<ACLMessage> shows) {
            /*int remainder_budget = budget;
            for (int i = 0; i < possible_bands.size(); i++) {
                String[] content = possible_bands.get(i).getContent().split("::");
                int min_price = Integer.parseInt(content[2]);

                if (remainder_budget >= min_price && isProfitable(possible_bands.get(i))) {
                    remainder_budget -= min_price;
                    possible_bands.get(i).setContent(Integer.toString(min_price));
                } else {
                    possible_bands.get(i).setContent("0");
                }
                venue_proposal.add(possible_bands.get(i));
            }*/
        }

        private void sortShows(ArrayList<ACLMessage> shows) {
            switch(behaviour) {
                case MOSTBANDS:
                    sortShowsByLowestPrice(shows);
                    break;
            }
        }

        private void sortShowsByLowestPrice(ArrayList<ACLMessage> shows) {
            int n = shows.size();
            for (int i = 0; i < n-1; i++)
                for (int j = 0; j < n-i-1; j++) {
                    String[] content1 = shows.get(j).getContent().split("::");
                    String[] content2 = shows.get(j+1).getContent().split("::");
                    int ticket_price1 = Integer.parseInt(content1[3]);
                    int ticket_price2 = Integer.parseInt(content2[3]);

                    int min_preco1 = Integer.parseInt(content1[2]);
                    int min_preco2 = Integer.parseInt(content2[2]);

                    if (ticket_price1 < ticket_price2)
                    {
                        ACLMessage temp = shows.get(j);
                        shows.set(j, shows.get(j+1));
                        shows.set(j+1, temp);
                    }
                }
        }


    }

    /**
     * Start communication with Venue to decide which shows the spectator will view
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
                        retry();
                        break;
                    case ACLMessage.PROPOSE:
                        venue_has_shows++;
                        System.out.println("Spectator : " + getLocalName() + " has a proprosal from " + msg.getSender().getLocalName());
                        //System.out.println("MESSAGE: " + msg.getContent().toString());

                        String[] show = msg.getContent().split("//");
                        String venue_name = msg.getSender().getLocalName();
                        int venue_location = Integer.valueOf(show[0]);


                        for (int j = 1; j < show.length; j++) {
                            ACLMessage temp = msg;
                            String[] show_information = show[j].split("::");

                            /*for(String info : show_information) {
                                System.out.println("Info:" + info.toString());
                            }*/

                            String band_name = show_information[0];
                            int ticket_price = Integer.valueOf(show_information[1]);
                            int prestige = Integer.valueOf(show_information[2]);
                            int genre = Integer.valueOf(show_information[3]);

                            String message = venue_name + "::" + venue_location + "::"
                                    + show_information[0] + "::" + show_information[1]
                                    + show_information[2] + "::" + show_information[3];

                            if (genre >= min_genre_spectrum && genre <= max_genre_spectrum) {
                                temp.setContent(message);
                                wanted_shows.add(temp);
                            }

                        }

                        for(ACLMessage asd : wanted_shows) {
                            System.out.println("Info:   " + asd.getContent().toString());
                        }

                        break;

                }
            }

        }

        protected void handleAllResultNotifications(Vector resultNotifications) {
            if (Utils.DEBUG)
                System.out.println("Spectator: " + getLocalName() + " got " + resultNotifications.size() + " result notifications!");
        }
    }
}

