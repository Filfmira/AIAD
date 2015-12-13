package hospital.agents;

import java.util.List;

import hospital.Patologies;
import hospital.Treatment;
import hospital.Treatments;
import hospital.agents.behaviours.BasePatientBehaviour;
import hospital.agents.behaviours.EquipmentSubscriptionBehaviour;
import hospital.agents.behaviours.InTreatmentBehaviour;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class PatientAgent extends Agent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String patology;
	private Treatments treatments;
	private int priority;
	private int nextTreatmentIndex = 0; // inicialmente vai assumir ordem obrigatória nos tratamentos
	private boolean isInTreatment = false;
	
	private boolean finished = false;
	
	// while in treatment, if patient gets hold of equipment for 
	// the next treatment, it stores that equipment's AID
	private AID nextTreatmentEquipment = null; 
	
	public boolean isFinished(){
		return this.finished;
	}
	
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
			else{
				DFAgentDescription dfd = new DFAgentDescription();
				dfd.setName(this.getAID());
				
				List<Treatment> ts = this.treatments.getTreatments();
				for(int i = 0; i < ts.size(); i++){
					ServiceDescription sd = new ServiceDescription();
					sd.setType(ts.get(i).getName()+"-patient");
					sd.setName("EquipmentSubscription");
					dfd.addServices(sd);
					System.out.println("vou registar: "+ts.get(i).getName()+"-patient");
				}
				try {
					DFService.register(this, dfd);
				}
				catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}
			
			//addBehaviour(new EquipmentSubscriptionBehaviour(this));
			addBehaviour(new BasePatientBehaviour(this));
			//addBehaviour(new ResourceScannerPatientBehaviour(this));
		}
		else {
			// Make the agent terminate
			System.out.println("No patology or priority specified");
			doDelete();
		}
	}
	
	public void modifyRegisterDF(){
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(this.getAID());
		
		List<Treatment> ts = this.treatments.getTreatments();
		for(int i = this.nextTreatmentIndex; i < ts.size(); i++){
			ServiceDescription sd = new ServiceDescription();
			sd.setType(ts.get(i).getName()+"-patient");
			sd.setName("EquipmentSubscription");
			dfd.addServices(sd);
			System.out.println("vou registar: "+ts.get(i).getName()+"-patient");
		}
		try {
			DFService.modify(this, dfd);
		}
		catch (FIPAException fe) {
			System.out.println("Problem modifying DF register at agent: "+this.getLocalName());
			fe.printStackTrace();
		}
	}
	
	public void deregisterTreatment(String treatmentName){
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(this.getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType(treatmentName+"-patient");
		sd.setName("EquipmentSubscription");
		dfd.addServices(sd);
		try {
			DFService.deregister(this, dfd);
		} catch (FIPAException e) {
			System.out.println("Error deregistering treatment ("+treatmentName+") from DF (patient: "
				+this.getLocalName());
			e.printStackTrace();
		}
	}
	
	// Put agent clean-up operations here
	@Override
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Patient-agent "+getAID().getName()+" terminating.");
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			System.out.println("problem Deregistering patient: " + this.getLocalName());
			e.printStackTrace();
		}
		doDelete(); // ?
	}
	
	public Treatments getTreatment() {
		return treatments;
	}

	public boolean isInTreatment() {
		return isInTreatment;
	}

	public void setInTreatment(boolean isInTreatment) {
		if(isInTreatment){ //if setting to true
			this.nextTreatmentIndex++;
			this.addBehaviour(new InTreatmentBehaviour(this)); 
		}
		else if(this.nextTreatmentIndex == this.treatments.getTreatments().size()){ //if last treatment over, force next treatment
			System.out.println("######################################");
			System.out.println("Patient " + this.getName() + " ended all treatments. Killing...");
			System.out.println("######################################");
			this.takeDown();
		}
		this.isInTreatment = isInTreatment;
	}

	
	public Treatment getNextTreatment(){
		if(this.nextTreatmentIndex == this.treatments.getTreatments().size())
			return null;
		return this.treatments.getTreatments().get(this.nextTreatmentIndex);
	}
	
	public Treatment getCurrentTreatment(){
		if(this.nextTreatmentIndex == 0)
			return null;
		return this.treatments.getTreatments().get(this.nextTreatmentIndex-1);
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public AID getNextTreatmentEquipment() {
		return nextTreatmentEquipment;
	}

	public void setNextTreatmentEquipment(AID nextTreatmentEquipment) {
		this.nextTreatmentEquipment = nextTreatmentEquipment;
	}

	
}
