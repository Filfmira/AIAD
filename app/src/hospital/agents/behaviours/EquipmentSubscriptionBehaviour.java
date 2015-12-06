package hospital.agents.behaviours;

import java.util.Hashtable;
import java.util.List;

import hospital.Treatment;
import hospital.agents.PatientAgent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class EquipmentSubscriptionBehaviour extends Behaviour{

	private static final long serialVersionUID = -4128873967125249267L;
	private int repliesCnt = 0; // The counter of replies from equipment agents
	private int equipmentAgents = 0; // The number of replies expected
	private MessageTemplate mt; // The template to receive replies
	private boolean allTreatmentsNotified = false;
	private String replyWith;
	private Hashtable<String, Boolean> foundTreatments = new Hashtable<String, Boolean>();
	
	public EquipmentSubscriptionBehaviour(PatientAgent patientAgent) {
		myAgent = patientAgent;
		
		replyWith = "cfp"+System.currentTimeMillis(); // Unique value
		mt = MessageTemplate.and(MessageTemplate.MatchConversationId("subscribe-equipment"),
				MessageTemplate.MatchInReplyTo(replyWith));
	}

	@Override
	public void action() {
		if(foundTreatments.size() != ((PatientAgent) myAgent).getTreatment().getTreatments().size()){
			
			//send SUBSCRIBE messages to all equipments "needed"
			List<Treatment> ts = ((PatientAgent) myAgent).getTreatment().getTreatments();
			
			int i = 0;
			for(; i < ts.size(); i++){
				Treatment t = ts.get(i);
				if(foundTreatments.get(t.getName()) == null){
					String equipmentName = t.getName()+"-equipment";
					//System.out.println("Looking for: "+equipmentName);
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType(equipmentName);
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent, template); 
						//System.out.println("Found the following " + result.length + " " + equipmentName + " agents:");
						
						ACLMessage cfp = new ACLMessage(ACLMessage.SUBSCRIBE);
						if(result.length > 0){
							foundTreatments.put(t.getName(), true);
						}
						
						for (int j = 0; j < result.length; j++) {
							cfp.addReceiver(result[j].getName());
							equipmentAgents++;
							System.out.println(result[j].getName());
						}
						
						cfp.setContent("");
						cfp.setConversationId("subscribe-equipment");
						cfp.setReplyWith(replyWith); // Unique value
						myAgent.send(cfp);
					}
					catch (FIPAException fe) {
						fe.printStackTrace();
					}
				}
			}		
		}
		
		
		// Receive all AGREE from equipment agents
		ACLMessage reply = myAgent.receive(mt);
		if (reply != null) {
			// Reply received
			if (reply.getPerformative() == ACLMessage.AGREE) {
				//System.out.println(myAgent.getName() + " got subscribed to " + reply.getSender().getName());
			}
			else {
				//System.out.println(myAgent.getName() + " didn't subscribe to " + reply.getSender().getName());
			}
			repliesCnt++;
		}
		else {
			block();
		}
		
	}
	
	@Override
	public boolean done() {
		//System.out.println("Equipment Subscription ended in agent "+myAgent.getName());
		return allTreatmentsNotified && repliesCnt >= equipmentAgents;
	}

}
