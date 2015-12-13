package hospital;

import java.util.Arrays;
import java.util.Hashtable;

public class Patologies {
	private static Hashtable<String,Treatments> patologyTreatments = new Hashtable<String,Treatments>()
		{
			private static final long serialVersionUID = -815164009799967292L;

			{ 
				put("patology-1", new Treatments(Arrays.asList("treatment-1", "treatment-2",  "treatment-4"), true)); 
				put("patology-2", new Treatments(Arrays.asList("treatment-2", "treatment-3", "treatment-4"), false));
				put("patology-3", new Treatments(Arrays.asList("treatment-3", "treatment-4", "treatment-5"), false));
				put("patology-4", new Treatments(Arrays.asList("treatment-3", "treatment-5", "treatment-4"), false));
			}
		};
		

	/**
	 * 
	 * @param patology - Patology 
	 * @return Treatment for specified patology. Null if no treatment available. 
	 */
	public static Treatments get(String patology){
		return Patologies.patologyTreatments.get(patology);
	}
}
