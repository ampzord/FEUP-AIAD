package agents;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import utils.Utils;
import java.util.ArrayList;
import java.util.Vector;
import javafx.util.Pair;

public class Band extends Agent {

    private int genre, prestige, min_price, min_attendance, current_shows, business_cards_handed;
    private ArrayList<Pair<String, Integer>> all_proposals;

    @Override
    public String toString() {
        return String.format("Band - %1$-17s", this.getAID().getLocalName())
            + String.format(" Genre=%s, Prestige=%s, Min Price=%s, Min Attendance=%s",
                this.genre, this.prestige, this.min_price, this.min_attendance);
    }

    public int getGenre() {
        return genre;
    }
    public void setGenre(int genre) {
        this.genre = genre;
    }
    public int getPrestige() {
        return prestige;
    }
    public void setPrestige(int prestige) {
        this.prestige = prestige;
    }
    public int getMin_price() {
        return min_price;
    }
    public void setMin_price(int min_price) {
        this.min_price = min_price;
    }
    public int getMin_attendance() {
        return min_attendance;
    }
    public void setMin_attendance(int min_attendance) {
        this.min_attendance = min_attendance;
    }
    public int getCurrent_shows() {
        return current_shows;
    }
    public void setCurrent_shows(int current_shows) {
        this.current_shows = current_shows;
    }

