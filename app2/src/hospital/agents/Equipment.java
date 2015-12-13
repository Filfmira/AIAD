package hospital.agents;

import hospital.Treatment;
import hospital.Treatments;
import hospital.behaviours.BaseEquipmentBehaviour;
import hospital.gui.EquipmentBusyGui;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class Equipment extends Agent{

	private String name;
	private Treatment treatment;
	private AID currentPatient = null;
	private EquipmentBusyGui busyGui = null;
	
	private long treatmentStartTime;
	
	// Put agent initializations here
	@Override
	protected void setup() {
		// Printout a welcome message
		//System.out.println("Hello! Equipment-agent " + getAID().getName() + " is ready.");

		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			this.name = (String) args[0];
			this.treatment = Treatments.get(this.name); 
						
			//addBehaviour(new SubscriptionHandlerBehaviour(this));
			addBehaviour(new BaseEquipmentBehaviour(this));
		}
		else{
			System.out.println("No service described in equipment agent "+this.getLocalName());
		}
				
	}
	
	
	public DFAgentDescription[] getSubscribedPatients() {
		//return subscribedPatients;
		String patientAdvertise = this.treatment.getName()+"-patient";
		//System.out.println("vou procurar: "+treatmentRegisterName);
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(patientAdvertise);
		template.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(this, template);
			return result;
		}
		catch(Exception e){
			System.out.println("Problem getting subscribed agents....");
			e.printStackTrace();
		}
		return null;
			
	}
	
	public void startBusyGui(){
		this.busyGui = new EquipmentBusyGui(this);
		this.busyGui.showGui();
	}
	
	/**
	 * called when patient starts treatment.
	 * @return true if gui exists, false otherwise.
	 */
	public boolean startBusyGuiCountdown(){
		if (this.busyGui == null)
			return false;
		this.busyGui.startTimer();
		this.treatmentStartTime = System.currentTimeMillis();
		return true;
	}
	
	/**
	 * Method called at the end of a treatment. INFORM message will be sent to Patient agent,
	 * informing that treatment has ended.
	 */
	public void finishTreatment() {
		// treatment duration will be modified acording to usual times in actual treatments.
		long endTime = System.currentTimeMillis();
		long duration = (endTime - this.treatmentStartTime)/1000;
		Treatments.get(this.getTreatment().getName()).updateDuration(duration);
		
		this.busyGui.dispose();
		this.busyGui = null;
		ACLMessage end_treatment = new ACLMessage(ACLMessage.INFORM);
		end_treatment.addReceiver(this.currentPatient);
		end_treatment.setConversationId("treatment-ended");
		this.send(end_treatment);

		this.currentPatient = null;
		//addBehaviour(new BaseEquipmentBehaviour(this));
	}
	
	
	public AID getCurrentPatient() {
		return this.currentPatient;
	}
	
	public void setCurrentPatient(AID currentPatient) {
		this.currentPatient = currentPatient;
	}
	
	public Treatment getTreatment() {
		return treatment;
	}
	
	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Equipment-agent "+getAID().getName()+" terminating.");
		this.doDelete();
	}
}
