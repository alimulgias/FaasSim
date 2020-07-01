public class Function implements Comparable<Function> {

	String name;
	int age;


	double idlePeriodStartTime;

	private double totalInMemoryTime;

	double lastMemoryEntryTime;

	double memoryRequirement;
	double idleMemoryRequirement;

	int callCount;
	int hitCount;
	int requestDropCount;

	int totalColdStarts;
	int totalInitColdStarts;

	double serviceTime, coldStartTime, idleTime;

	public Function(int id) {

		name = "f" + id;
		age = 0;
		requestDropCount = 0;

		callCount = 0;
		hitCount = 0;

		totalColdStarts = 0;
		totalInitColdStarts = 0;

		totalInMemoryTime = 0;
		lastMemoryEntryTime = -1;

		idlePeriodStartTime = -1;

	}

	public double getTotalInMemoryTime(double currentTime) {
		//-1 means either deallocated or never allocated. the equation holds for both.
		if (lastMemoryEntryTime != -1)
			totalInMemoryTime = totalInMemoryTime + (currentTime - lastMemoryEntryTime);

		return totalInMemoryTime;
	}

	public void updateTotalInMemoryTime(double currentTime) {

		totalInMemoryTime += (currentTime - lastMemoryEntryTime);
	}

	public double getMemoryRequirement() {
		return memoryRequirement;
	}

	public void setMemoryRequirement(double memoryRequirement) {
		this.memoryRequirement = memoryRequirement;
	}

	public int getId() {

		return Integer.parseInt(this.name.substring(1));
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}


	@Override
	public boolean equals(Object obj) {

		if (obj == this) {
			return true;
		}

		if (!(obj instanceof Function)) {
			return false;
		}

		Function f = (Function) obj;

		return f.name.equals(name);
	}

	@Override
	public int compareTo(Function f) {

		return this.age - f.age;
	}
}
