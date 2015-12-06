package hospital.agents;

import hospital.Patologies;
import hospital.Treatment;
import hospital.Treatments;
import hospital.agents.behaviours.BasePatientBehaviour;
import hospital.agents.behaviours.EquipmentSubscriptionBehaviour;
import hospital.agents.behaviours.InTreatmentBehaviour;
import jade.core.Agent;

public class PatientAgent extends Agent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String patology;
	private Treatments treatments;
	private int priority;
	private int nextTreatmentIndex = 0; //inicialmente vai assumir ordem obrigatória nos tratamentos
	private boolean isInTreatment = false;
	
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hello! Patient-agent " + getAID().getName() + " is ready.");

		// Get the title of the book to buy as a start-up argument
		Object[] args = getArguments();
		if (args != null && args.length > 1) {
			this.patology = (String) args[0];
			System.out.println("Patient patology is "+this.patology);
			
			this.priority = Integer.parseInt((String) args[1]);
			this.treatments = Patologies.get(this.patology);
			
			if(this.treatments == null){
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
		return treatments;
	}

	public boolean isInTreatment() {
		return isInTreatment;
	}

	public void setInTreatment(boolean isInTreatment) {
		if(isInTreatment){ //if setting to true
			this.nextTreatment();
			this.addBehaviour(new InTreatmentBehaviour(this)); 
		}
		else if(this.nextTreatmentIndex + 1 == this.treatments.getTreatments().size()) //if last treatment over, force next treatment
			this.nextTreatment();
		this.isInTreatment = isInTreatment;
	}
	
	public void nextTreatment(){
		this.nextTreatmentIndex++;
		this.isInTreatment = false;
		if(this.nextTreatmentIndex == this.treatments.getTreatments().size()){
			System.out.println("######################################");
			System.out.println("Patient " + this.getName() + " ended all treatments. Killing...");
			System.out.println("######################################");
			this.takeDown();
		}		
	}
	
	public Treatment getNextTreatment(){
		return this.treatments.getTreatments().get(this.nextTreatmentIndex);
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	
}
