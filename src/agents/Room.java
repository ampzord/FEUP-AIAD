package agents;
import java.util.ArrayList;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;

public class Room extends Agent {
   
    private int capacity;
    private ArrayList<String> characteristics;
    private int budget;
 
    public void setup() {
        addBehaviour(new WorkingBehaviour());
        System.out.println(getLocalName() + ": starting to work");
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    public void setBudget(int budget) {
        this.budget = budget;
    }
    public boolean addCharacteristic(String characteristic) {
        if(!this.characteristics.contains(characteristic)) {
            this.characteristics.add(characteristic);
            return true;
        }
        return false;
    }
    public int getCapacity() {
        return this.capacity;
    }
    public int getBudget() {
        return this.budget;
    }
    public ArrayList<String> getCharacteristics() {
        return this.characteristics;
    }
   
   
   
    public void takeDown() {
        System.out.println(getLocalName() + ": done working");
    }
   
    class WorkingBehaviour extends Behaviour {
        private int n = 0;
       
        public void action() {
            System.out.println(++n + " doing something");
        }
       
        public boolean done() {
            return n == 3;
        }
    }
       
}