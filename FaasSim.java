
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class FaasSim {

	int numberOfFunctions;
	int memorySpace;

	double arrivalRate;

	double sampleCountStartTime;

	int numberOfSamples, numberOfSamplesToBeDiscarded, age, hits;

	int numberOfCPUs;

	Map<Double, Double> memoryReservationTimeSeries;
	Map<Double, Double> cpuUsageTimeSeries;
	Map<Double, Double> memoryUsageTimeSeries;

	double totalMemoryReservation;
	double totalMemoryUsage;
	double maxMemoryUsage;

	String expectedHitrate;
	String experimentFolder;

	double pollingInterval;

	int totalRequestDrop, totalColdStarts, totalNotInitColdStarts;

	Map<String, Double> burstParams;

	List<Job> jobList;
	List<Integer> coldFunctionIds;

	List<Job> delays;

	List<Function> functions;
	List<Function> inMemoryFunctions;

	int[] functionCallCount;
	double[] functionTotalResponseTimes;

	double lambdaTot;

	int iteration;

	double[] lambda;
	double[] ps;
	double time = 0.0;

	double zipfShape;

	Random randServiceTime, randEvents, randFunctions, randColdStartTime;

	String logFile;
	String memoryReservationFile;
	String memoryUsageFile;
	String cpuFile;
	String parameterFolder;
	String idleMemoryFolder;
	String idleTimeFolder;

	public FaasSim(int numberOfCPUs, int numberOfSamples, int numberOfFunctions, double spaceToFunctionRatio,
			double zipfShape, double arrivalRate, int iteration, String expectedHitrate, String experimentFolder,
			String parameterRootDir) {

		this.numberOfCPUs = numberOfCPUs;

		this.numberOfSamples = numberOfSamples;

		numberOfSamplesToBeDiscarded = numberOfSamples / 3;

		this.iteration = iteration;

		this.expectedHitrate = expectedHitrate;
		this.experimentFolder = experimentFolder;

		this.arrivalRate = arrivalRate;

		this.numberOfFunctions = numberOfFunctions;
		this.memorySpace = (int) (numberOfFunctions * spaceToFunctionRatio);

		this.logFile = experimentFolder + "/logs/" + numberOfFunctions + "/" + arrivalRate + "/"
				+ (zipfShape == 1 ? "1" : zipfShape) + "/" + expectedHitrate + "/" + iteration + "/" + "logs.txt";
		this.memoryReservationFile = experimentFolder + "/mem/" + numberOfFunctions + "/" + arrivalRate + "/"
				+ (zipfShape == 1 ? "1" : zipfShape) + "/" + expectedHitrate + "/" + iteration + "/"
				+ "memory_reservation.txt";
		this.memoryUsageFile = experimentFolder + "/mem/" + numberOfFunctions + "/" + arrivalRate + "/"
				+ (zipfShape == 1 ? "1" : zipfShape) + "/" + expectedHitrate + "/" + iteration + "/"
				+ "memory_usage.txt";
		this.cpuFile = experimentFolder + "/cpu/" + numberOfFunctions + "/" + arrivalRate + "/"
				+ (zipfShape == 1 ? "1" : zipfShape) + "/" + expectedHitrate + "/" + iteration + "/"
				+ "utilisation.txt";
		this.parameterFolder = parameterRootDir + "/params/" + numberOfFunctions + "/" + arrivalRate + "/"
				+ (zipfShape == 1 ? "1" : zipfShape) + "/";
		this.idleMemoryFolder = experimentFolder + "/params/" + numberOfFunctions + "/" + arrivalRate + "/"
				+ (zipfShape == 1 ? "1" : zipfShape) + "/";
		this.idleTimeFolder = experimentFolder + "/params/" + numberOfFunctions + "/" + arrivalRate + "/"
				+ (zipfShape == 1 ? "1" : zipfShape) + "/" + "idleTimes/" + expectedHitrate + "/";

		age = 0;
		hits = 0;

		totalMemoryReservation = 0;
		memoryReservationTimeSeries = new TreeMap<Double, Double>();
		memoryUsageTimeSeries = new TreeMap<Double, Double>();

		totalMemoryUsage = 0;
		maxMemoryUsage = 0;

		cpuUsageTimeSeries = new TreeMap<Double, Double>();

		this.zipfShape = zipfShape;

		functionCallCount = new int[numberOfFunctions];
		functionTotalResponseTimes = new double[numberOfFunctions];

		for (int i = 0; i < functionCallCount.length; i++) {
			functionCallCount[i] = 0;
			functionTotalResponseTimes[i] = 0;
		}

		totalRequestDrop = 0;
		totalColdStarts = 0;
		totalNotInitColdStarts = 0;

		pollingInterval = 5.0;

		randServiceTime = new Random();
		randEvents = new Random();
		randFunctions = new Random();
		randColdStartTime = new Random();

		lambda = new double[numberOfFunctions];

		lambdaTot = 0.0;

		ps = new double[numberOfFunctions];

		functions = new ArrayList<>();

		coldFunctionIds = new ArrayList<>();

		delays = new ArrayList<>();

		for (int i = 0; i < numberOfFunctions; i++) {

			Function f = new Function(i);
			f.setMemoryRequirement(1);
			functions.add(f);
		}

		loadParameters();

		inMemoryFunctions = new ArrayList<>();

		for (int k = 0; k < numberOfFunctions; k++) {
			lambda[k] = 1.0 / Math.pow((k + 1), zipfShape);
			lambdaTot += lambda[k];
		}
		for (int k = 0; k < numberOfFunctions; k++) {
			ps[k] = lambda[k] / lambdaTot;
		}

		jobList = new ArrayList<>();

	}

	public void loadParameters() {

		try {
			String line;

			BufferedReader br = new BufferedReader(new FileReader(new File(parameterFolder + "service.dat")));
			int i = 0;
			while ((line = br.readLine()) != null) {
				double value = Double.parseDouble(line);
				functions.get(i).serviceTime = value;
				i++;
			}

			br.close();

			br = new BufferedReader(new FileReader(new File(parameterFolder + "coldStart.dat")));
			i = 0;
			while ((line = br.readLine()) != null) {
				double value = Double.parseDouble(line);
				functions.get(i).coldStartTime = value;
				i++;
			}

			br.close();

			br = new BufferedReader(new FileReader(new File(idleTimeFolder + "idleTime.dat")));
			i = 0;
			while ((line = br.readLine()) != null) {
				double value = Double.parseDouble(line);
				functions.get(i).idleTime = value;
				i++;
			}

			br.close();

			br = new BufferedReader(new FileReader(new File(parameterFolder + "memory.dat")));
			i = 0;
			while ((line = br.readLine()) != null) {
				double value = Double.parseDouble(line);
				functions.get(i).memoryRequirement = value;
				i++;
			}

			br.close();

			br = new BufferedReader(new FileReader(new File(idleMemoryFolder + "idleMemory.dat")));
			i = 0;
			while ((line = br.readLine()) != null) {
				double value = Double.parseDouble(line);
				functions.get(i).idleMemoryRequirement = value;
				i++;
			}

			br.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Function getFunction() {

		double r = randFunctions.nextDouble();

		double acc = 0.0;
		for (int i = 0; i < numberOfFunctions; i++) {
			acc += ps[i];
			if (r < acc) {
				return functions.get(i);
			}
		}
		return null;
	}

	public void writeLog(String str) {

		try {

			File targetFile = new File(logFile);
			File parent = targetFile.getParentFile();
			if (!parent.exists() && !parent.mkdirs()) {
				throw new IllegalStateException("Couldn't create dir: " + parent);
			}

			BufferedWriter bw = new BufferedWriter(new FileWriter(targetFile, true));
			bw.write(str + "\n");
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isIdle(Function f) {

		for (Job j : jobList) {

			if (j.fId == f.getId())
				return false;

		}

		return true;

	}

	public void processJob(Job j) {

		functionCallCount[j.fId]++;
		functionTotalResponseTimes[j.fId] += j.getResponseTime();

	}

	public void calculateIndividualFunctionResponseTimes() {

		String filename;

		try {

			filename = experimentFolder + "/response/" + numberOfFunctions + "/" + arrivalRate + "/"
					+ (zipfShape == 1 ? "1" : zipfShape) + "/" + expectedHitrate + "/" + iteration + "/"
					+ "RespTperFunction.csv";

			File targetFile = new File(filename);
			File parent = targetFile.getParentFile();
			if (!parent.exists() && !parent.mkdirs()) {
				throw new IllegalStateException("Couldn't create dir: " + parent);
			}

			BufferedWriter bw = new BufferedWriter(new FileWriter(targetFile, true));
			for (int i = 0; i < functionCallCount.length; i++) {
				bw.write(functionTotalResponseTimes[i] / functionCallCount[i] + "\n");
			}
			bw.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public double getNextArrival() {
		return Math.log(1 - randEvents.nextDouble()) / (-arrivalRate);
	}

	public double getNextServiceTime(int fId) {

		double lambda = 1.0 / functions.get(fId).serviceTime;

		return Math.log(1 - randServiceTime.nextDouble()) / (-lambda);
	}

	public double getNextColdStartTime(int fId) {

		double lambda = 1.0 / functions.get(fId).coldStartTime;

		return Math.log(1 - randColdStartTime.nextDouble()) / (-lambda);
	}

	public boolean isWarm(int fId) {

		return !coldFunctionIds.contains(fId);
	}

	public Job getJob(double currentTime, boolean sampleCountStarted) {

		Job job = null;

		Function f = getFunction();
		if (sampleCountStarted) {
			f.callCount++;
		}
		if (inMemoryFunctions.contains(f)) {
			if (isWarm(f.getId())) {

				if (sampleCountStarted) {
					hits++;
					f.hitCount++;
				}

				f.setAge(++age);

				job = new Job(f.getId(), currentTime);

				if (isIdle(f)) {

					totalMemoryUsage += (f.memoryRequirement - f.idleMemoryRequirement);

					if (sampleCountStarted) {

						memoryUsageTimeSeries.put(currentTime, totalMemoryUsage);

						if (totalMemoryUsage > maxMemoryUsage)
							maxMemoryUsage = totalMemoryUsage;
					}
				}

			} else {

				f.setAge(++age);

				Job delayJob = new Job(f.getId(), currentTime);
				delayJob.delayEntryTime = currentTime;
				delays.add(delayJob);

				if (sampleCountStarted) {
					totalColdStarts++;
					totalNotInitColdStarts++;
					f.totalColdStarts++;
				}

			}
		} else {

			if (evictAndAdd(f, true)) {

				f.setAge(++age);
				f.lastMemoryEntryTime = currentTime;

				job = new Job(f.getId(), currentTime);
				job.isCold = true;
				coldFunctionIds.add(job.fId);

				if (sampleCountStarted) {
					totalColdStarts++;
					f.totalColdStarts++;
					f.totalInitColdStarts++;
				}

				Job delayJob = new Job(f.getId(), currentTime);
				delayJob.delayEntryTime = currentTime;
				delays.add(delayJob);

				double memory = f.getMemoryRequirement();
				totalMemoryReservation += memory;

				totalMemoryUsage += memory;

				if (sampleCountStarted) {
					memoryReservationTimeSeries.put(currentTime, totalMemoryReservation);
					memoryUsageTimeSeries.put(currentTime, totalMemoryUsage);

					if (totalMemoryUsage > maxMemoryUsage)
						maxMemoryUsage = totalMemoryUsage;
				}

			} else {
				totalRequestDrop++;
				f.requestDropCount++;
			}
		}

		return job;
	}

	public void poll(double currentTime, boolean sampleCountStarted) {

		Iterator<Function> itr = inMemoryFunctions.iterator();
		while (itr.hasNext()) {
			Function f = (Function) itr.next();

			if (isIdle(f)) {

				double idlePeriod = currentTime - f.idlePeriodStartTime;

				if (idlePeriod >= f.idleTime) {

					f.updateTotalInMemoryTime(currentTime);
					f.lastMemoryEntryTime = -1;

					itr.remove();

					double memory = f.getMemoryRequirement();
					totalMemoryReservation -= memory;

					totalMemoryUsage -= f.idleMemoryRequirement;

					if (sampleCountStarted) {
						memoryReservationTimeSeries.put(currentTime, totalMemoryReservation);
						memoryUsageTimeSeries.put(currentTime, totalMemoryUsage);

						if (totalMemoryUsage > maxMemoryUsage)
							maxMemoryUsage = totalMemoryUsage;

					}

				}

			}
		}

	}

	public int dispatcher() {

		int jobs = 0;
		double time = 0;

		double interval = getNextArrival();

		double arrivalTime = time + interval;

		double serviceCompletionTime = Double.POSITIVE_INFINITY;

		int serviceCompletionCount = 0;

		double previousPollTime = 0;
		double pollTime = previousPollTime + pollingInterval;

		boolean sampleCountStarted = false;

		while (serviceCompletionCount + totalRequestDrop < numberOfSamples + numberOfSamplesToBeDiscarded) {

			if (arrivalTime == serviceCompletionTime) {
				
				System.out.println("Warning: Assumption doesn't hold! Will restart current iteration.");

				return -1;
			}

			double nextEvent = arrivalTime < serviceCompletionTime ? arrivalTime : serviceCompletionTime;

			/********* Polling Block *********/

			if (pollTime < nextEvent) {

				poll(pollTime, sampleCountStarted);
				previousPollTime = pollTime;
				pollTime = previousPollTime + pollingInterval;
				continue;

			}

			/********* Polling Block *********/

			// check that serviceCompletionCount and totalRequestDrop are in separate
			// branches and if their values are increased,
			// they are increased by 1 and only once in each iteration
			if (!sampleCountStarted && (serviceCompletionCount + totalRequestDrop) == numberOfSamplesToBeDiscarded) {

				sampleCountStartTime = nextEvent;
				sampleCountStarted = true;

			}

			jobs = jobList.size();

			if (arrivalTime < serviceCompletionTime) {

				Job job = getJob(arrivalTime, sampleCountStarted);

				double serviceTime = 0;

				if (jobs == 0) {

					time = arrivalTime;
					interval = getNextArrival();
					arrivalTime = time + interval;

					if (job != null) {

						jobs = 1;

						if (job.isCold)
							serviceTime = getNextColdStartTime(job.fId);
						else
							serviceTime = getNextServiceTime(job.fId);

						job.serviceTime = serviceTime;
						job.residualTime = serviceTime;
						jobList.add(job);

						// Utilisation calculation. Used time rather than arrival time since already
						// updated above
						if (sampleCountStarted) {
							double cpuCount = jobs > numberOfCPUs ? numberOfCPUs : jobs;
							cpuUsageTimeSeries.put(time, cpuCount);
						}

						serviceCompletionTime = time + serviceTime;

					}

				} else if (jobs > 0) {

					double workProcessed;
					if (jobs > numberOfCPUs)
						workProcessed = (arrivalTime - time) * numberOfCPUs / jobs;
					else
						workProcessed = (arrivalTime - time);

					for (Job j : jobList) {
						j.residualTime -= workProcessed;
					}

					time = arrivalTime;
					interval = getNextArrival();
					arrivalTime = time + interval;

					if (job != null) {

						jobs++;

						if (job.isCold)
							serviceTime = getNextColdStartTime(job.fId);
						else
							serviceTime = getNextServiceTime(job.fId);

						job.serviceTime = serviceTime;
						job.residualTime = serviceTime;
						jobList.add(job);

						// Utilisation calculation. Used time rather than arrival time since already
						// updated above
						if (sampleCountStarted) {
							double cpuCount = jobs > numberOfCPUs ? numberOfCPUs : jobs;
							cpuUsageTimeSeries.put(time, cpuCount);
						}

					}

					Collections.sort(jobList);

					// check
					if (jobs > numberOfCPUs)
						serviceCompletionTime = time + (jobList.get(0).residualTime * jobs / numberOfCPUs);
					else
						serviceCompletionTime = time + (jobList.get(0).residualTime);

				}

			} else if (serviceCompletionTime < arrivalTime) {

				jobs--;

				Job completedJob = jobList.remove(0);

				// Utilisation calculation
				if (sampleCountStarted) {
					double cpuCount = jobs > numberOfCPUs ? numberOfCPUs : jobs;
					cpuUsageTimeSeries.put(serviceCompletionTime, cpuCount);
				}

				if (!completedJob.isCold)
					serviceCompletionCount++;

				if (jobs == 0) {

					time = serviceCompletionTime;
					serviceCompletionTime = Double.POSITIVE_INFINITY;

				} else if (jobs > 0) {

					double workProcessed;
					if ((jobs + 1) > numberOfCPUs) // (jobs+1) as we reduced jobs before work calculation
						workProcessed = (serviceCompletionTime - time) * numberOfCPUs / (jobs + 1);
					else
						workProcessed = (serviceCompletionTime - time);

					for (Job j : jobList) {
						j.residualTime -= workProcessed;

					}

					time = serviceCompletionTime;

					// check
					if (jobs > numberOfCPUs)
						serviceCompletionTime = time + (jobList.get(0).residualTime * jobs / numberOfCPUs);
					else
						serviceCompletionTime = time + (jobList.get(0).residualTime);

				}

				completedJob.endTime = time;

				if (completedJob.isCold) {

					flushDelays(completedJob.fId, time);

					Collections.sort(jobList);

					// this line is important. check again.
					jobs = jobList.size();

					// Utilisation calculation. Because of flush delays
					if (sampleCountStarted) {
						double cpuCount = jobs > numberOfCPUs ? numberOfCPUs : jobs;
						cpuUsageTimeSeries.put(time, cpuCount);
					}

					// check
					if (jobs > numberOfCPUs)
						serviceCompletionTime = time + (jobList.get(0).residualTime * jobs / numberOfCPUs);
					else
						serviceCompletionTime = time + (jobList.get(0).residualTime);

				} else {

					if (sampleCountStarted)
						processJob(completedJob);

					Function f = functions.get(completedJob.fId);

					if (isIdle(f)) {

						totalMemoryUsage -= (f.memoryRequirement - f.idleMemoryRequirement);

						if (sampleCountStarted) {
							memoryUsageTimeSeries.put(time, totalMemoryUsage);

							if (totalMemoryUsage > maxMemoryUsage)
								maxMemoryUsage = totalMemoryUsage;
						}

						/***** Polling Block *****/

						f.idlePeriodStartTime = time;

						/***** Polling Block *****/

						/***** No Polling Block. For immediate removal *****/

						// inMemoryFunctions.remove(f);

						/***** No Polling Block. For immediate removal *****/

					}
				}
			}

		}

		writeResults(time);

		return 0;
	}

	public void flushDelays(int fId, double currentTime) {

		coldFunctionIds.remove(new Integer(fId));

		Iterator<Job> itr = delays.iterator();
		while (itr.hasNext()) {
			Job job = itr.next();
			if (job.fId == fId) {

				itr.remove();

				double serviceTime = getNextServiceTime(job.fId);

				job.serviceTime = serviceTime;
				job.residualTime = serviceTime;
				job.delayExitTime = currentTime;
				jobList.add(job);

			}
		}
	}

	public void writeResults(double time) {

		for (Function function : functions) {

			writeLog(function.name + " hit count: " + function.hitCount + " call count: " + function.callCount
					+ " total in-mem time: " + function.getTotalInMemoryTime(time));

		}

		int callCount = 0;
		for (Function function : functions) {
			callCount += function.callCount;

		}

		writeLog("Hit rate: " + (double) (hits) / (double) callCount);
		writeLog("Cold start rate: " + (double) totalColdStarts / (double) callCount);

		writeLog("Considered Simulation time: " + (time - sampleCountStartTime));

		processMemoryUsage((time - sampleCountStartTime));
		processCpuUsage((time - sampleCountStartTime));

		System.out.println(maxMemoryUsage);

		calculateIndividualFunctionResponseTimes();

	}

	public void processCpuUsage(double simulationTime) {

		double previousTime = 0;
		double lastCpuUsage = 0;
		double area = 0;

		for (Map.Entry<Double, Double> entry : cpuUsageTimeSeries.entrySet()) {

			area += (entry.getKey() - previousTime) * lastCpuUsage;
			previousTime = entry.getKey();
			lastCpuUsage = entry.getValue();

		}

		double util = area / simulationTime;

		System.out.println("Utilisation: " + util);

		try {

			File targetFile = new File(cpuFile);
			File parent = targetFile.getParentFile();
			if (!parent.exists() && !parent.mkdirs()) {
				throw new IllegalStateException("Couldn't create dir: " + parent);
			}

			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(cpuFile), true));

			bw.write("Utilisation: " + util + "\n");

			bw.close();
		} catch (IOException e) { // TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void processMemoryUsage(double simulationTime) {

		double previousTime = 0;
		double lastMemoryUsage = 0;
		double area = 0;

		for (Map.Entry<Double, Double> entry : memoryReservationTimeSeries.entrySet()) {

			area += (entry.getKey() - previousTime) * lastMemoryUsage;
			previousTime = entry.getKey();
			lastMemoryUsage = entry.getValue();

		}

		double avgMemory = area / simulationTime;
		avgMemory /= 1024;

		previousTime = 0;
		lastMemoryUsage = 0;
		area = 0;
		double max = -1;
		for (Map.Entry<Double, Double> entry : memoryUsageTimeSeries.entrySet()) {

			area += (entry.getKey() - previousTime) * lastMemoryUsage;
			previousTime = entry.getKey();
			lastMemoryUsage = entry.getValue();

			if (lastMemoryUsage > max) {
				max = lastMemoryUsage;
			}
		}

		if (max != maxMemoryUsage) {
			System.out.println("Memory Warning. Map max: " + max + "Continuous max: " + maxMemoryUsage);
		}

		double avgIdleActiveMemory = area / simulationTime;
		avgIdleActiveMemory /= 1024;

		try {

			File targetFile = new File(memoryReservationFile);
			File parent = targetFile.getParentFile();
			if (!parent.exists() && !parent.mkdirs()) {
				throw new IllegalStateException("Couldn't create dir: " + parent);
			}

			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(memoryReservationFile), true));

			bw.write("Average Memory Reservation (GB/S): " + avgMemory + "\n");

			bw.close();

			targetFile = new File(memoryUsageFile);
			parent = targetFile.getParentFile();
			if (!parent.exists() && !parent.mkdirs()) {
				throw new IllegalStateException("Couldn't create dir: " + parent);
			}

			bw = new BufferedWriter(new FileWriter(new File(memoryUsageFile), true));

			bw.write("MaxGB," + maxMemoryUsage / 1024 + ",AverageGB," + avgIdleActiveMemory + "\n");

			bw.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public boolean evictAndAdd(Function function, boolean checkIdle) {

		boolean success = false;

		if (inMemoryFunctions.size() < memorySpace) {
			inMemoryFunctions.add(function);
			success = true;
		} else {
			if (checkIdle) {
				Collections.sort(inMemoryFunctions);

				Iterator<Function> itr = inMemoryFunctions.iterator();
				while (itr.hasNext()) {

					Function tmpFunction = itr.next();

					if (isIdle(tmpFunction)) {

						itr.remove();

						inMemoryFunctions.add(function);
						success = true;
						break;
					}
				}
			}
		}

		return success;
	}

	public static void main(String[] args) {

		int numberOfSamples, numberOfFunctions;

		double zipfParameter, arrivalRate, spaceToFunctionRatio;
		int iterStart;

		String expectedHitrate;
		String experimentFolder, parameterRootDir;
		experimentFolder = "experiment";
		parameterRootDir = experimentFolder;

		spaceToFunctionRatio = 1;

		numberOfSamples = 10000000;
		int numberOfCpus;
		int iterationLimit = 11;

		if (args.length == 0) {
			numberOfFunctions = 64;
			arrivalRate = 0.5;
			zipfParameter = 0.6;
			expectedHitrate = "cocoa";
			numberOfCpus = 2;
			iterStart = 11;

		} else {

			numberOfFunctions = Integer.parseInt(args[0]);
			arrivalRate = Double.parseDouble(args[1]);
			zipfParameter = Double.parseDouble(args[2]);
			expectedHitrate = args[3];
			numberOfCpus = Integer.parseInt(args[4]);
			iterStart = Integer.parseInt(args[5]);

		}

		for (int iter = iterStart; iter <= iterationLimit; iter++) {

			FaasSim sim = new FaasSim(numberOfCpus, numberOfSamples, numberOfFunctions, spaceToFunctionRatio,
					zipfParameter, arrivalRate, iter, expectedHitrate, experimentFolder, parameterRootDir);
			int code;
			code = sim.dispatcher();

			if (code == -1)
				--iter;

		}
	}
}
