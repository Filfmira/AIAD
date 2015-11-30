package hospital;

public class Treatment {
	private String name;
	private boolean complete;
	private String result;
	private int time; // usual time the treatment takes, in seconds.
	
	public Treatment(String n, int time){
		this.name = n;
		this.complete = false;
		this.setResult(null);
		this.time = time;
	}

	public int getTime() {
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