    public void setup() {
        setBandInformation();
        printBandInformation();
        registerToDFService();
        addBehaviour(new VenueRequestResponder(this, MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
    }

    private void setBandInformation() {
        setGenre((int)getArguments()[1]);
        setPrestige((int)getArguments()[2]);
        setMin_price((int)getArguments()[3]);
        setMin_attendance((int)getArguments()[4]);
        setCurrent_shows(0);
        all_proposals = new ArrayList<>();
        business_cards_handed = 0;
    }

    private void printBandInformation() {
        System.out.println(this.toString());
    }

    private void registerToDFService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("band");
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
            System.out.println("BAND: " + getLocalName() + ": done working");
    }

    private void unregisterFromDFService() {
        try {
            DFService.deregister(this);
        } catch(FIPAException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Handles the requests of Venue
     */
    class VenueRequestResponder extends AchieveREResponder {

        public VenueRequestResponder(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        protected ACLMessage handleRequest(ACLMessage request) throws RefuseException {
            if(Utils.DEBUG)
                System.out.println("BAND: " + getLocalName() + " [VenueRequestResponder] received " + request.getOntology() + " from " + request.getSender().getLocalName());
            ACLMessage reply = request.createReply();

            switch (request.getOntology()) {
                case "Give_BusinessCard":
                    if(Utils.DEBUG)
                        System.out.println("BAND: " + getLocalName() + " [VenueRequestResponder] Giving Business Card to " + request.getSender().getLocalName());

                    String[] tokens = request.getContent().split("::");
                    int attendance = Integer.parseInt(tokens[0]);
                    int min_genre_spectrum = Integer.parseInt(tokens[1]);
                    int max_genre_spectrum = Integer.parseInt(tokens[2]);

                    if (evaluateAcceptance(attendance, min_genre_spectrum, max_genre_spectrum) && current_shows < Utils.MAX_SHOWS_PER_BAND) {
                        reply.setPerformative(ACLMessage.AGREE);
                        reply.setOntology("Give_BusinessCard");
                    } else {
                        reply.setPerformative(ACLMessage.REFUSE);
                        reply.setOntology("Give_BusinessCard");
                    }

                    break;

                case "Hiring":
                    if(Utils.DEBUG)
                        System.out.println("BAND: " + request.getSender().getLocalName() + " hiring " + getLocalName());

                    int proposed_payment = Integer.parseInt(request.getContent());
                    all_proposals.add(new Pair<>(request.getSender().getLocalName(),proposed_payment));

                    if (current_shows < Utils.MAX_SHOWS_PER_BAND) {
                        reply.setPerformative(ACLMessage.AGREE);
                        reply.setOntology("Hiring");
                    } else
                        throw new RefuseException("Refused Request");

                    break;
            }

            return reply;
        }

        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
            ACLMessage result = request.createReply();
            //System.out.println("BAND: " + getLocalName() + " ----- " + business_cards_handed + " must be == to proposals.size");

            switch (request.getOntology()) {
                case "Give_BusinessCard":
                    String content = getLocalName() + "::" + prestige + "::" + min_price;
                    result.setContent(content);
                    result.setPerformative(ACLMessage.INFORM);
                    result.setOntology("Give_BusinessCard");
                    business_cards_handed++;
                    break;

                case "Hiring" :
                    result.setContent("I'll get back to you");
                    result.setPerformative(ACLMessage.INFORM);
                    result.setOntology("Hiring");

                    //System.out.println("BAND: " + " ------ business_cards_handed = " + business_cards_handed + "  &&  all_proposals.size = " + all_proposals.size() + " ------------ " + getLocalName());
                    if (business_cards_handed == all_proposals.size() && business_cards_handed > 0) {
                        business_cards_handed = 0;

                        ArrayList<Pair<String, Integer>> temp = new ArrayList();
                        for (Pair<String, Integer> p : all_proposals) {
                            temp.add(p);
                        }

                        all_proposals = new ArrayList<>();

                        if(Utils.DEBUG)
                            System.out.println("BAND: " + getLocalName() + " will now respond to all Venues");
                        decideWhereToPlay();


                        addBehaviour(new ConfirmShow(this.myAgent, null, temp));
                    }

                    break;
            }

            return result;
        }

        private boolean evaluateAcceptance(int attendance, int min_genre_spectrum, int max_genre_spectrum) {
            if (attendance >= min_attendance && min_genre_spectrum <= genre && genre <= max_genre_spectrum)
                return true;
            return false;
        }

        private void decideWhereToPlay() {
            Pair<String, Integer> temp;
            for (int i = 0; i < all_proposals.size() - 1; i++) {
                for (int j = 1; j < all_proposals.size() - i; j++) {
                    if (all_proposals.get(j-1).getValue() < all_proposals.get(j).getValue()) {
                        temp = all_proposals.get(j-1);
                        all_proposals.set(j-1, all_proposals.get(j));
                        all_proposals.set(j, temp);
                    }
                }
            }
        }

        @Override
        public int onEnd() {
            System.out.println("Band communication to Venue has ended!");
            return 0;
        }
    }

    /**
     *  Confirm Shows to Venue
     */
    class ConfirmShow extends AchieveREInitiator {

        ArrayList<Pair<String, Integer>> proposals;

        public ConfirmShow(Agent a, ACLMessage msg, ArrayList<Pair<String, Integer>> temp) {
            super(a, msg);
            proposals = temp;
        }

        protected Vector<ACLMessage> prepareRequests(ACLMessage msg) {
            Vector<ACLMessage> v = new Vector<>();

            for (int i = 0; i < proposals.size(); i++) {
                ACLMessage m = new ACLMessage(ACLMessage.REQUEST);

                if (current_shows < Utils.MAX_SHOWS_PER_BAND && proposals.get(i).getValue() >= min_price) {
                    current_shows++;
                    m.setOntology("Confirming_Presence");
                    m.addReceiver(new AID(proposals.get(i).getKey(), false));


                    System.out.println("BAND: " + getLocalName() + " Confirming_Presence @ " + proposals.get(i).getKey() + " for " + proposals.get(i).getValue() + "$");
                    String content = getLocalName() + "::" + proposals.get(i).getValue() + "::" + prestige + "::" + genre;

                    m.setContent(content);
                } else if (proposals.get(i).getValue() == 0) {
                    m.setOntology("Ignore_Message");
                    m.addReceiver(new AID(proposals.get(i).getKey(), false));

                    if(Utils.DEBUG)
                        System.out.println("BAND: " + getLocalName() + " --- Ignore_Message --- from " + proposals.get(i).getKey());

                    m.setContent("");
                } else {
                    m.setOntology("Refusing_Show");
                    m.addReceiver(new AID(proposals.get(i).getKey(), false));

                    if(Utils.DEBUG)
                        System.out.println("BAND: " + getLocalName() + " XXX Refusing_Show XXX @ " + proposals.get(i).getKey() + " for " + proposals.get(i).getValue() + "$");

                    m.setContent("");
                }
                v.add(m);
            }

            //System.out.println("BAND: " + getLocalName() + " got " + proposals.size() + " replying to " + v.size());

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

        @Override
        public int onEnd() {
            //System.out.println("BAND: " + getLocalName() + " finished informing all venues. Ending...");
            myAgent.removeBehaviour(this);
            return 0;
        }
    }
}