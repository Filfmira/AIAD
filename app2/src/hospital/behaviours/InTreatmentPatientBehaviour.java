package hospital.behaviours;

import hospital.agents.Equipment;
import hospital.agents.Patient;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class InTreatmentPatientBehaviour extends Behaviour{

	private boolean ended = false;
	
	public InTreatmentPatientBehaviour(Patient patient, AID currentEquipment) {
		super(patient);
		
		sendStartToEquipment(currentEquipment);
	}

	private void sendStartToEquipment(AID currentEquipment) {
		ACLMessage startMsg = new ACLMessage(ACLMessage.INFORM);
		startMsg.addReceiver(currentEquipment);
		startMsg.setContent("start-treatment");
		myAgent.send(startMsg);		
		((Patient) myAgent).setCurrentTreatmentStartTime(System.currentTimeMillis());
	}

	@Override
	public void action() {
		MessageTemplate mt = MessageTemplate.and( MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchConversationId("treatment-ended"));
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			if (((Patient) myAgent).getNextTreatmentEquipment() == null){
				((Patient) myAgent).setInTreatment(false, null);
			}
			else{
				//if patient already as hold of next treatment's equipment
				((Patient) myAgent).setInTreatment(true, ((Patient) myAgent).getNextTreatmentEquipment());
				((Patient) myAgent).setNextTreatmentEquipment(null);
			}
			((Patient) myAgent).healthImprovement(Integer.parseInt(msg.getContent()));
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
