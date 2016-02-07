package edu.mansfield.algorithms.tsp;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class TSPSolver {
	static volatile int[] bestArray, swapIndex, valueArray;
	static volatile long time;
	static volatile long runTime;
	static volatile long possiblePermutes;
	static volatile int[][] matrix, pathArray;
	static volatile int bestValue, threadCount, lowEstimate, watchperson;

	public static void main(String[] args) throws FileNotFoundException {
		TSPSolver tsp = new TSPSolver();
		watchperson = 1;
		int[] currentPermute;
		double threadCeiling = 0;
		NearestNeighbor2 nnSolver = new NearestNeighbor2();
		String matrixString = tsp.fileReadIn();
		time = System.currentTimeMillis();
		matrix = tsp.StringToIntMatrix(matrixString);
		lowEstimate = nnSolver.oldNearestNeighborRun(matrix);
		
		//possiblePermutes is involved in optimizing based on a symmetrical graph
		possiblePermutes = (factorial(matrix[0].length - 2));
		
		//currentPermute is used for tracking which route is being evaluated
		currentPermute = new int[matrix[0].length];
		
		//no need to start more than n/2 threads, assuming symmetric graph
		//each thread shouldn't have to run more than factorial(matrix.length -2) since index 0 needn't increment
		/* 012345 = 012345 terminates at: 054321
		 * 123450 = 102345 terminates at: 154320
		 * 234501 = 201345 terminates at: 254310
		 * 345012 = 301245 terminates at: 354210
		 * 450123 = 401235 terminates at: 453210
		 * 501234 = 501234 terminates at: 543210
		 * */
		
		
		//initializing currentPermute with a valid path
		for(int i = 0; i < currentPermute.length; i++){
			currentPermute[i] = i;
		}
		
		bestArray = currentPermute.clone();
		bestValue = Integer.MAX_VALUE;
		// Launching sufficient threads, assuming a symmetric graph.
		threadCeiling = Math.ceil(currentPermute.length / 2.0);
		threadCount = (int) threadCeiling;
		// Arrays used to collect results from the various threads.
		//valueArray = new int[threadCount];
		pathArray = new int[threadCount][bestArray.length];
		
		//Many thanks to Squirtle Squad for examples of how to launch multiple threads in Java.
		swapIndex = bestArray.clone();
		ArrayList<Thread> threads = new ArrayList<Thread>();
		
		for (int i=0; i< threadCount; i++) {
			currentPermute[watchperson] = swapIndex[i+1];
			currentPermute[i+1] = swapIndex[1];
			bubbleSort(currentPermute, 1);
			final int[] threadPermute = currentPermute.clone();
			final int threadID = i;
			Thread aThread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						TSPSolver threadTSP = new TSPSolver();
						int[][] maybeReadIssue = matrix.clone();
						pathArray[threadID] = threadTSP.threadablePermuteFinding(maybeReadIssue, threadPermute, possiblePermutes, lowEstimate, watchperson).clone();
						//valueArray[threadID] = threadTSP.threadablePermuteValue(pathArray[threadID], matrix);
					} catch (Exception e) {
						e.printStackTrace();
					}					
				}
			});

			System.out.println("Current path: " + tsp.MatrixLineToString(currentPermute) + 
					" Current distance: " + tsp.threadablePermuteValue(currentPermute, matrix));
			currentPermute = swapIndex.clone();
			aThread.start();
			threads.add(aThread);
			
		}

		boolean threadsAreAlive;

		do {
			threadsAreAlive = false;
			for (Thread thread : threads) {
				if (thread.isAlive()) {
					threadsAreAlive = true;
				}
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (threadsAreAlive);

		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		for (int[] path : pathArray){
			if (tsp.threadablePermuteValue(path, matrix) < bestValue){
				bestArray = path.clone();
				bestValue = tsp.threadablePermuteValue(path, matrix);
			}
			System.out.println("This path:\t" + tsp.MatrixLineToString(path) + "\tThis Distance:\t" + tsp.threadablePermuteValue(path, matrix));
		}
		
		runTime = System.currentTimeMillis() - time;
		
		System.out.println("Best path:\t" + tsp.MatrixLineToString(bestArray) + "\tBest Distance:\t" + bestValue);
		System.out.println("Run time in milliseconds: " + runTime);
		
		// Time formatting code taken from http://stackoverflow.com/questions/9027317/how-to-convert-milliseconds-to-hhmmss-format
		System.out.println("hh:mm:ss " + 
				String.format("%02d:%02d:%02d", 
					    TimeUnit.MILLISECONDS.toHours(runTime),
					    TimeUnit.MILLISECONDS.toMinutes(runTime) - 
					    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(runTime)),
					    TimeUnit.MILLISECONDS.toSeconds(runTime) - 
					    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(runTime))));
		
	}
	
	/*
	 * Evaluates all possible permutations, and uses branch-and-bound implementation.
	 * */
	
	public int[] threadablePermuteFinding(int[][] clonedMatrix, int[] initial, long loops, int estimate, int sentinelIndex){
		TSPSolver tsp1 = new TSPSolver();
		int[] bestArray1 = initial.clone();
		int bestValue1 = Integer.MAX_VALUE;
		int currentValue = 0;
		int sentinel = initial[sentinelIndex];
		
		for (long i = 0; i < loops; i++) {
			initial = tsp1.threadablePermuteBranchBound(initial, clonedMatrix, estimate);
			currentValue = tsp1.threadablePermuteValue(initial, clonedMatrix);
			if(currentValue < bestValue1){
				bestArray1 = initial.clone();
				bestValue1 = currentValue;
				estimate = bestValue1;
			}
	//		System.out.print(printArray(currentPermute));		
	//		System.out.println("\tDistance:\t" + currentValue);
			initial = tsp1.getLexes(initial);
			if (initial[sentinelIndex] != sentinel){
				return bestArray1;
			}
		}
		System.out.println("Best path lol:\t" + tsp1.MatrixLineToString(bestArray1) + "\tBest Distance lol:\t" + bestValue1);
		return bestArray1;
	}
	
	/*
	 * helper method which assesses candidate permutation for branch and bound purposes.
	 * */
	public int[] threadablePermuteBranchBound(int[] sortable, int[][] matrix, int estimate){
		int num = 0;
		
		for(int i = 0; i < sortable.length - 1; i++){
			num += matrix[sortable[i]][sortable[i+1]];
			if(num > estimate){
				insertionSort(sortable, i);
				return sortable;
			}
		}
		
		num += matrix[sortable[sortable.length-1]][sortable[0]];
		
		return sortable;
	}
	/*
	 * threadablePermuteValue only assesses the cost of the path, in order to not assume single-threading
	 * */
	public int threadablePermuteValue(int[] permute, int[][] matrix){
		int num = 0;
		
		for(int i = 0; i < permute.length - 1; i++){
			num += matrix[permute[i]][permute[i+1]];
		}
		
		num += matrix[permute[permute.length-1]][permute[0]];
		
		return num;
	}
	

	/*
	 * nextPermutaion will return the next permutation of the input.
	 */
	public int[] getLexes(int[] currentPermute) {
		currentPermute = next_permutation(currentPermute);
		return currentPermute;
	}

	/*
	 * When given an array of ints this will return the corresponding symbols
	 * from a long list of symbols to indicate which order jumps are made in.
	 */
	public String MatrixLineToString(int[] matrixJumps) {
		String alteredMatrixLine = "";
		String lineValues = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789)!@#$%^&*(";

		for (int i = 0; i < matrixJumps.length; i++) {
			alteredMatrixLine += lineValues.substring(matrixJumps[i],
					matrixJumps[i] + 1) + " ";
		}
		alteredMatrixLine += alteredMatrixLine.substring(0, 1);

		return alteredMatrixLine;
	}

	/*
	 * The StringToIntMatrix takes the string that has been entered into the
	 * method requirements and then converts it into a new int[][] matrix. It
	 * then returns the matrix.
	 */
	public int[][] StringToIntMatrix(String matrixString) {
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
	public String fileReadIn() throws FileNotFoundException {
		Scanner inputGetter = new Scanner(System.in);
		Scanner sc;
		String fileName = "", inputArrayString = "";
		File file;

		System.out
				.print("Please enter the name of the file in which the array is stored: ");
		fileName = inputGetter.nextLine();
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
	 * The factorial method will return the factorial of any int in the method's
	 * input.
	 */
	public static long factorial(long n) {
		long fact = 1; // this will be the result
		for (long i = 1; i <= n; i++) {
			fact *= i;
		}
		return fact;
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
			//System.out.println("End");
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
	
	public static void insertionSort(int[] permute, int position){
		/*
		 * This code is based on the implementation shown in Java Foundations Second Edition
		 */
		position++;

			for(int l=position;l<permute.length;l++){
				int key = permute[l];
				int p = l;
				
				while(p > position &&permute[p-1] <= key){
					permute[p] = permute[p-1];
					p--;
				}
				
				permute[p] = key;
			}
	}
	
	public static void upInsertionSort(int[] permute, int position){
		/*
		 * This code is based on the implementation shown in Java Foundations Second Edition
		 */
		position++;

			for(int l=position;l<permute.length;l++){
				int key = permute[l];
				int p = l;
				
				while(p < position &&permute[p-1] >= key){
					permute[p] = permute[p-1];
					p--;
				}
				
				permute[p] = key;
			}
	}
	
	public static void bubbleSort(int[] a, int position)
	{
		int out, in;
		position++;
		for(out=a.length-1; out>position; out--)
			for(in=position; in<out; in++)
				if(a[in] > a[in+1])
				{
					int temp = a[in];
					a[in] = a[in+1];
					a[in+1] = temp;
				}
	}

	public static void swap(int[] array, int x, int y) {
		array[x] ^= array[y];
		array[y] ^= array[x];
		array[x] ^= array[y];
	}

}
