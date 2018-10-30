package agents;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;

public class Spectator extends Agent {

    private int budget;
    private int min_genre_spectrum;
    private int max_genre_spectrum;
    private int location;

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

    public void setup() {
        addBehaviour(new WorkingBehaviour());
        System.out.println(getLocalName() + ": starting to work");
    }

    public void takeDown() {
        System.out.println(getLocalName() + ": done working");
    }

    class WorkingBehaviour extends Behaviour {
        public void action() {
            //System.out.println("lul");
        }
        public boolean done() {
            return true;
        }
    }

}