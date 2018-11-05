package agents;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

public class Band extends Agent {

    private int genre;
    private int prestige;
    private int min_price;
    private int min_attendance;

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

    public void setup() {
        registerToDFService();
        setBandInformation();
        printBandInformation();


        addBehaviour(new ReceiveVenueRequest(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)));
        System.out.println(getLocalName() + ": starting to work");

    }

    private void setBandInformation() {
        setGenre((int)getArguments()[1]);
        setPrestige((int)getArguments()[2]);
        setMin_price((int)getArguments()[3]);
        setMin_attendance((int)getArguments()[4]);
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

        System.out.println(getLocalName() + ": done working");
    }

    private void unregisterFromDFService() {
        try {
            DFService.deregister(this);
        } catch(FIPAException e) {
            e.printStackTrace();
        }
    }

    class ReceiveVenueRequest extends ContractNetResponder {

        public ReceiveVenueRequest(Agent a, MessageTemplate mt) {
            super(a, mt);
        }


        protected ACLMessage handleCfp(ACLMessage cfp) {
            ACLMessage reply = cfp.createReply();
            reply.setPerformative(ACLMessage.PROPOSE);
            reply.setContent("I will do it for free!!!");
            // ...
            return reply;
        }

        protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
            System.out.println(myAgent.getLocalName() + " got a reject...");
        }

        protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
            System.out.println(myAgent.getLocalName() + " got an accept!");
            ACLMessage result = accept.createReply();
            result.setPerformative(ACLMessage.INFORM);
            result.setContent("this is the result");

            return result;
        }
    }
}