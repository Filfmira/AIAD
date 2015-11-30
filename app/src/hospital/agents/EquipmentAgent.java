package hospital.agents;

import java.util.ArrayList;
import java.util.List;

import hospital.Treatment;
import hospital.Treatments;
import hospital.agents.behaviours.BaseEquipmentBehaviour;
import hospital.agents.behaviours.SubscriptionHandlerBehaviour;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class EquipmentAgent extends Agent{
	
	private String name;
	private List<AID> subscribedPatients = new ArrayList<AID>();
	

	private Treatment treatment; 
	private AID currentPatient = null;
	
	// Put agent initializations here
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hello! Equipment-agent " + getAID().getName() + " is ready.");

		// Get the title of the book to buy as a start-up argument
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			this.name = (String) args[0];
			treatment = Treatments.get(this.name); 
			
			// Register the book-selling service in the yellow pages
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(this.getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setType(args[0]+"-equipment");
			System.out.println("advertising: "+args[0]+"-equipment");
			sd.setName("Hospital");
			dfd.addServices(sd);
			try {
				DFService.register(this, dfd);
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}
			
			addBehaviour(new SubscriptionHandlerBehaviour(this));
			addBehaviour(new BaseEquipmentBehaviour(this));
		}
		else{
			System.out.println("No service described in equipment agent "+this.getName());
		}
				
	}
	
	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Equipment-agent "+getAID().getName()+" terminating.");
	}
	
	public void addSubscribedAgent(AID aid){
		this.subscribedPatients.add(aid);
	}

	public AID getCurrentPatient() {
		return currentPatient;
	}

	public void setCurrentPatient(AID currentPatient) {
		this.currentPatient = currentPatient;
	}
	
	public List<AID> getSubscribedPatients() {
		return subscribedPatients;
	}
}
