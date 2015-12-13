package hospital.behaviours;

import hospital.agents.Patient;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class InTreatmentPatientBehaviour extends Behaviour{

	private boolean ended = false;
	
	public InTreatmentPatientBehaviour(Patient patient) {
		super(patient);
	}

	@Override
	public void action() {
		MessageTemplate mt = MessageTemplate.and( MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchConversationId("treatment-ended"));
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			if (((Patient) myAgent).getNextTreatmentEquipment() == null){
				((Patient) myAgent).setInTreatment(false);
			}
			else{
				//if patient already as hold of next treatment's equipment
				((Patient) myAgent).setInTreatment(true);
				((Patient) myAgent).setNextTreatmentEquipment(null);
			}
			
			this.ended = true;
		}
		else {
			block();
		}		
		
	}

	@Override
	public boolean done() {
		return ended;
	}

}
