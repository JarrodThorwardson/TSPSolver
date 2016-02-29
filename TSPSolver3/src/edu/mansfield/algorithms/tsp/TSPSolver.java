package edu.mansfield.algorithms.tsp;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TSPSolver implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5685114427785070701L;
	private static volatile int[] bestArray, swapIndex, initialPermute;
	private static volatile long time;
	private static volatile long runTime;
	private static volatile int[][] matrix, pathArray, nnArray;
	private static volatile int bestValue, threadCount, lowEstimate, watchperson, nearestWatchPerson;
	private static boolean distributed;
	private static TSPSingle[] singles;

	public TSPSolver(int[][] inputMatrix, int[] inputPermute, int indexSentinel, int estimate, boolean multiMachine) {
		matrix = inputMatrix.clone();
		initialPermute = inputPermute.clone();
		watchperson = indexSentinel;
		lowEstimate = estimate;
		distributed = multiMachine;
	}

	public int[] getInitialPermute() {
		return initialPermute;
	}

	public void setInitialPermute(int[] initialPermute) {
		TSPSolver.initialPermute = initialPermute;
	}

	public int getLowEstimate() {
		return lowEstimate;
	}

	public void setLowEstimate(int lowEstimate) {
		TSPSolver.lowEstimate = lowEstimate;
	}

	public int getWatchperson() {
		return watchperson;
	}

	public void setWatchperson(int watchperson) {
		TSPSolver.watchperson = watchperson;
	}

	public static void main(String[] args) throws FileNotFoundException {
		TSPSolver tsp;
		int watchPersonToo = 1;
		int firstEstimate;
		int[][] start;
		int[] startHere;
		int[] shortest;
		boolean test = false;
		String matrixString = fileReadIn();

		start = StringToIntMatrix(matrixString);
		startHere = new int[start[0].length];
		firstEstimate = lowEstimateEval(start);
		for (int i = 0; i < startHere.length; i++) {
			startHere[i] = i;
		}
		
		//forcefeeding it the problematic subset
		/*startHere[1] = 9;
		startHere[9] = 1;
		bubbleSort(startHere, watchPersonToo);*/
		
		System.out.println("Matrix symmetry: " + symmetryCheck(start));

		tsp = new TSPSolver(start, startHere, watchPersonToo, firstEstimate, test);
		shortest = tsp.solve();

		System.out.println("Best path:\t" + MatrixLineToString(shortest) + "\tBest Distance:\t"
				+ threadablePermuteValue(shortest, start));
		System.out.println("Run time in milliseconds: " + runTime);

		// Time formatting code taken from
		// http://stackoverflow.com/questions/9027317/how-to-convert-milliseconds-to-hhmmss-format
		System.out.println("hh:mm:ss " + String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(runTime),
				TimeUnit.MILLISECONDS.toMinutes(runTime)
						- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(runTime)),
				TimeUnit.MILLISECONDS.toSeconds(runTime)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(runTime))));

	}

	public int[] solve() {
		time = System.currentTimeMillis();
		bestArray = threadLaunchingPermuteFinder();
		runTime = System.currentTimeMillis() - time;
		return bestArray;
	}

	public static boolean symmetryCheck(int[][] unknownMatrix) {
		boolean symmetric = true;
		int nodes = unknownMatrix[0].length;

		for (int i = 0; i < nodes; i++) {
			for (int j = 0; j < nodes; j++) {
				if (unknownMatrix[i][j] != unknownMatrix[j][i]) {
					symmetric = false;
				}
			}
		}

		return symmetric;
	}

	public static int[] threadLaunchingPermuteFinder() {
		// no need to start more than n/2 threads, assuming symmetric graph
		// each thread shouldn't have to run more than factorial(matrix.length
		// -2) since index 0 needn't increment
		/*
		 * 012345 = 012345 terminates at: 054321 123450 = 102345 terminates at:
		 * 154320 234501 = 201345 terminates at: 254310 345012 = 301245
		 * terminates at: 354210 450123 = 401235 terminates at: 453210 501234 =
		 * 501234 terminates at: 543210
		 */
		int cores = Runtime.getRuntime().availableProcessors();
		int[] currentPermute = new int[matrix[0].length];
		double threadCeiling = 0;
		// initializing currentPermute with a valid path
		currentPermute = initialPermute.clone();

		bestArray = currentPermute.clone();
		bestValue = Integer.MAX_VALUE;

		// Launching sufficient threads, assuming a symmetric graph.
		if (!distributed && TSPSolver.symmetryCheck(matrix)) {
			threadCeiling = Math.ceil((currentPermute.length - watchperson) / 2.0);
		} else {
			threadCeiling = currentPermute.length - watchperson;
		}

		threadCount = (int) threadCeiling;
		// Arrays used to collect results from the various threads.
		// valueArray = new int[threadCount];
		pathArray = new int[threadCount][bestArray.length];
		singles = new TSPSingle[threadCount];
		final int[][] maybeReadIssue = matrix.clone();
		final int watcher = watchperson;
		final int estimated = lowEstimate;
		swapIndex = bestArray.clone();
		
		for (int i=0;i<threadCount;i++){
			currentPermute[watchperson] = swapIndex[i+watchperson];
			currentPermute[i+watchperson] = swapIndex[watchperson];
			TSPSolver.bubbleSort(currentPermute, watchperson);
			singles[i] = new TSPSingle(maybeReadIssue, currentPermute, watcher, estimated);
			System.out.println("Current path: " + TSPSolver.MatrixLineToString(currentPermute) + " Current distance: "
					+ TSPSolver.threadablePermuteValue(currentPermute, matrix));
			currentPermute = swapIndex.clone();
		}
		

		System.out.println("Best Distance to Start: " + lowEstimate);

		// Many thanks to Squirtle Squad for examples of how to launch multiple
		// threads in Java.
		
		ExecutorService threading = Executors.newFixedThreadPool(cores);

		for (int i = 0; i < threadCount; i++) {
			final int threadID = i;
			threading.execute(new Runnable() {
				@Override
				public void run() {
					try {
						pathArray[threadID] = singles[threadID].dynSolve();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			/*aThread.start();
			threads.add(aThread);*/

		}
		
		threading.shutdown();
		
		while(!threading.isTerminated()){
			//wait until it is
		}


		for (int[] path : pathArray) {
			// first array is not written to programatically.
			if (TSPSolver.threadablePermuteValue(path, matrix) <= bestValue) {
				bestArray = path.clone();
				bestValue = TSPSolver.threadablePermuteValue(path, matrix);
			}
			System.out.println("This path:\t" + TSPSolver.MatrixLineToString(path) + "\tThis Distance:\t"
					+ TSPSolver.threadablePermuteValue(path, matrix));
		}

		return bestArray;
	}

	public static int lowEstimateEval(int[][] evalMatrix) {
		NearestNeighbor2 nnSolver = new NearestNeighbor2();
		lowEstimate = nnSolver.oldNearestNeighborRun(evalMatrix);
		nnArray = nnSolver.oldNearestNeighborRunArray(evalMatrix);
		//magic constant determined by informal testing.
		if (nnArray[0].length > 17) {
			nearestWatchPerson = nnArray[0].length - 11;
		} else {
			nearestWatchPerson = nnArray[0].length / 2;
		}

		for (int[] permutePath : nnArray) {
			TSPSolver.bubbleSort(permutePath, nearestWatchPerson);
			permutePath = TSPSolver.threadablePermuteFinding(evalMatrix, permutePath, lowEstimate, nearestWatchPerson);

			/*
			 * System.out.println("Approximation path: " +
			 * tsp.MatrixLineToString(permutePath) + " Approximation distance: "
			 * + tsp.threadablePermuteValue(permutePath, matrix));
			 */
			if (TSPSolver.threadablePermuteValue(permutePath, evalMatrix) < lowEstimate) {
				lowEstimate = TSPSolver.threadablePermuteValue(permutePath, evalMatrix);
			}
		}
		return lowEstimate;
	}

	/*
	 * Evaluates all possible permutations, and uses branch-and-bound
	 * implementation.
	 */

	public static int[] threadablePermuteFinding(int[][] clonedMatrix, int[] initial, int estimate, int sentinelIndex) {
		int[] bestArray1 = initial.clone();
		int bestValue1 = Integer.MAX_VALUE;
		int currentValue = 0;
		int sentinel = initial[sentinelIndex];
		// int lowEstTrip = initial[sentinelIndex + 1]; This, even done so
		// infrequently as seen below,
		// results in far too many issues with threads waiting their turn to be
		// worthwhile at 19 cities.
		
		/*System.out.println("Task #" + sentinel + " started: " + MatrixLineToString(initial) + " Distance: "
				+ threadablePermuteValue(initial, clonedMatrix));*/

		while (initial[sentinelIndex] == sentinel) {
			initial = TSPSolver.threadablePermuteBranchBound(initial, clonedMatrix, estimate);
			currentValue = TSPSolver.threadablePermuteValue(initial, clonedMatrix);
			if (currentValue < bestValue1) {
				bestArray1 = initial.clone();
				bestValue1 = currentValue;
				estimate = bestValue1;
			}
			/*
			 * if(lowEstTrip < initial[sentinelIndex + 1] - 3){ if(bestValue1 <
			 * lowEstimate){ lowEstimate = bestValue1; } if(lowEstimate <
			 * bestValue1){ bestValue1 = lowEstimate; } lowEstTrip =
			 * initial[sentinelIndex + 1]; }
			 */
			// System.out.print(printArray(currentPermute));
			// System.out.println("\tDistance:\t" + currentValue);
			initial = TSPSolver.getLexes(initial);
		}
		/*System.out.println("Task #" + sentinel + " done: " + MatrixLineToString(initial) + " Distance: "
				+ threadablePermuteValue(initial, clonedMatrix));*/
		return bestArray1;
	}

	/*
	 * helper method which assesses candidate permutation for branch and bound
	 * purposes.
	 */
	public static int[] threadablePermuteBranchBound(int[] sortable, int[][] matrix, int estimate) {
		int num = 0;

		for (int i = 0; i < sortable.length - 1; i++) {
			num += matrix[sortable[i]][sortable[i + 1]];
			if (num > estimate) {
				TSPSolver.insertionSort(sortable, i+1);
				return sortable;
			}
		}

		//num += matrix[sortable[sortable.length - 1]][sortable[0]];

		return sortable;
	}

	/*
	 * threadablePermuteValue only assesses the cost of the path, in order to
	 * not assume single-threading
	 */
	public static int threadablePermuteValue(int[] permute, int[][] matrix) {
		int num = 0;

		for (int i = 0; i < permute.length - 1; i++) {
			num += matrix[permute[i]][permute[i + 1]];
		}

		num += matrix[permute[permute.length - 1]][permute[0]];

		return num;
	}

	/*
	 * nextPermutaion will return the next permutation of the input.
	 */
	public static int[] getLexes(int[] currentPermute) {
		currentPermute = TSPSolver.next_permutation(currentPermute);
		return currentPermute;
	}

	/*
	 * When given an array of ints this will return the corresponding symbols
	 * from a long list of symbols to indicate which order jumps are made in.
	 */
	public static String MatrixLineToString(int[] matrixJumps) {
		String alteredMatrixLine = "";
		String lineValues = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789)!@#$%^&*(";

		for (int i = 0; i < matrixJumps.length; i++) {
			alteredMatrixLine += lineValues.substring(matrixJumps[i], matrixJumps[i] + 1) + " ";
		}
		alteredMatrixLine += alteredMatrixLine.substring(0, 1);

		return alteredMatrixLine;
	}

	/*
	 * The StringToIntMatrix takes the string that has been entered into the
	 * method requirements and then converts it into a new int[][] matrix. It
	 * then returns the matrix.
	 */
	public static int[][] StringToIntMatrix(String matrixString) {
		int[][] matrix;
		String[] singleMatrixLine;
		String[] matrixLinesArray;

		matrixLinesArray = matrixString.split("\n");
		matrix = new int[matrixLinesArray.length][matrixLinesArray.length];
		for (int i = 0; i < matrixLinesArray.length; i++) {
			singleMatrixLine = matrixLinesArray[i].split("\\D+?");
			for (int j = 0; j < singleMatrixLine.length; j++) {
				matrix[i][j] = Integer.parseInt(singleMatrixLine[j]);
			}
		}

		return matrix;
	}

	/*
	 * The fileReadIn method asks for the file that the user wants to use as
	 * their dataset. It then reads in each line of the file and puts this into
	 * a variable after attaching a newline character to the end of the line.
	 * This variable is then returned for the use of the rest of the program.
	 */
	public static String fileReadIn() throws FileNotFoundException {
		Scanner inputGetter = new Scanner(System.in);
		Scanner sc;
		String fileName = "", inputArrayString = "";
		File file;

		System.out.print("Please enter the name of the file in which the array is stored: ");
		fileName = inputGetter.nextLine();
		if (fileName.equalsIgnoreCase("")){
			fileName = "15in.txt";
		}
		file = new File(fileName);
		sc = new Scanner(file);

		while (sc.hasNext()) {
			inputArrayString += sc.nextLine() + "\n";
		}

		sc.close();
		inputGetter.close();

		return inputArrayString;
	}

	/*
	 * The following methods are copied from the LexPermsOriginal and modified
	 * for use in this class. print, swap and next_permute also appear in
	 * LexPermsOriginal in different forms. Check the class LexPermsOriginal if
	 * you need to see their original code.
	 */
	public static String print(int[] array) {
		String str = "";
		for (int tmp : array) {
			str += " " + tmp;
		}
		str += "\n";
		return str;
	}

	public static int[] next_permutation(int[] array) {
		int i, j;
		for (i = array.length - 2; i >= 0; i--) {
			if (array[i] < array[i + 1])
				break;
		}
		if (i < 0) {
			// System.out.println("End");
			// System.exit(0);
			return array;
		}

		for (j = array.length - 1; j > i; j--) {
			if (array[j] > array[i])
				break;
		}

		swap(array, i++, j);

		for (j = array.length - 1; j > i; i++, j--) {
			swap(array, i, j);
		}
		return array;
	}

	public static String printArray(int[] array) {
		String str = "";
		for (int tmp : array) {
			str += " " + tmp;
		}
		return str;
	}

	public static void insertionSort(int[] permute, int position) {
		/*
		 * This code is based on the implementation shown in Java Foundations
		 * Second Edition
		 */
		position++;

		for (int l = position; l < permute.length; l++) {
			int key = permute[l];
			int p = l;

			while (p > position && permute[p - 1] <= key) {
				permute[p] = permute[p - 1];
				p--;
			}

			permute[p] = key;
		}
	}

	public static void upInsertionSort(int[] permute, int position) {
		/*
		 * This code is based on the implementation shown in Java Foundations
		 * Second Edition
		 */
		position++;

		for (int l = position; l < permute.length; l++) {
			int key = permute[l];
			int p = l;

			while (p < position && permute[p - 1] >= key) {
				permute[p] = permute[p - 1];
				p--;
			}

			permute[p] = key;
		}
	}

	public static void bubbleSort(int[] a, int position) {
		int out, in;
		position++;
		for (out = a.length - 1; out > position; out--)
			for (in = position; in < out; in++)
				if (a[in] > a[in + 1]) {
					int temp = a[in];
					a[in] = a[in + 1];
					a[in + 1] = temp;
				}
	}

	public static void swap(int[] array, int x, int y) {
		array[x] ^= array[y];
		array[y] ^= array[x];
		array[x] ^= array[y];
	}

}
