package hospital.behaviours;

import hospital.agents.Equipment;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class WaitForPatientStartBehaviour extends Behaviour {

	private AID patientAID;
	private boolean finished = false;
	
	public WaitForPatientStartBehaviour(Equipment eq, AID bestProposalAID) {
		super(eq);
		this.patientAID = bestProposalAID;
	}

	@Override
	public void action() {
		MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchSender(this.patientAID),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msg = this.myAgent.receive(mt);
		if (msg != null){
			if (msg.getContent().equals("start-treatment")){
				((Equipment) myAgent).startBusyGuiCountdown();
				this.finished = true;
			}
			else{
				System.out.println("UNEXPECTED MESSAGE RECEIVED! Behaviour of the program may be affected. In: " + myAgent.getLocalName());
			}
		}
		else block();

	}

	@Override
	public boolean done() {
		return finished;
	}

}
