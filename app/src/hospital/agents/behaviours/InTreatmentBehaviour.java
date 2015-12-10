package hospital.agents.behaviours;

import hospital.agents.PatientAgent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class InTreatmentBehaviour extends Behaviour {

	private static final long serialVersionUID = 1L;
	
	private boolean ended = false;
	
	public InTreatmentBehaviour(PatientAgent patientAgent) {
		this.myAgent = patientAgent;
	}

	@Override
	public void action() {
		MessageTemplate mt = MessageTemplate.and( MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchConversationId("treatment-ended"));
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			System.out.println("received ended treatment no patient");
			if (((PatientAgent) myAgent).getNextTreatment() == null){
				((PatientAgent) myAgent).setInTreatment(false);
			}
			else{
				//if patient already as hold of next treatment's equipment
				((PatientAgent) myAgent).setInTreatment(true);
				((PatientAgent) myAgent).setNextTreatmentEquipment(null);
			}
			
			this.ended = true;
		}
		else {
			System.out.println("received something else no patient");
			block();
		}		

	}

	@Override
	public boolean done() {
		return ended;
	}

}
