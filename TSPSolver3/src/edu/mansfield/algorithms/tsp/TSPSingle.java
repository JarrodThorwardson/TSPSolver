package edu.mansfield.algorithms.tsp;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class TSPSingle implements Runnable{
	
	private int[][] matrix;
	private int[] initialPermute, bestArray;
	private int watchPerson, lowEstimate, sentinel;

	public TSPSingle(int[][] inputMatrix, int[] inputPermute, int indexSentinel, int estimate) {
		matrix = inputMatrix.clone();
		initialPermute = inputPermute.clone();
		bestArray = new int[inputPermute.length];
		watchPerson = indexSentinel;
		lowEstimate = estimate;
		sentinel = inputPermute[indexSentinel];
	}
	
	public int[] getInitialPermute() {
		return initialPermute;
	}

	public void setInitialPermute(int[] initialPermute1) {
		initialPermute = initialPermute1;
	}

	public static void main(String[] args) throws FileNotFoundException {
		TSPSingle tsp;
		int watchPersonToo = 0;
		int firstEstimate;
		long time, runTime;
		int[][] start;
		int[] startHere;
		int[] shortest;
		String matrixString = fileReadIn();

		start = StringToIntMatrix(matrixString);
		startHere = new int[start[0].length];
		firstEstimate = 90;
		for (int i = 0; i < startHere.length; i++) {
			startHere[i] = i;
		}

		tsp = new TSPSingle(start, startHere, watchPersonToo, firstEstimate);
		time = System.currentTimeMillis();
		shortest = tsp.solve();
		runTime = System.currentTimeMillis() - time;

		System.out.println("Best path:\t" + tsp.MatrixLineToString(shortest) + "\tBest Distance:\t"
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
	
	public int[] solve() {
		this.permuteFinding();
		return bestArray;
	}
	
	private void permuteFinding() {

		while (initialPermute[watchPerson] == sentinel) {
			this.permuteBranchBound();
			this.getLexes(initialPermute);
		}
	}
	
	private int[] permuteBranchBound() {
		/*Opportunity for linear/dynamic programming in this method.*/
		
		int num = 0;

		for (int i = 0; i < initialPermute.length - 1; i++) {
			num += matrix[initialPermute[i]][initialPermute[i + 1]];
			if (num > lowEstimate) {
				this.insertionSort(initialPermute, i+1);
				return initialPermute;
			}
		}

		num += matrix[initialPermute[initialPermute.length - 1]][initialPermute[0]];
		if (num < lowEstimate) {
			bestArray = initialPermute.clone();
			lowEstimate = num;
		}
		
		return initialPermute;
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
