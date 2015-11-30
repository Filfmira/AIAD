package hospital.agents.behaviours;

import java.util.ArrayList;
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
	 * 1 -> cfp sent. receiving proposals
	 * 2 -> all proposals received. sending answers.
	 * 3 -> answers sent. waiting for patient confirmation
	 * 4 -> being used by patient 
	 */
	private int state = 0;
	
	private String replyWith = null;
	private MessageTemplate mt; // The template to receive replies
	private int proposalsSent; //number of Patients that CFP was sent to

	private List<AID> proposalsReceivedAIDs = new ArrayList<AID>();
	private int cfpResponsesReceived = 0;
	private AID bestProposalAID = null;
	private int bestProposal = 0;
	
	private boolean acceptedByPatient = false;
	private int finalAnswersReceived = 0;
	
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
			bestProposalAID = null;
			proposalsReceivedAIDs = new ArrayList<AID>();
			cfpResponsesReceived = 0;
			this.state++;
			break;
		case 1:
			receiveProposal();
			if(this.cfpResponsesReceived == this.proposalsSent)
				this.state++;
			break;
		case 2:
			acceptedByPatient = false;
			finalAnswersReceived = 0;
			sendProposalResponses();
			break;
		case 3:
			receiveFinalResponses();
			if(finalAnswersReceived == proposalsSent){
				if(acceptedByPatient)
					this.state++;
				else this.state = 0;
			}			
			break;		
		case 4:
			// wait for patient to check in??
			// + 
			// wait for treatment to finish.
			break;
		}
		
		
	}

	private void receiveFinalResponses() {
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			if(msg.getSender() == this.bestProposalAID)
				if(msg.getPerformative() == ACLMessage.INFORM){
					this.acceptedByPatient = true;
					((EquipmentAgent) myAgent).setCurrentPatient(msg.getSender());
				}
			this.finalAnswersReceived++;
		}
		else {
			block();
		}
	}

	private void sendProposalResponses() {
		// REJECT
		ACLMessage reject = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
		
		for (int j = 0; j < this.proposalsReceivedAIDs.size(); j++) {
			if(this.proposalsReceivedAIDs.get(j).equals(this.bestProposalAID))
				reject.addReceiver(this.proposalsReceivedAIDs.get(j));
		}
		reject.setConversationId("auction-equipment-usage");
		reject.setReplyWith(replyWith); // Unique value
		myAgent.send(reject);
		
		// ACCEPT 
		ACLMessage accept = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
		accept.addReceiver(this.bestProposalAID);
		accept.setConversationId("auction-equipment-usage");
		reject.setReplyWith(replyWith); // Unique value
		myAgent.send(accept);		
	}

	private void receiveProposal() {
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			if(msg.getPerformative() == ACLMessage.REFUSE){
				cfpResponsesReceived++;
			}
			else if (msg.getPerformative() == ACLMessage.PROPOSE){
				int proposal = Integer.parseInt(msg.getContent());
				if(proposal > this.bestProposal){
					this.bestProposal = proposal;
					this.bestProposalAID = msg.getSender();
				}
				this.proposalsReceivedAIDs.add(msg.getSender());
				cfpResponsesReceived++;
			}
		}
		else {
			block();
		}		
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
		cfp.setContent(((EquipmentAgent) myAgent).getTreatment().getName());
		cfp.setConversationId("auction-equipment-usage");
		myAgent.send(cfp);
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
