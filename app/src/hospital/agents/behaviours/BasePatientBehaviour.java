package hospital.agents.behaviours;

import hospital.Treatment;
import hospital.agents.PatientAgent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BasePatientBehaviour extends Behaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private MessageTemplate mt; // The template to receive CFP
	private int state = 0;

	public BasePatientBehaviour(PatientAgent a) {
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
			Treatment treatment = ((PatientAgent) myAgent).getNextTreatment();
			if (treatment == null)
				return;
			String nextTreatment = treatment.getName();
			
			// check if next treatment is the same as the received CFP 
			if(nextTreatment.equals(msg.getContent())){
				// send PROPOSAL
				ACLMessage cfp = new ACLMessage(ACLMessage.PROPOSE);
				cfp.setReplyWith(msg.getReplyWith()); // Unique value
				cfp.addReceiver(msg.getSender());
				cfp.setContent(Integer.toString(((PatientAgent) myAgent).getPriority()));
				myAgent.send(cfp);
				this.state++;
				mt = MessageTemplate.MatchReplyWith(msg.getReplyWith());
				System.out.println(" ## sent propose to " + msg.getSender().getLocalName() + " reply with: #"+msg.getReplyWith()+"#");
			}
			else{
				// send REFFUSE
				ACLMessage cfp = new ACLMessage(ACLMessage.REFUSE);
				cfp.setReplyWith(msg.getReplyWith()); // Unique value
				cfp.addReceiver(msg.getSender());
				myAgent.send(cfp);
				this.state = 0;
				System.out.println(" ## sent refuse to " + msg.getSender().getLocalName());
			}
		}
		else {
			block();
		}		
		
	}
	
	private void initialize() {
		mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);		
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
