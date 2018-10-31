/**************************************
/
/      filename:  PCB.java
/
/   description:  Process Control Block
/                 setup for each job,
/                 contains all job data
/
/        author:  Berkshire, Tyler
/      login id:  FA_18_CPS356_32
/
/         class:  CPS 356
/    instructor:  Perugini
/    assignment:  Midterm Project
/
/      assigned:  September 27, 2018
/           due:  October 25, 2018
/
/**************************************/

public class PCB {
	private String type; // Event type
	private int time; // Arrival time
	private int jobNumber;
	private int memory; // Memory required
	private int totalRunTime;
	private int burstTime; // Time left job needs on CPU to complete
	private int startTime; // Time it first got on CPU
	private int comTime; // Time job completed
	private int quantum; // Current quantum value
	private int IOTotalTime; // Original time needed for IO
	private int IOStartTime;
	private int IOCompTime;
	private boolean hasStarted; // If job has been on CPU before
	private boolean isSecondLevelJob;
	private boolean hadIO; // If job came from IO
	
	public PCB(String job) {
		String[] nextEvent = job.split("\\s+"); // Split on spaces and tabs
		switch (nextEvent[0]) {
		// Add job
		case "A" : 
			setType("A");
			time = Integer.parseInt(nextEvent[1]);
			jobNumber = Integer.parseInt(nextEvent[2]);
			memory = Integer.parseInt(nextEvent[3]);
			totalRunTime = Integer.parseInt(nextEvent[4]);
			startTime = -1;
			burstTime = totalRunTime;
			hasStarted = false;
			isSecondLevelJob = false;
			hadIO = false;
			break;
		// I/O request
		case "I" :
			setType("I");
			time = Integer.parseInt(nextEvent[1]);
			IOTotalTime = Integer.parseInt(nextEvent[2]);
			break;
		// Display
		case "D" :
			setType("D");
			time = Integer.parseInt(nextEvent[1]);
			break;
		}
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}
	
	public int getJobNumber() {
		return jobNumber;
	}

	public void setJobNumber(int jobNumber) {
		this.jobNumber = jobNumber;
	}
	
	public int getMemory() {
		return memory;
	}

	public void setMemory(int memory) {
		this.memory = memory;
	}
	
	public int getTotalRunTime() {
		return totalRunTime;
	}

	public void setTotalRunTime(int totalRunTime) {
		this.totalRunTime= totalRunTime;
	}
	
	public int getBurstTime() {
		return burstTime;
	}

	public void setBurstTime(int burstTime) {
		this.burstTime = burstTime;
	}
	
	public int getStartTime() {
		return startTime;
	}
	
	public void setStartTime(int startTime) {
		this.startTime = startTime;
	}
	
	public int getComTime() {
		return comTime;
	}
	
	public void setComTime(int comTime) {
		this.comTime = comTime;
	}
	
	public int getQuantum() {
		return quantum;
	}
	
	public void setQuantum(int quantum) {
		this.quantum = quantum;
	}

	public int getIOTotalTime() {
		return IOTotalTime;
	}
	
	public void setIOTotalTime(int IOTotalTime) {
		this.IOTotalTime = IOTotalTime;
	}

	public int getIOStartTime() {
		return IOStartTime;
	}

	public void setIOStartTime(int IOStartTime) {
		this.IOStartTime = IOStartTime;
	}

	public int getIOCompTime() {
		return IOCompTime;
	}

	public void setIOCompTime(int IOCompTime) {
		this.IOCompTime = IOCompTime;
	}

	public boolean getHasStarted() {
		return hasStarted;
	}
	
	public void setHasStarted(boolean hasStarted) {
		this.hasStarted = hasStarted;
	}
	
	public boolean getIsSecondLevelJob() {
		return isSecondLevelJob;
	}
	
	public void setIsSecondLevelJob(boolean isSecondLevelJob) {
		this.isSecondLevelJob = isSecondLevelJob;
	}

	public boolean getHadIO() {
		return hadIO;
	}

	public void setHadIO(boolean hadIO) {
		this.hadIO = hadIO;
	}
}
