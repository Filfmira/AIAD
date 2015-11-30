package hospital.agents.behaviours;

import java.util.List;

import hospital.agents.EquipmentAgent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BaseEquipmentBehaviour extends Behaviour {

	/**
	 * States: 
	 * 0 -> waiting for patient
	 * 1 -> receiving proposals
	 * 2 -> waiting for patient confirmation
	 * 3 -> being used by patient 
	 */
	private int state = 0;
	
	private String replyWith = null;
	private MessageTemplate mt; // The template to receive replies
	private int proposalsSent; //number of Patients that CFP was sent to
	private int proposalsReceived;
	private int bestProposal = 0;
	
	public BaseEquipmentBehaviour(EquipmentAgent equipmentAgent) {
		myAgent = equipmentAgent;
		state = 0;
	}

	@Override
	public void action() {
		/*ACLMessage.CFP;
		ACLMessage.PROPOSE; ACLMessage.REFUSE;
		ACLMessage.ACCEPT_PROPOSAL; ACLMessage.REJECT_PROPOSAL;
		ACLMessage.INFORM; ACLMessage.FAILURE;*/
		switch(this.state){
		case 0: 
			initiateContractNet();
			bestProposal = 0;
			this.state++;
			break;
		case 1:
			receiveProposal();
			if(this.proposalsReceived == this.proposalsSent)
				this.state++;
			break;
		case 2:
			
			break;
		case 3:
			
			break;
		}
		
		
	}

	private void receiveProposal() {
		// TODO Auto-generated method stub
		
	}

	private void initiateContractNet() {
		List<AID> patients = ((EquipmentAgent) myAgent).getSubscribedPatients();
		ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
		
		replyWith = "cfp"+System.currentTimeMillis(); // Unique value
		mt = MessageTemplate.and(MessageTemplate.MatchConversationId("auction-equipment-usage"),
				MessageTemplate.MatchInReplyTo(replyWith));
		
		cfp.setReplyWith(replyWith); // Unique value
		for(int i = 0; i < patients.size(); i++){
			cfp.addReceiver(patients.get(i));
		}
		this.proposalsSent = patients.size();
		cfp.setContent("");
		cfp.setConversationId("auction-equipment-usage");
		myAgent.send(cfp);
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
