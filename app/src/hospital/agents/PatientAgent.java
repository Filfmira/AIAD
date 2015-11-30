package hospital.agents;

import hospital.Patologies;
import hospital.Treatments;
import hospital.agents.behaviours.BasePatientBehaviour;
import hospital.agents.behaviours.EquipmentSubscriptionBehaviour;
import jade.core.Agent;

public class PatientAgent extends Agent{

	private String patology;
	private Treatments treatment;
	private int priority;

	protected void setup() {
		// Printout a welcome message
		System.out.println("Hello! Patient-agent " + getAID().getName() + " is ready.");

		// Get the title of the book to buy as a start-up argument
		Object[] args = getArguments();
		if (args != null && args.length > 1) {
			this.patology = (String) args[0];
			System.out.println("Patient patology is "+this.patology);
			
			this.priority = Integer.parseInt((String) args[1]);
			this.treatment = Patologies.get(this.patology);
			
			if(this.treatment == null){
				// Make the agent terminate
				System.out.println("No treatment for patology: " + this.patology);
				doDelete();
			}
			
			addBehaviour(new EquipmentSubscriptionBehaviour(this));
			addBehaviour(new BasePatientBehaviour(this));
			//addBehaviour(new ResourceScannerPatientBehaviour(this));
		}
		else {
			// Make the agent terminate
			System.out.println("No patology or priority specified");
			doDelete();
		}
	}
	
	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Patient-agent "+getAID().getName()+" terminating.");
	}
	
	public Treatments getTreatment() {
		return treatment;
	}

	
}
