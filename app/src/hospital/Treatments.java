package hospital;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class Treatments {
	private List<Treatment> treatmentsList;
	private Boolean ordered;
	
	public Treatments(List<String> t, Boolean o){
		this.treatmentsList = new ArrayList<Treatment>();
		for(int i = 0; i < t.size(); i++){
			Treatment t_temp = Treatments.treatments.get(t.get(i));
			if(t_temp == null)
				System.out.println("Treatment "+t.get(i)+" not found... Ignoring.");
			else treatmentsList.add(t_temp);
		}
		this.ordered = false;
	}

	public Boolean getOrdered() {
		return ordered;
	}

	public List<Treatment> getTreatments() {
		return treatmentsList;
	}
	
	private static Hashtable<String,Treatment> treatments = new Hashtable<String,Treatment>()
	{
		{ 
			put("treatment-1", new Treatment("treatment-1", 10));
			put("treatment-2", new Treatment("treatment-2", 15));
			put("treatment-3", new Treatment("treatment-3", 20));
			put("treatment-4", new Treatment("treatment-4", 25));
		}
	};
	
	public static Treatment get(String treatment){
		return Treatments.treatments.get(treatment);
	}
}
