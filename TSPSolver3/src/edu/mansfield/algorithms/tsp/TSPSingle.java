package edu.mansfield.algorithms.tsp;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class TSPSingle implements Runnable {

	private int[][] matrix;
	private int[] initialPermute, bestArray, comparePermute, permuteSums;
	private int watchPerson, lowEstimate, sentinel, compareSentinel;

	public TSPSingle(int[][] inputMatrix, int[] inputPermute, int indexSentinel, int estimate) {
		matrix = inputMatrix.clone();
		initialPermute = inputPermute.clone();
		bestArray = new int[inputPermute.length];
		watchPerson = indexSentinel;
		lowEstimate = estimate;
		sentinel = inputPermute[indexSentinel];
		comparePermute = initialPermute.clone();
		permuteSums = new int[initialPermute.length];
		permuteSums[0] = matrix[initialPermute[0]][initialPermute[1]];
		for (int i = 1; i < permuteSums.length - 1; i++) {
			permuteSums[i] = permuteSums[i - 1] + matrix[initialPermute[i]][initialPermute[i + 1]];
		}
		compareSentinel = permuteSums.length / 2;
	}

	public int[] getInitialPermute() {
		return initialPermute;
	}

	public void setInitialPermute(int[] initialPermute1) {
		initialPermute = initialPermute1;
	}

	public static void main(String[] args) throws FileNotFoundException {
		TSPSingle tsp, xtsp;
		int watchPersonToo = 0;
		int firstEstimate;
		long time, runTime, xtime, xrunTime;
		int[][] start;
		int[] startHere;
		int[] shortest, xshortest;
		String matrixString = fileReadIn();

		start = StringToIntMatrix(matrixString);
		startHere = new int[start[0].length];
		firstEstimate = 200;
		for (int i = 0; i < startHere.length; i++) {
			startHere[i] = i;
		}

		tsp = new TSPSingle(start, startHere, watchPersonToo, firstEstimate);
		xtsp = new TSPSingle(start, startHere, watchPersonToo, firstEstimate);
		time = System.currentTimeMillis();
		shortest = tsp.solve();
		runTime = System.currentTimeMillis() - time;

		System.out.println("Best path:\t" + tsp.MatrixLineToString(shortest) + "\tBest Distance:\t"
				+ TSPSolver.threadablePermuteValue(shortest, start));
		System.out.println("Run time in milliseconds: " + runTime);

		xtime = System.currentTimeMillis();
		xshortest = xtsp.dynSolve();
		xrunTime = System.currentTimeMillis() - xtime;

		System.out.println("xBest path:\t" + tsp.MatrixLineToString(xshortest) + "\txBest Distance:\t"
				+ TSPSolver.threadablePermuteValue(xshortest, start));
		System.out.println("xRun time in milliseconds: " + xrunTime);

		// Time formatting code taken from
		// http://stackoverflow.com/questions/9027317/how-to-convert-milliseconds-to-hhmmss-format
		System.out.println("hh:mm:ss " + String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(runTime),
				TimeUnit.MILLISECONDS.toMinutes(runTime)
						- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(runTime)),
				TimeUnit.MILLISECONDS.toSeconds(runTime)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(runTime))));

		System.out.println("xhh:mm:ss " + String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(xrunTime),
				TimeUnit.MILLISECONDS.toMinutes(xrunTime)
						- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(xrunTime)),
				TimeUnit.MILLISECONDS.toSeconds(xrunTime)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(xrunTime))));

	}

	public int[] solve() {
		this.permuteFinding();
		return bestArray;
	}

	public int[] dynSolve() {
		this.dynpermuteFinding();
		return bestArray;
	}

	private void permuteFinding() {

		while (initialPermute[watchPerson] == sentinel) {
			this.permuteBranchBound();
			this.getLexes(initialPermute);
		}
	}

	private void dynpermuteFinding() {

		while (initialPermute[watchPerson] == sentinel) {
			this.dynBranchBound();
			this.getLexes(initialPermute);
		}

	}

	private void permuteBranchBound() {
		/* Opportunity for linear/dynamic programming in this method. */

		int num = 0;

		for (int i = 0; i < initialPermute.length - 1; i++) {
			num += matrix[initialPermute[i]][initialPermute[i + 1]];
			if (num > lowEstimate) {
				this.insertionSort(initialPermute, i + 1);
				return;
			}
		}

		num += matrix[initialPermute[initialPermute.length - 1]][initialPermute[0]];
		if (num < lowEstimate) {
			bestArray = initialPermute.clone();
			lowEstimate = num;
		}

		return;
	}

	private void dynBranchBound() {
		/* Opportunity for linear/dynamic programming in this method. */

		if (compareSentinel == 0) {
			permuteSums[0] = matrix[initialPermute[0]][initialPermute[1]];
			for (int i = 1; i < permuteSums.length - 1; i++) {
				comparePermute[i] = initialPermute[i];
				permuteSums[i] = permuteSums[i - 1] + matrix[initialPermute[i]][initialPermute[i + 1]];
				if (permuteSums[i] > lowEstimate) {
					this.insertionSort(initialPermute, i + 1);
					return;
				}
			}
		} else {
			for (int i = compareSentinel; i < initialPermute.length - 1; i++) {
				comparePermute[i] = initialPermute[i];
				permuteSums[i] = permuteSums[i - 1] + matrix[initialPermute[i]][initialPermute[i + 1]];
				if (permuteSums[i] > lowEstimate) {
					this.insertionSort(initialPermute, i + 1);
					return;
				}
			}
		}

		permuteSums[permuteSums.length - 1] = permuteSums[permuteSums.length - 2]
				+ matrix[initialPermute[initialPermute.length - 1]][initialPermute[0]];
		if (permuteSums[permuteSums.length - 1] < lowEstimate) {
			bestArray = initialPermute.clone();
			lowEstimate = permuteSums[permuteSums.length - 1];
		}

		return;
	}

	private String MatrixLineToString(int[] matrixJumps) {
		String alteredMatrixLine = "";
		String lineValues = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789)!@#$%^&*(";

		for (int i = 0; i < matrixJumps.length; i++) {
			alteredMatrixLine += lineValues.substring(matrixJumps[i], matrixJumps[i] + 1) + " ";
		}
		alteredMatrixLine += alteredMatrixLine.substring(0, 1);

		return alteredMatrixLine;
	}

	private static int[][] StringToIntMatrix(String matrixString) {
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

	private static String fileReadIn() throws FileNotFoundException {
		Scanner inputGetter = new Scanner(System.in);
		Scanner sc;
		String fileName = "", inputArrayString = "";
		File file;

		System.out.print("Please enter the name of the file in which the array is stored: ");
		fileName = inputGetter.nextLine();
		if (fileName.equalsIgnoreCase("")) {
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

	private int[] getLexes(int[] currentPermute) {
		currentPermute = this.next_permutation(currentPermute);
		return currentPermute;
	}

	private int[] next_permutation(int[] array) {
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

		compareSentinel = i-1;
		this.swap(array, i++, j);

		for (j = array.length - 1; j > i; i++, j--) {
			this.swap(array, i, j);
		}
		return array;
	}

	private void swap(int[] array, int x, int y) {
		array[x] ^= array[y];
		array[y] ^= array[x];
		array[x] ^= array[y];
	}

	private int[] insertionSort(int[] permute, int position) {
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
		return permute;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}
