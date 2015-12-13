package hospital.behaviours;

import hospital.Treatment;
import hospital.agents.Patient;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BasePatientBehaviour extends Behaviour {

	private String replyWith = null;
	
	private int state = 0;
	
	public BasePatientBehaviour(Patient a) {
		super(a);
	}
	
	@Override
	public void action() {
		System.out.println("state in "+myAgent.getLocalName() + ": "+this.state);
		switch(this.state){
		case 0: 
			receiveCFPAndAnswer();
			break;
		case 1:
			receiveEquipmentVeredictAndAnswer();
			break;
		}
	}

	private void receiveEquipmentVeredictAndAnswer() {
		MessageTemplate mt = MessageTemplate.MatchReplyWith(replyWith);
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
				// send INFORM
				ACLMessage cfp = new ACLMessage(ACLMessage.INFORM);
				cfp.setReplyWith(msg.getReplyWith()); // Unique value
				cfp.addReceiver(msg.getSender());
				myAgent.send(cfp);
				
				// set in treatment
				if(((Patient) myAgent).isInTreatment())
					((Patient) myAgent).setNextTreatmentEquipment(msg.getSender());
				else ((Patient) myAgent).setInTreatment(true);
				
				// patient already has the equipment, so it 
				// needs to deregister the "need for equipment" from de DF
				((Patient) this.myAgent).modifyRegisterDF();
			}
			else{ 
				if(msg.getPerformative() != ACLMessage.REJECT_PROPOSAL){
					System.out.println("Unexpected response type in agent: "+this.myAgent.getLocalName());
					System.out.println("	expected: ACCEPT_PROPOSAL or REJECT_PROPOSAL");
					System.out.println("	got: " + msg.getPerformative());
				}
			}
			
			this.state = 0;
		}
		else block();
	}

	private void receiveCFPAndAnswer() {
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			Treatment treatment = ((Patient) myAgent).getNextTreatment();
			
			if (treatment != null 
					&& treatment.getName().equals(msg.getContent())
					&& ((Patient) myAgent).getNextTreatmentEquipment() == null){
				this.replyWith = msg.getReplyWith();
				
				// send PROPOSAL
				ACLMessage cfp = new ACLMessage(ACLMessage.PROPOSE);
				cfp.setReplyWith(this.replyWith); // Unique value
				cfp.addReceiver(msg.getSender());
				cfp.setContent(Integer.toString(((Patient) myAgent).getPriority()));
				myAgent.send(cfp);
				this.state++;
				mt = MessageTemplate.MatchReplyWith(msg.getReplyWith());
				
			}
			else {
				// send REFFUSE
				ACLMessage cfp = new ACLMessage(ACLMessage.REFUSE);
				cfp.setReplyWith(msg.getReplyWith()); // Unique value
				cfp.addReceiver(msg.getSender());
				myAgent.send(cfp);
				this.state = 0;
			}
		}
		else block();
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
