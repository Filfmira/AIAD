package hospital.agents.behaviours;

import hospital.agents.PatientAgent;
import jade.core.behaviours.Behaviour;

public class BasePatientBehaviour extends Behaviour {

	private PatientAgent agent;

	public BasePatientBehaviour(PatientAgent a) {
		// TODO Auto-generated constructor stub
		this.agent = a;
	}

	@Override
	public void action() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
