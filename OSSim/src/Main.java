/**************************************
/
/      filename:  Main.java
/
/   description:  Initial job functions
/                 and time increments
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
import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		// Create the list of events from the input file
		ArrayList<String> eventList = new ArrayList<String>();
		Scanner s = new Scanner(System.in);
		while (s.hasNextLine()) {
			eventList.add(s.nextLine().toUpperCase());
		}
		s.close();
		
		// Create a PCB for every event
		ArrayList<PCB> jobList = new ArrayList<PCB>();
		while (!eventList.isEmpty()) {
			PCB newJob = new PCB(eventList.remove(0));
			jobList.add(newJob);
		}
		if (jobList.get(jobList.size() - 1).getType() == null) {
			jobList.remove(jobList.size() - 1);
		}
		
		// Set up initial scheduler
		Scheduler scheduler = new Scheduler();
		int currentTime = -1;
		boolean finished = false;

		// Run OS until all jobs completed
		while (!finished) {
			currentTime++;
			// Check for external jobs
			if (currentTime >= jobList.get(0).getTime()) {
				scheduler.jobSchedule(currentTime, jobList.remove(0));
			// Process continues
			} else {
				scheduler.processSchedule(currentTime);
			}
			finished = jobList.isEmpty();
		}
		
		scheduler.showDisplayFinal(++currentTime);
	}
}
