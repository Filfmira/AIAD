package hospital.behaviours;

import java.util.ArrayList;
import java.util.List;

import hospital.agents.Equipment;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BaseEquipmentBehaviour extends TickerBehaviour{

	private int state = 0;	
	private String replyWith = null;
	
	private AID bestProposalAID = null;
	private int bestProposal = 0;
	private int cfpsSent = 0;
	private int cfpResponsesReceived = 0;
	private List<AID> proposalsReceivedAIDs = new ArrayList<AID>();
	private boolean acceptedByPatient = false;
	
	public BaseEquipmentBehaviour(Agent a) {
		super(a, 10000);
	}

	@Override
	protected void onTick() {
		//if already in treatment, agent doesn't look for patients.
		if(((Equipment) myAgent).getCurrentPatient() != null)
			return;
		
		this.state = 0;
		//get patients that want this treatment
		DFAgentDescription[] patients = ((Equipment) myAgent).getSubscribedPatients();
		System.out.println(myAgent.getLocalName() + " -> number of patients before while = " + patients.length);
		long start = System.currentTimeMillis();
		while(state != -1 && System.currentTimeMillis() - start < 5000){
			//System.out.println(" ## state in "+myAgent.getLocalName() + ": "+this.state);
			switch(this.state){
				
			case 0: 
				if(patients.length > 0){
					initiateContractNetProtocol(patients);
					this.state++;
				}
				else this.state = -1;
				break;
			
			case 1:
				block();
				receiveProposal();
				//System.out.println("received: "+this.cfpResponsesReceived + " -> sent: "+this.cfpsSent);
				if(this.cfpResponsesReceived == this.cfpsSent)
					this.state++;
				break;
				
			case 2:
				if (sendProposalResponses())
					this.state++;
				else this.state = -1;
				break;
			
			case 3:
				block();
				if (receivePatientConfirmation()){
					if (this.acceptedByPatient)
						this.state++;
					else this.state = -1;
				}
				break;
				
			case 4:
				//operator will have to press the button on the GUI to end treatment
				((Equipment) myAgent).startBusyGui();
				myAgent.addBehaviour(new WaitForPatientStartBehaviour((Equipment) myAgent, bestProposalAID));
				state = -1;
				break;
			}
		}
		
	}

	private void initiateContractNetProtocol(DFAgentDescription[] patients) {
		System.out.println("Initiate contract net in " + myAgent.getLocalName());
		System.out.println("number of patients = " + patients.length + " -> they are: ");
		
		this.bestProposal = 0;
		this.bestProposalAID = null;
		this.cfpsSent = 0;
		this.cfpResponsesReceived = 0;
		this.proposalsReceivedAIDs = new ArrayList<AID>();
		this.acceptedByPatient = false;
		
		ACLMessage cfp = new ACLMessage(ACLMessage.CFP);		
		this.replyWith = "cfp"+System.currentTimeMillis(); // Unique value
		cfp.setReplyWith(replyWith); // Unique value
		for(int i = 0; i < patients.length; i++){
			cfp.addReceiver(patients[i].getName());
			System.out.print(patients[i].getName().getLocalName()+",");
		}
		System.out.println("");
		this.cfpsSent  = patients.length;
		cfp.setContent(((Equipment) myAgent).getTreatment().getName());
		myAgent.send(cfp);
	}

	private void receiveProposal() {
		MessageTemplate mt = MessageTemplate.MatchReplyWith(this.replyWith);
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			
			if (msg.getPerformative() == ACLMessage.PROPOSE){
				System.out.println(myAgent.getLocalName() + " : received propose from " + msg.getSender().getLocalName());
				int proposal = Integer.parseInt(msg.getContent());
				if(proposal > this.bestProposal){
					this.bestProposal = proposal;
					this.bestProposalAID = msg.getSender();
				}
				this.proposalsReceivedAIDs.add(msg.getSender());
			}
			//else performative -> ACLMessage.REFUSE
			else System.out.println(myAgent.getLocalName() + " : received refuse from " + msg.getSender().getLocalName());
			this.cfpResponsesReceived++;
		}/*
		else {
			block();
		}	*/	
		
	}
	
	/**
	 * 
	 * @return true if sent response to anyone, false otherwise
	 */
	private boolean sendProposalResponses() {
		if(this.proposalsReceivedAIDs.size() == 0)
			return false;
		
		// REJECT
		ACLMessage reject = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
		if(this.proposalsReceivedAIDs.size() > 1){ //if only one proposal, there are no rejects
			for (int j = 0; j < this.proposalsReceivedAIDs.size(); j++) {
				if(!this.proposalsReceivedAIDs.get(j).equals(this.bestProposalAID))
					reject.addReceiver(this.proposalsReceivedAIDs.get(j));
			}
			reject.setReplyWith(replyWith); // Unique value
			myAgent.send(reject);
		}
		
		// ACCEPT 
		ACLMessage accept = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
		accept.addReceiver(this.bestProposalAID);
		accept.setReplyWith(replyWith); // Unique value
		myAgent.send(accept);	
		
		return true;
	}
	
	private boolean receivePatientConfirmation() {
		MessageTemplate mt = MessageTemplate.MatchReplyWith(this.replyWith);
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			if(msg.getSender().equals(this.bestProposalAID) && msg.getPerformative() == ACLMessage.INFORM){
				this.acceptedByPatient  = true;
				((Equipment) myAgent).setCurrentPatient(msg.getSender());
			}
			return true;
		}
		else return false;
		
	}
}
