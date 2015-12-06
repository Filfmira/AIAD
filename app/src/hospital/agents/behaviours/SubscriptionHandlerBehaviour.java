package hospital.agents.behaviours;

import hospital.agents.EquipmentAgent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SubscriptionHandlerBehaviour extends Behaviour {

	private static final long serialVersionUID = 1L;

	public SubscriptionHandlerBehaviour(EquipmentAgent equipmentAgent) {
		myAgent = equipmentAgent;
	}

	@Override
	public void action() {
		MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
		
		ACLMessage msg = myAgent.receive(mt);
		if (msg != null) {
			System.out.println(" recebi subscription "+ msg.getReplyWith());
			ACLMessage reply = msg.createReply();
			reply.setPerformative(ACLMessage.AGREE);
			myAgent.send(reply);
			
			((EquipmentAgent) myAgent).addSubscribedAgent(msg.getSender());
			//System.out.println("Patient "+msg.getSender().getName() + " is subscribed");
		}
		else {
			block();
		}
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
