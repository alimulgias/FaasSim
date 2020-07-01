
public class Job implements Comparable<Job> {

	int fId;

	double startTime;
	double endTime;
	
	double delayEntryTime;
	double delayExitTime;
	
	boolean isCold;
	
	double residualTime;
	double serviceTime;

	public Job(int fId, double startTime) {

		this.fId = fId;
		this.startTime = startTime;
		residualTime=0;
		
		isCold=false;
		
	}

	public double getResponseTime() {

		return endTime - startTime;
	}
	
	public double getWaitingTime() {

		return delayExitTime - delayEntryTime;
	}

	@Override
	public int compareTo(Job j) {

		return Double.compare(this.residualTime,j.residualTime);
	}

}
