package hospital.agents.behaviours;

import java.util.ArrayList;
import java.util.List;

import hospital.agents.EquipmentAgent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BaseEquipmentBehaviour extends Behaviour {

	private static final long serialVersionUID = -1736761707583385302L;

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
	//private MessageTemplate mt; // The template to receive replies
	private int proposalsSent; //number of Patients that CFP was sent to

	private List<AID> proposalsReceivedAIDs = new ArrayList<AID>();
	private int cfpResponsesReceived = 0;
	private AID bestProposalAID = null;
	private int bestProposal = 0;
	
	private boolean acceptedByPatient = false;
	
	public BaseEquipmentBehaviour(EquipmentAgent equipmentAgent) {
		myAgent = equipmentAgent;
		state = 0;
	}

	@Override
	public void action() {
		System.out.println("STATE: "+state+" -> "+myAgent.getName());
		/*ACLMessage.CFP;
		ACLMessage.PROPOSE; ACLMessage.REFUSE;
		ACLMessage.ACCEPT_PROPOSAL; ACLMessage.REJECT_PROPOSAL;
		ACLMessage.INFORM; ACLMessage.FAILURE;*/
		switch(this.state){
		case 0:
			if( ((EquipmentAgent) myAgent).getCurrentPatient() == null &&
			((EquipmentAgent) myAgent).getSubscribedPatients().size() > 0){
				System.out.println("  ## iniciando contract net "+myAgent.getLocalName());
				initiateContractNet();
				bestProposal = 0;
				bestProposalAID = null;
				proposalsReceivedAIDs = new ArrayList<AID>();
				cfpResponsesReceived = 0;
				this.state++;
			}
			else block();			
			break;
		case 1:
			receiveProposal();
			if(this.cfpResponsesReceived == this.proposalsSent)
				this.state++;
			else System.out.println("## not enough responses " + myAgent.getLocalName());
			break;
		case 2:
			acceptedByPatient = false;
			sendProposalResponses();
			this.state++;
			break;
		case 3:
			if (receiveFinalResponses()){
				if(acceptedByPatient)
					this.state++;
				else this.state = 0;
			}
			break;		
		case 4:
			((EquipmentAgent) myAgent).startGui(); //operator will have to press the button on the GUI to end treatment
			state = 0;
			break;
		}
		
		
	}

	private boolean receiveFinalResponses() {
		MessageTemplate mt = MessageTemplate.MatchReplyWith(this.replyWith);
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			if(msg.getSender().equals(this.bestProposalAID) && msg.getPerformative() == ACLMessage.INFORM){
				this.acceptedByPatient = true;
				((EquipmentAgent) myAgent).setCurrentPatient(msg.getSender());
			}
			return true;
		}
		else {
			block();
			return false;
		}
	}

	private void sendProposalResponses() {
		// REJECT
		ACLMessage reject = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
		if(this.proposalsReceivedAIDs.size() > 1){ //if only one proposal, there are no rejects
			for (int j = 0; j < this.proposalsReceivedAIDs.size(); j++) {
				if(!this.proposalsReceivedAIDs.get(j).equals(this.bestProposalAID))
					reject.addReceiver(this.proposalsReceivedAIDs.get(j));
			}
			
			reject.setConversationId("auction-equipment-usage");
			reject.setReplyWith(replyWith); // Unique value
			myAgent.send(reject);
		}
		
		// ACCEPT 
		ACLMessage accept = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
		accept.addReceiver(this.bestProposalAID);
		accept.setConversationId("auction-equipment-usage");
		accept.setReplyWith(replyWith); // Unique value
		myAgent.send(accept);		
	}

	private void receiveProposal() {
		MessageTemplate mt = MessageTemplate.MatchReplyWith(this.replyWith);
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			System.out.println("||||received proposal? "+msg.getReplyWith() + " - " + myAgent.getLocalName());
			if(msg.getPerformative() == ACLMessage.REFUSE){
				System.out.println("||||received refuse "+msg.getReplyWith() + " - " + myAgent.getLocalName());
				cfpResponsesReceived++;
			}
			else if (msg.getPerformative() == ACLMessage.PROPOSE){
				System.out.println("||||received propose "+msg.getReplyWith() + " - " + myAgent.getLocalName());
				int proposal = Integer.parseInt(msg.getContent());
				if(proposal > this.bestProposal){
					this.bestProposal = proposal;
					this.bestProposalAID = msg.getSender();
				}
				this.proposalsReceivedAIDs.add(msg.getSender());
				cfpResponsesReceived++;
			}
			System.out.println("received qq coisa nas proposes");
		}
		else {
			System.out.println(" ## msg null??? "+myAgent.getLocalName());
			block();
		}		
	}

	private void initiateContractNet() {
		List<AID> patients = ((EquipmentAgent) myAgent).getSubscribedPatients();
		ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
		
		replyWith = "cfp"+System.currentTimeMillis(); // Unique value
		//mt = MessageTemplate.MatchInReplyTo(replyWith);
		System.out.println("vou receber replyWith: #"+ this.replyWith+"#");
		cfp.setReplyWith(replyWith); // Unique value
		for(int i = 0; i < patients.size(); i++){
			cfp.addReceiver(patients.get(i));
		}
		this.proposalsSent = patients.size();
		cfp.setContent(((EquipmentAgent) myAgent).getTreatment().getName());
		//cfp.setConversationId("auction-equipment-usage");
		myAgent.send(cfp);
	}

	@Override
	public boolean done() {
		return state == 5;
	}

}
