/**************************************
/
/      filename:  Scheduler.java
/
/   description:  Contains logic for
/                 moving jobs between
/                 queues and CPU
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeSet;

public class Scheduler {
	public static final int MAX_MEMORY = 512;
	public static final int QUANTUM_PRIMARY = 100;
	public static final int QUANTUM_SECONDARY = 300;
	
	private int currentMemory = MAX_MEMORY;
	
	private PCB CPU;
	private Queue<PCB> jobQueue = new LinkedList<PCB>();
	private Queue<PCB> procQueueL1 = new LinkedList<PCB>();
	private Queue<PCB> procQueueL2 = new LinkedList<PCB>();
	private TreeSet<PCB> IOQueue = new TreeSet<PCB>(new PCBComp());
	private ArrayList<PCB> finishedList = new ArrayList<PCB>();
	private ArrayList<Integer> TATimes = new ArrayList<Integer>();
	private ArrayList<Integer> WaitTimes = new ArrayList<Integer>();
	
	// Compare based on completion time
	class PCBComp implements Comparator<PCB> {
		public int compare(PCB one, PCB two) {
			Integer iOne = new Integer(one.getIOCompTime());
			Integer iTwo = new Integer(two.getIOCompTime());
			return iOne.compareTo(iTwo);
		}
	}

	// Trigger job events
	public void jobSchedule(int currentTime, PCB job) {
		switch(job.getType()) {
		// Job arrival
		case "A":
			eventA(currentTime, job);
			break;
		// I/O request
		case "I":
			eventI(currentTime, job);
			break;
		// Display
		case "D":
			eventD(currentTime, job);
			break;
		// Quantum expired
		case "E":
			eventE(currentTime, job);
			break;
		// Job terminated
		case "T":
			eventT(currentTime, job);
			break;
		// I/O Complete
		case "C":
			eventC(currentTime, job);
			break;
		// Just running on CPU
		case "R":
			processSchedule(currentTime);
			break;
		}
	}
	
	// Only run the CPU once per time slot
	public void processSchedule(int currentTime) {
		// Add jobs to level 1 queue if there's space
		while (!jobQueue.isEmpty() && jobQueue.peek().getMemory() <= currentMemory) {
			currentMemory -= jobQueue.peek().getMemory();
			PCB job = jobQueue.remove();
			// WT = Time Exited JQueue - Arrival
			WaitTimes.add(currentTime - job.getTime());
			procQueueL1.add(job);
		}
		
		// If CPU idle, add process from ready queue
		if (CPU == null) {
			// Items in the level 1 queue have priority
			if (!procQueueL1.isEmpty()) {
				CPU = procQueueL1.remove();
				CPU.setType("R");
				CPU.setQuantum(QUANTUM_PRIMARY);
				CPU.setHadIO(false);
				if (CPU.getStartTime() == -1) {
					CPU.setStartTime(currentTime);
				}
				runCPU();
			} else if (!procQueueL2.isEmpty()){
				CPU = procQueueL2.remove();
				CPU.setType("R");
				CPU.setQuantum(QUANTUM_SECONDARY);
				CPU.setHadIO(false);
				runCPU();
			} else if (!IOQueue.isEmpty()) {
				if (IOQueue.first().getIOCompTime() <= currentTime) {
					PCB job = IOQueue.pollFirst();
					job.setType("C");
					jobSchedule(currentTime, job);
				}
			}
			return;
		}
		
		// Boot off process if it came from the second level queue
		// and the first level queue isn't empty
		if (CPU.getIsSecondLevelJob() && !procQueueL1.isEmpty()) {
			procQueueL2.add(CPU);
			CPU = procQueueL1.remove();
			CPU.setType("R");
			CPU.setQuantum(QUANTUM_PRIMARY);
			if (CPU.getStartTime() == -1) {
				CPU.setStartTime(currentTime);
			}
			runCPU();
			return;
		}
		
		// Check if any IO is done
		if (!IOQueue.isEmpty()) {
			if (IOQueue.first().getIOCompTime() <= currentTime) {
				PCB job = IOQueue.pollFirst();
				job.setType("C");
				jobSchedule(currentTime, job);
				return;
			}
		}
		
		// Check if job on CPU is done
		if (CPU.getBurstTime() <= 0) {
			CPU.setType("T");
			CPU.setComTime(currentTime);
			jobSchedule(currentTime, CPU);
			return;
		}
		
		// Check if quantum is expired
		if (CPU.getQuantum() <= 0) {
			CPU.setType("E");
			jobSchedule(currentTime, CPU);
			return;
		}
		
		runCPU();
	}
	
	private void runCPU() {
		// Decrement total burst time left
		CPU.setBurstTime(CPU.getBurstTime() - 1);
		
		// Decrement current quantum
		CPU.setQuantum(CPU.getQuantum() - 1);
	}
	
	private void eventA(int currentTime, PCB job) {
		// Only add job if it can fit in main memory
		if (job.getMemory() > 512) {
			System.out.println("Event: A   Time: " + currentTime);
			System.out.println("This job exceeds the system's main memory capacity.");
			processSchedule(currentTime);
		} else {
			jobQueue.add(job);
			processSchedule(currentTime);
			System.out.println("Event: A   Time: " + currentTime);
		}
	}
	
	private void eventI(int currentTime, PCB job) {
		// Make sure event collisions favor internal processes
		if (!IOQueue.isEmpty()) {
			if (IOQueue.first().getIOCompTime() <= currentTime) {
				PCB job2 = IOQueue.pollFirst();
				job2.setType("C");
				System.out.println("Event: C   Time: " + currentTime);
				procQueueL1.add(job2);
			}
		}
		CPU.setIOStartTime(job.getTime());
		CPU.setIOTotalTime(job.getIOTotalTime());
		CPU.setIOCompTime(CPU.getIOStartTime() + job.getIOTotalTime());
		CPU.setHadIO(true);
		IOQueue.add(CPU);
		CPU = null;
		processSchedule(currentTime);
		System.out.println("Event: I   Time: " + currentTime);
	}
	
	private void eventD(int currentTime, PCB job) {		
		processSchedule(currentTime);
		System.out.println("Event: D   Time: " + currentTime);
		showDisplay(currentTime);
	}
	
	private void eventE(int currentTime, PCB job) {
		System.out.println("Event: E   Time: " + currentTime);
		CPU.setIsSecondLevelJob(true);
		procQueueL2.add(CPU);
		CPU = null;
		processSchedule(currentTime);
	}
	
	private void eventT(int currentTime, PCB job) {
		System.out.println("Event: T   Time: " + currentTime);
		currentMemory += job.getMemory();
		// TA = Completion - Arrival
		int TAT = job.getComTime() - job.getTime();
		TATimes.add(TAT);
		finishedList.add(job);
		CPU = null;
		processSchedule(currentTime);
	}
	
	private void eventC(int currentTime, PCB job) {
		System.out.println("Event: C   Time: " + currentTime);
		procQueueL1.add(job);
		processSchedule(currentTime);
	}
	
	
	private void showDisplay(int currentTime) {
		System.out.println();
		System.out.println("************************************************************");
		System.out.println();
		System.out.println("The status of the simulator at time " + currentTime + ".");
		System.out.println();
		System.out.println("The contents of the JOB SCHEDULING QUEUE");
		System.out.println("----------------------------------------");
		System.out.println();
		if (jobQueue.isEmpty()) {
			System.out.println("The Job Scheduling Queue is empty.");
		} else {
			System.out.println("Job #  Arr. Time  Mem. Req.  Run Time");
			System.out.println("-----  ---------  ---------  --------");
			System.out.println();
			for (PCB job : jobQueue) {
				System.out.printf("%5d  %9d  %9d  %8d%n", 
						job.getJobNumber(), job.getTime(), job.getMemory(), job.getTotalRunTime());
			}
		}
		System.out.println();
		System.out.println();
		System.out.println("The contents of the FIRST LEVEL READY QUEUE");
		System.out.println("-------------------------------------------");
		System.out.println();
		if (procQueueL1.isEmpty()) {
			System.out.println("The First Level Ready Queue is empty.");
		} else {
			System.out.println("Job #  Arr. Time  Mem. Req.  Run Time");
			System.out.println("-----  ---------  ---------  --------");
			System.out.println();
			for (PCB job : procQueueL1) {
				System.out.printf("%5d  %9d  %9d  %8d%n", 
						job.getJobNumber(), job.getTime(), job.getMemory(), job.getTotalRunTime());
			}
		}
		System.out.println();
		System.out.println();
		System.out.println("The contents of the SECOND LEVEL READY QUEUE");
		System.out.println("--------------------------------------------");
		System.out.println();
		if (procQueueL2.isEmpty()) {
			System.out.println("The Second Level Ready Queue is empty.");
		} else {
			System.out.println("Job #  Arr. Time  Mem. Req.  Run Time");
			System.out.println("-----  ---------  ---------  --------");
			System.out.println();
			for (PCB job : procQueueL2) {
				System.out.printf("%5d  %9d  %9d  %8d%n", 
						job.getJobNumber(), job.getTime(), job.getMemory(), job.getTotalRunTime());
			}
		}
		System.out.println();
		System.out.println();
		System.out.println("The contents of the I/O WAIT QUEUE");
		System.out.println("----------------------------------");
		System.out.println();
		if (IOQueue.isEmpty()) {
			System.out.println("The I/O Wait Queue is empty.");
		} else {
			System.out.println("Job #  Arr. Time  Mem. Req.  Run Time  IO Start Time  IO Burst  Comp. Time");
			System.out.println("-----  ---------  ---------  --------  -------------  --------  ----------");
			System.out.println();
			for (PCB job : IOQueue) {
				System.out.printf("%5d  %9d  %9d  %8d  %13d  %8d  %10d%n", 
						job.getJobNumber(), job.getTime(), job.getMemory(), job.getTotalRunTime(), 
						job.getIOStartTime(), job.getIOTotalTime(), job.getIOCompTime());
			}
		}
		System.out.println();
		System.out.println();
		System.out.println("The CPU  Start Time  CPU burst time left");
		System.out.println("-------  ----------  -------------------");
		System.out.println();
		if (CPU == null) {
			System.out.println("The CPU is idle.");
		} else {
			System.out.printf("%7d  %10d  %19d%n", 
					CPU.getJobNumber(), CPU.getStartTime(), CPU.getBurstTime() + 1);
		}
		System.out.println();
		System.out.println();
		System.out.println("The contents of the FINISHED LIST");
		System.out.println("---------------------------------");
		System.out.println();
		if (finishedList.isEmpty()) {
			System.out.println("The Finished List is empty.");
		} else {
			System.out.println("Job #  Arr. Time  Mem. Req.  Run Time  Start Time  Com. Time");
			System.out.println("-----  ---------  ---------  --------  ----------  ---------");
			System.out.println();
			for (PCB job : finishedList) {
				System.out.printf("%5d  %9d  %9d  %8d  %10d  %9d%n", 
						job.getJobNumber(), job.getTime(), job.getMemory(), job.getTotalRunTime(), 
						job.getStartTime(), job.getComTime());
			}
		}
		System.out.println();
		System.out.println();
		System.out.println("There are " + currentMemory + " blocks of main memory available in the system.");
		System.out.println();
	}
	
	
	public void showDisplayFinal(int currentTime) {
		// Finish all internal processes
		while(!jobQueue.isEmpty() || !procQueueL1.isEmpty() || !procQueueL2.isEmpty() || CPU != null) {
			processSchedule(currentTime);
			currentTime++;
		}
		System.out.println();
		System.out.println("The contents of the FINAL FINISHED LIST");
		System.out.println("---------------------------------------");
		System.out.println();
		if (finishedList.isEmpty()) {
			System.out.println("The Final Finished List is empty.");
		} else {
			System.out.println("Job #  Arr. Time  Mem. Req.  Run Time  Start Time  Com. Time");
			System.out.println("-----  ---------  ---------  --------  ----------  ---------");
			System.out.println();
			for (PCB job : finishedList) {
				System.out.printf("%5d  %9d  %9d  %8d  %10d  %9d%n", 
						job.getJobNumber(), job.getTime(), job.getMemory(), job.getTotalRunTime(), 
						job.getStartTime(), job.getComTime());
			}
		}
		System.out.println();
		System.out.println();
		double totalTATimes = 0.0;
		for (int time : TATimes) {
			totalTATimes += time;
		}
		double averageTATime = totalTATimes / TATimes.size();
		System.out.printf("The Average Turnaround Time for the simulation was %.3f units.%n", averageTATime);
		System.out.println();
		double totalWaitTimes = 0.0;
		for (int time : WaitTimes) {
			totalWaitTimes += time;
		}
		double averageWaitTime = totalWaitTimes / WaitTimes.size();
		System.out.printf("The Average Job Scheduling Wait Time for the simulation was %.3f units.%n", averageWaitTime);
		System.out.println();
		System.out.println("There are " + currentMemory + " blocks of main memory available in the system.");
		System.out.println();
	}
}
