package edu.mansfield.algorithms.tsp.thor;

import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

import edu.mansfield.algorithms.tsp.TSPSolver;

public class TSPGinsu {

	public static void main(String[] args) throws FileNotFoundException {
		long time, runTime, tempTime, tempRunTime;
		TSPSolver tsp;
		int watchPersonToo = 1;
		int watchPersonAlso = 2;
		int currentEstimate;
		int[][] start = null;
		int[] shorterOne = null;
		int[] startHere, swapIndex, swapIndexExtra = null;
		int[] shortest = null;
		boolean test = true;
		int tspsPlural;
		String matrixString = TSPSolver.fileReadIn();
		time = System.currentTimeMillis();

		start = TSPSolver.StringToIntMatrix(matrixString);
		currentEstimate = TSPSolver.lowEstimateEval(start);
		startHere = new int[start[0].length];
		shorterOne = new int[start[0].length];
		for (int i = 0; i < startHere.length; i++) {
			startHere[i] = i;
		}
		swapIndex = startHere.clone();
		swapIndexExtra = startHere.clone();

		if (TSPSolver.symmetryCheck(start)) {
			tspsPlural = (int) Math.ceil(startHere.length / 2.0);
		} else {
			tspsPlural = startHere.length;
		}
		
		tsp = new TSPSolver(start, startHere, watchPersonAlso, currentEstimate, test);
		
		for(int i=0; i<tspsPlural;i++){
			swapIndexExtra[watchPersonToo] = swapIndex[i+watchPersonToo];
			swapIndexExtra[i+watchPersonToo] = swapIndex[watchPersonToo];
			TSPSolver.bubbleSort(swapIndexExtra, watchPersonToo);
			startHere = swapIndexExtra.clone();
			swapIndexExtra = swapIndex.clone();
			
			tempTime = System.currentTimeMillis();
			
			tsp.setInitialPermute(startHere);
			tsp.setLowEstimate(currentEstimate);
			shorterOne = tsp.solve();
			
			tempRunTime = System.currentTimeMillis() - tempTime;
			System.out.println("Starting path:\t" + TSPSolver.MatrixLineToString(startHere) + "\tStarting Distance:\t"
			+ TSPSolver.threadablePermuteValue(startHere, start));
			
			System.out.println("Temp run time in milliseconds: " + tempRunTime);
			if(currentEstimate>TSPSolver.threadablePermuteValue(shorterOne, start)){
				currentEstimate = TSPSolver.threadablePermuteValue(shorterOne, start);
				shortest = shorterOne.clone();
			}
		}
		runTime = System.currentTimeMillis() - time;

		System.out.println("Best path:\t" + TSPSolver.MatrixLineToString(shortest) + "\tBest Distance:\t"
				+ TSPSolver.threadablePermuteValue(shortest, start));
		System.out.println("Run time in milliseconds: " + runTime);

		// Time formatting code taken from
		// http://stackoverflow.com/questions/9027317/how-to-convert-milliseconds-to-hhmmss-format
		System.out.println("hh:mm:ss " + String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(runTime),
				TimeUnit.MILLISECONDS.toMinutes(runTime)
						- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(runTime)),
				TimeUnit.MILLISECONDS.toSeconds(runTime)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(runTime))));

	}

}
