package hospital.agents.behaviours;

import hospital.agents.EquipmentAgent;
import hospital.agents.PatientAgent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BasePatientBehaviour extends Behaviour {

	private MessageTemplate mt; // The template to receive CFP
	private int state = 0;

	public BasePatientBehaviour(PatientAgent a) {
		// TODO Auto-generated constructor stub
		myAgent = a;
	}

	@Override
	public void action() {
		switch(this.state){
		case 0: 
			initialize();
			state++;
			break;
		case 1:
			receiveCFP();
			break;
		case 2:
			receiveProposeResponse();
			break;
		case 3:
			((PatientAgent) myAgent).setInTreatment(true);
			state = 0;
			break;
		}

	}


	private void receiveProposeResponse() {
		//receive 
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			
			if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
				// send INFORM
				ACLMessage cfp = new ACLMessage(ACLMessage.INFORM);
				cfp.setReplyWith(msg.getReplyWith()); // Unique value
				cfp.addReceiver(msg.getSender());
				cfp.setConversationId("auction-equipment-usage");
				myAgent.send(cfp);
				this.state++;
			}
			else if(msg.getPerformative() == ACLMessage.REJECT_PROPOSAL){
				this.state = 0;
			}
		}
		else {
			block();
		}		
		
	}

	private void receiveCFP() {
		if(((PatientAgent) myAgent).isInTreatment())
			return;
		
		//receive 
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			String nextTreatment = ((PatientAgent) myAgent).getNextTreatment().getName();
			
			// check if next treatment is the same as the received CFP 
			if(nextTreatment.equals(msg.getContent())){
				// send PROPOSAL
				ACLMessage cfp = new ACLMessage(ACLMessage.PROPOSE);
				cfp.setReplyWith(msg.getReplyWith()); // Unique value
				cfp.addReceiver(msg.getSender());
				cfp.setContent(Integer.toString(((PatientAgent) myAgent).getPriority()));
				cfp.setConversationId("auction-equipment-usage");
				myAgent.send(cfp);
				this.state++;
				mt = MessageTemplate.and(MessageTemplate.MatchReplyWith(msg.getReplyWith()),
						MessageTemplate.MatchConversationId("auction-equipment-usage"));
			}
			else{
				// send REFFUSE
				ACLMessage cfp = new ACLMessage(ACLMessage.REFUSE);
				cfp.setReplyWith(msg.getReplyWith()); // Unique value
				cfp.addReceiver(msg.getSender());
				cfp.setConversationId("auction-equipment-usage");
				myAgent.send(cfp);
				this.state = 0;
			}
		}
		else {
			block();
		}		
		
	}
	
	private void initialize() {
		mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.CFP),
				MessageTemplate.MatchConversationId("auction-equipment-usage"));		
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
