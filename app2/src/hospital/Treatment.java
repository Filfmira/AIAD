package hospital;

public class Treatment {
	private String name;
	private boolean complete;
	private String result;
	private long time; // usual time the treatment takes, in seconds.
	private int numberTreatmentsPerformed = 1; // number of treatments like this performed to date
	
	public Treatment(String n, long time){
		this.name = n;
		this.complete = false;
		this.setResult(null);
		this.time = time;
	}
	
	public void updateDuration(long newDuration){
		long oldTime = this.time;
		long sumTime = (this.time* (long)this.numberTreatmentsPerformed)+newDuration;
		this.numberTreatmentsPerformed++;
		this.time = sumTime / (long) this.numberTreatmentsPerformed;
		System.out.println("Updating time for "+this.name+" -> old: "+oldTime+" # new: "+this.time);
	}

	public long getTime() {
		return time;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public boolean isComplete() {
		return complete;
	}

	public void complete() {
		this.complete = true;
	}

	public String getName() {
		return name;
	}
}
