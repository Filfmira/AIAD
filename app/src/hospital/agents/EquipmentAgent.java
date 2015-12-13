package hospital.agents;

import java.util.ArrayList;
import java.util.List;

import hospital.Treatment;
import hospital.Treatments;
import hospital.agents.behaviours.BaseEquipmentBehaviour;
import hospital.agents.behaviours.SubscriptionHandlerBehaviour;
import hospital.agents.gui.EquipmentBusyGui;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class EquipmentAgent extends Agent{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String name;
	private List<AID> subscribedPatients = new ArrayList<AID>();
	

	private Treatment treatment; 
	private AID currentPatient = null;
	
	private EquipmentBusyGui busyGui;
	
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
			
			//addBehaviour(new SubscriptionHandlerBehaviour(this));
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
		this.subscribedPatients.remove(currentPatient);
	}
	
	public DFAgentDescription[] getSubscribedPatients() {
		//return subscribedPatients;
		String treatmentRegisterName = this.treatment.getName()+"-patient";
		System.out.println("vou procurar: "+treatmentRegisterName);
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(treatmentRegisterName);
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

	public Treatment getTreatment() {
		return treatment;
	}

	public void startBaseBehaviour() {

	}
	
	public void startGui(){
		this.busyGui = new EquipmentBusyGui(this);
		this.busyGui.showGui();
	}

	public void finishTreatment() {
		this.busyGui.dispose();
		
		ACLMessage end_treatment = new ACLMessage(ACLMessage.INFORM);
		end_treatment.addReceiver(this.currentPatient);
		end_treatment.setConversationId("treatment-ended");
		this.send(end_treatment);

		this.currentPatient = null;
		//addBehaviour(new BaseEquipmentBehaviour(this));
	}
}
