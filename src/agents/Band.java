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

    private int genre;
    private int prestige;
    private int min_price;
    private int min_attendance;
    private int current_shows;
    private ArrayList<Pair<String, Integer>> all_proposals;
    private int business_cards_handed;


    @Override
    public String toString() {
        return String.format("Band - %1$-15s", this.getAID().getLocalName())
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
        if(Utils.DEBUG)
            printBandInformation();
        registerToDFService();

        addBehaviour(new RequestResponder(this, MessageTemplate.MatchPerformative(ACLMessage.REQUEST)));
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
     *  Venue request responder
     */
    class RequestResponder extends AchieveREResponder {

        public RequestResponder(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        protected ACLMessage handleRequest(ACLMessage request) throws RefuseException {
            if(Utils.DEBUG)
                System.out.println(getLocalName() + " received " + request.getContent() + " from " + request.getSender().getLocalName());
            ACLMessage reply = request.createReply();

            switch (request.getOntology()) {
                case "Give_BusinessCard":
                    if(Utils.DEBUG)
                        System.out.println(getLocalName() + "says: I'll give you my business card!");

                    String[] tokens = request.getContent().split("::");
                    int attendance = Integer.parseInt(tokens[0]);
                    int min_genre_spectrum = Integer.parseInt(tokens[1]);
                    int max_genre_spectrum = Integer.parseInt(tokens[2]);

                    if (evaluateAcceptance(attendance, min_genre_spectrum, max_genre_spectrum) && current_shows < Utils.MAX_SHOWS_PER_BAND) {
                        reply.setPerformative(ACLMessage.AGREE);
                        reply.setOntology("Give_BusinessCard");
                    } else
                        throw new RefuseException("Refused Request");

                    break;

                case "Hiring":
                    if(Utils.DEBUG)
                        System.out.println(getLocalName() + " says THEN GIB THE MONEIS!!!!");

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

        private boolean evaluateAcceptance(int attendance, int min_genre_spectrum, int max_genre_spectrum) {
            if (attendance >= min_attendance && min_genre_spectrum <= genre && genre <= max_genre_spectrum)
                return true;
            return false;
        }

        protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) {
            ACLMessage result = request.createReply();

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

                    if (business_cards_handed == all_proposals.size()) {
                        //System.out.println(getLocalName() + " RESPONDER A TODAS AS VENUES");
                        decideWhereToPlay();

                        // TODO: mandar "request" para as venues a aceitar as propostas
                        //addBehaviour(new ConfirmShow(this.myAgent, null));
                    }

                    break;
            }

            return result;
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
    }

    /**
     *  Confirm Shows to Venue
     */
    class ConfirmShow extends AchieveREInitiator {

        public ConfirmShow(Agent a, ACLMessage msg) {
            super(a, msg);
        }


        protected Vector<ACLMessage> prepareRequests(ACLMessage msg) {
            Vector<ACLMessage> v = new Vector<>();

            for (int i = 0; i < all_proposals.size(); i++) {
                ACLMessage m = new ACLMessage(ACLMessage.REQUEST);

                if (current_shows < Utils.MAX_SHOWS_PER_BAND && all_proposals.get(i).getValue()>min_price) {
                    m.setOntology("Confirming_Presence");
                    m.addReceiver(new AID(all_proposals.get(i).getKey(), false));
                    if(Utils.DEBUG)
                        System.out.println(getLocalName() + " vvv Confirming_Presence vvv @ " + all_proposals.get(i).getKey() + " for " + all_proposals.get(i).getValue() + "$");
                    String content = getLocalName() + "::" + all_proposals.get(i).getValue();
                    m.setContent(content);
                } else {
                    m.setOntology("Refusing_Show");
                    m.addReceiver(new AID(all_proposals.get(i).getKey(), false));
                    if(Utils.DEBUG)
                        System.out.println(getLocalName() + " XXX Refusing_Show XXX @ " + all_proposals.get(i).getKey() + " for " + all_proposals.get(i).getValue() + "$");
                    m.setContent("");
                }

                v.add(m);
            }

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
}