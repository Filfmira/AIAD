package hospital.agents;

import java.util.List;

import hospital.Patologies;
import hospital.Treatment;
import hospital.Treatments;
import hospital.behaviours.BasePatientBehaviour;
import hospital.behaviours.InTreatmentPatientBehaviour;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class Patient extends Agent{
	
	boolean finished = false;
	private String patology;
	private Treatments treatments;
	private int priority;
	private int nextTreatmentIndex = 0; // inicialmente vai assumir ordem obrigatória nos tratamentos
	private boolean isInTreatment = false;
	// while in treatment, if patient gets hold of equipment for 
	// the next treatment, it stores that equipment's AID
	private AID nextTreatmentEquipment = null; 
	
	private long currentTreatmentStartTime; //milis
	private long currentTreatmentExpectedDuration; //seconds

	@Override
	protected void setup(){
		Object[] args = getArguments();
		if (args != null && args.length > 1) {
			this.patology = (String) args[0];
			//System.out.println("Patient patology is "+this.patology);
			
			this.priority = Integer.parseInt((String) args[1]);
			this.treatments = Patologies.get(this.patology);
			
			if(this.treatments == null){
				// Make the agent terminate
				System.out.println("No treatment for patology: " + this.patology+ " - " + this.getLocalName());
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
					//System.out.println("vou registar: "+ts.get(i).getName()+"-patient");
				}
				try {
					DFService.register(this, dfd);
				}
				catch (FIPAException fe) {
					System.out.println("Problem registering patient services ("+this.getLocalName()+")");
					fe.printStackTrace();
				}
			}
			
			//addBehaviour(new EquipmentSubscriptionBehaviour(this));
			addBehaviour(new BasePatientBehaviour(this));
			//addBehaviour(new ResourceScannerPatientBehaviour(this));
		}
		else {
			// Make the agent terminate
			System.out.println("No patology or priority specified - " + this.getLocalName());
			doDelete();
		}
		
	}
	
	public void modifyRegisterDF(){
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(this.getAID());
		
		List<Treatment> ts = this.treatments.getTreatments();
		
		if(this.nextTreatmentIndex != ts.size()){ //still treatments left
			int i = this.nextTreatmentIndex;
			if(this.nextTreatmentEquipment != null)
				i++;
			for(; i < ts.size(); i++){
				ServiceDescription sd = new ServiceDescription();
				sd.setType(ts.get(i).getName()+"-patient");
				sd.setName("EquipmentSubscription");
				dfd.addServices(sd);
				System.out.println("vou registar: "+ts.get(i).getName()+"-patient");
			}
		}		
		
		try {
			DFService.modify(this, dfd);
		}
		catch (FIPAException fe) {
			System.out.println("Problem modifying DF register at agent: "+this.getLocalName());
			fe.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @return this patient's proposal
	 */
	public double calculateNextTreatmentProposal(){
		double factor = 1;
		//System.out.println("next treatment time: "+this.getNextTreatment().getTime()*1000);
		//System.out.println("curr treatment time left: "+this.getCurrentTreatmentExpectedTimeLeft());
		if(this.getNextTreatment().getTime()*1000 < this.getCurrentTreatmentExpectedTimeLeft())
			factor = factor*0.1;
		
		return this.priority*factor;
	}
	
	
	/**
	 * time left for current treatment to end in milis
	 * @return
	 */
	public long getCurrentTreatmentExpectedTimeLeft(){
		return this.currentTreatmentExpectedDuration*1000 - 
				(System.currentTimeMillis() - this.currentTreatmentStartTime);
	}
	
	public long getCurrentTreatmentStartTime() {
		return currentTreatmentStartTime;
	}

	public void setCurrentTreatmentStartTime(long l) {
		this.currentTreatmentStartTime = l;
	}

	public long getCurrentTreatmentExpectedDuration() {
		return currentTreatmentExpectedDuration;
	}

	public void setCurrentTreatmentExpectedDuration(int currentTreatmentExpectedDuration) {
		this.currentTreatmentExpectedDuration = currentTreatmentExpectedDuration;
	}

	public void setInTreatment(boolean newIsInTreatment, AID currentEquipment) {
		this.isInTreatment = newIsInTreatment;		
		if(newIsInTreatment){ //if setting to true
			this.nextTreatmentIndex++;
			this.currentTreatmentExpectedDuration = this.treatments.getTreatments().get(nextTreatmentIndex-1).getTime();
			this.addBehaviour(new InTreatmentPatientBehaviour(this,currentEquipment)); 			
		}
		else if(this.nextTreatmentIndex == this.treatments.getTreatments().size()){ //if last treatment over, force next treatment
			this.doDelete();
		}
		
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
	
	public Treatments getTreatments() {
		return treatments;
	}

	public boolean isInTreatment() {
		return isInTreatment;
	}
	
	
	
	
	// Put agent clean-up operations here
	@Override
	protected void takeDown() {
		System.out.println("######################################");
		System.out.println("Patient " + this.getName() + " ended all treatments. Killing...");
		System.out.println("######################################");
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			System.out.println("problem Deregistering patient: " + this.getLocalName());
			e.printStackTrace();
		}
		
	}
}
