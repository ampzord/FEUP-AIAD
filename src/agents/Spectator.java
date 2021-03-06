package agents;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.proto.ContractNetInitiator;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import utils.Utils;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Spectator extends Agent {


    public enum SpectatorBehaviour {
        MOSTBANDS, MOSTPRESTIGE, LEASTDISTANCE, LEASTCOST;
    }

    private int budget;
    private int min_genre_spectrum;
    private int max_genre_spectrum;
    private int location;
    private ArrayList<String> ticket_shows_bought;
    private ArrayList<ACLMessage> wanted_shows;
    private DFAgentDescription[] existent_venues;
    private SpectatorBehaviour behaviour;
    private Behaviour init_negotiations;
    private ConcurrentLinkedQueue<AgentController> queue;

    @Override
    public String toString() {
        return String.format("Spectator - %s, Budget=%4s, Min Genre Spectrum=%s, Max Genre Spectrum=%s, Location=%s,Behaviour=%s",
                this.getLocalName(), this.budget, this.min_genre_spectrum, this.max_genre_spectrum, this.location, this.behaviour);
    }
    public void setBudget(int budget) {
        this.budget = budget;
    }
    public void setMin_genre_spectrum(int min_genre_spectrum) {
        this.min_genre_spectrum = min_genre_spectrum;
    }
    public void setMax_genre_spectrum(int max_genre_spectrum) {
        this.max_genre_spectrum = max_genre_spectrum;
    }
    public void setLocation(int location) {
        this.location = location;
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
                break;

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
        //printSpectatorInformation();
        searchVenues();
        startBehaviours();
    }

    private void startBehaviours() {
        init_negotiations = new InitiateNegotiationWithVenue(this, new ACLMessage(ACLMessage.CFP));
        addBehaviour(init_negotiations);
    }

    private void setSpectatorInformation() {
        setBudget((int) getArguments()[1]);
        setMin_genre_spectrum((int) getArguments()[2]);
        setMax_genre_spectrum((int) getArguments()[3]);
        setLocation((int) getArguments()[4]);
        setBehaviour((String) getArguments()[5]);
        wanted_shows = new ArrayList<>();
        ticket_shows_bought = new ArrayList<>();
        queue = (ConcurrentLinkedQueue<AgentController>) getArguments()[6];
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
     * Start communication with Venue to decide which shows the spectator will view
     */
    class InitiateNegotiationWithVenue extends ContractNetInitiator {

        public InitiateNegotiationWithVenue(Agent a, ACLMessage msg) {
            super(a, msg);
        }

        protected Vector prepareCfps(ACLMessage cfp) {
            Vector v = new Vector();
            System.out.println("SPECTATOR: " + getLocalName() + " found " + existent_venues.length + " with DF.\n" +
                    "      also, queue size = " + queue.size());

            for (int i = 0; i < existent_venues.length; i++) {
                cfp.addReceiver(new AID(existent_venues[i].getName().getLocalName(), false));
                if (Utils.DEBUG)
                    System.out.println("SPECTATOR: " + getLocalName() + " - Sending Call For Proposal (CFP) to " + existent_venues[i].getName().getLocalName());
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
                        if (Utils.DEBUG)
                            System.out.println("SPECTATOR: " + getLocalName() + " received a not ready from " + msg.getSender().getLocalName());
                        break;
                    case ACLMessage.PROPOSE:
                        //venue_has_shows++;
                        //System.out.println("Spectator : " + getLocalName() + " has a proprosal from " + msg.getSender().getLocalName());
                        //System.out.println("MESSAGE: " + msg.getContent().toString());

                        String[] show = msg.getContent().split("//");
                        String venue_name = msg.getSender().getLocalName();
                        int venue_location = Integer.valueOf(show[0]);

                        for (int j = 1; j < show.length; j++) {
                            ACLMessage temp = msg.shallowClone();
                            String[] show_information = show[j].split("::");

                            /*String band_name = show_information[0];
                            int prestige = Integer.valueOf(show_information[2]);*/
                            int ticket_price = Integer.valueOf(show_information[1]);
                            int genre = Integer.valueOf(show_information[3]);

                            String message = venue_name + "::" + venue_location + "::"
                                    + show_information[0] + "::" + show_information[1] + "::"
                                    + show_information[2] + "::" + show_information[3];

                            if (genre >= min_genre_spectrum && genre <= max_genre_spectrum && ticket_price <= budget) {
                                temp.setContent(message);
                                wanted_shows.add(temp);
                            }

                        }
                        break;
                }

            }

            if (wanted_shows.size() <= 0) {
                System.out.println("SPECTATOR: " + getLocalName() + " has no interesting shows to go to.");
            } else {
                if (Utils.DEBUG)
                    for (ACLMessage m : wanted_shows)
                        System.out.println("Wanted shows by : " + getLocalName() + " - " + m.getContent());

                switch (behaviour) {
                    case MOSTBANDS:
                        getMostBandsBehaviour();
                        break;

                    default:
                        break;
                }

                for (ACLMessage m : wanted_shows) {
                    ACLMessage reply = m.createReply();

                    String[] content = m.getContent().split("::");
                    String band_name = content[2];
                    int ticket_price = Integer.parseInt(content[3]);

                    int temp_budget = budget;

                    if (temp_budget >= ticket_price) {
                        temp_budget -= ticket_price;
                        reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                        reply.setContent(m.getContent());
                    } else {
                        reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        reply.setContent("");
                    }

                    acceptances.add(reply);

                }
            }
        }

        protected void handleInform(ACLMessage inform) {
            String[] tokens = inform.getContent().split("::");
            int ticket_price = Integer.parseInt(tokens[3]);

            if (ticket_price <= budget) {
                budget -= ticket_price;
                ticket_shows_bought.add(inform.getContent());
                System.out.println("\nSpectator: " + getLocalName() + " is going to Venue: " + tokens[0] + " to watch Band: " + tokens[2]);
            }

            if (!queue.isEmpty()) {
                try {
                    queue.poll().start();
                } catch (StaleProxyException e) {
                    e.printStackTrace();
                }
            }
        }

        protected void handleFailure(ACLMessage failure) {
            System.out.println("--------------==================RETRY==============----------------");
        }

        /* --- */

        private void getMostBandsBehaviour() {
            ArrayList<ACLMessage> ordered_possible_bands = wanted_shows;
            sortShows(ordered_possible_bands);
            wanted_shows = ordered_possible_bands;
        }

        private void sortShows(ArrayList<ACLMessage> shows) {
            switch(behaviour) {
                case MOSTBANDS:
                    sortShowsByLowestPrice(shows);

                case MOSTPRESTIGE:
                    sortShowsByPrestige(shows);
                    break;

                case LEASTCOST:
                    //sortShowsByCost(shows);
                    break;

                case LEASTDISTANCE:
                    sortShowsByDistance(shows);
                    break;

                default:
                    break;
            }
        }

        private void sortShowsByPrestige(ArrayList<ACLMessage> shows){
            int n = shows.size();
            for (int i = 0; i < n-1; i++)
                for (int j = 0; j < n-i-1; j++) {
                    String[] content1 = shows.get(j).getContent().split("::");
                    String[] content2 = shows.get(j+1).getContent().split("::");
                    int prestige1 = Integer.parseInt(content1[4]);
                    int prestige2 = Integer.parseInt(content2[4]);
                    int ticket_price1 = Integer.parseInt(content1[3]);
                    int ticket_price2 = Integer.parseInt(content2[3]);

                    if (prestige1 > prestige2)
                    {
                        ACLMessage temp = shows.get(j);
                        shows.set(j, shows.get(j+1));
                        shows.set(j+1, temp);
                    }
                    else if (prestige1 == prestige2)
                    {
                        if (ticket_price2 > ticket_price1) {
                            ACLMessage temp = shows.get(j);
                            shows.set(j, shows.get(j + 1));
                            shows.set(j + 1, temp);
                        }
                    }
                }
        }

        private void sortShowsByDistance(ArrayList<ACLMessage> shows){
            int n = shows.size();
            for (int i = 0; i < n-1; i++)
                for (int j = 0; j < n-i-1; j++) {
                    String[] content1 = shows.get(j).getContent().split("::");
                    String[] content2 = shows.get(j+1).getContent().split("::");
                    int show_location1 = Integer.parseInt(content1[1]);
                    int show_location2 = Integer.parseInt(content2[1]);

                    int ticket_price1 = Integer.parseInt(content1[3]);
                    int ticket_price2 = Integer.parseInt(content2[3]);

                    if ( (location - show_location2) > (location - show_location1) )
                    {
                        ACLMessage temp = shows.get(j);
                        shows.set(j, shows.get(j+1));
                        shows.set(j+1, temp);
                    }
                    else if ( (location - show_location1) == (location - show_location2) )
                    {
                        if (ticket_price2 > ticket_price1) {
                            ACLMessage temp = shows.get(j);
                            shows.set(j, shows.get(j + 1));
                            shows.set(j + 1, temp);
                        }
                    }
                }
        }

        private void sortShowsByLowestPrice(ArrayList<ACLMessage> shows) {
            int n = shows.size();
            for (int i = 0; i < n-1; i++)
                for (int j = 0; j < n-i-1; j++) {
                    String[] content1 = shows.get(j).getContent().split("::");
                    String[] content2 = shows.get(j+1).getContent().split("::");
                    int location1 = Integer.parseInt(content1[1]);
                    int location2 = Integer.parseInt(content2[1]);
                    int ticket_price1 = Integer.parseInt(content1[3]);
                    int ticket_price2 = Integer.parseInt(content2[3]);

                    if (ticket_price1 > ticket_price2)
                    {
                        ACLMessage temp = shows.get(j);
                        shows.set(j, shows.get(j+1));
                        shows.set(j+1, temp);
                    }
                    else if (ticket_price1 == ticket_price2)
                    {
                        if (Math.abs(location-location1) > Math.abs(location-location2)) {
                            ACLMessage temp = shows.get(j);
                            shows.set(j, shows.get(j + 1));
                            shows.set(j + 1, temp);
                        }
                    }
                }
        }

    }
}

