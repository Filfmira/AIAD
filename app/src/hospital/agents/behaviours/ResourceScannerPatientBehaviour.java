package hospital.agents.behaviours;

import hospital.agents.PatientAgent;
import jade.core.behaviours.TickerBehaviour;

public class ResourceScannerPatientBehaviour extends TickerBehaviour {

	private PatientAgent agent;

	public ResourceScannerPatientBehaviour(PatientAgent patientAgent) {
		super(patientAgent, 30000); //30 seconds
		this.agent = patientAgent;
	}

	@Override
	protected void onTick() {
		//look for new resources (equipments)	
	}

}
