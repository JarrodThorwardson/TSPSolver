package edu.mansfield.algorithms.tsp;

import java.util.Stack;

public class NearestNeighbor2 {

	public int[] newNearestNeighbor(int[][] matrix, int startCity) {
		int[] reply = new int[matrix[0].length];
		boolean[] visited = new boolean[matrix[0].length];
		for (boolean visit : visited) {
			visit = false;
		}
		int cityLowest, indexLowest, currentCity;
		reply[0] = startCity;
		visited[startCity] = true;
		currentCity = startCity;
		indexLowest = 0;

		for (int i = 1; i < reply.length; i++) {
			cityLowest = Integer.MAX_VALUE;
			for (int j = 0; j < matrix[currentCity].length; j++) {
				if ((visited[j] == false)) {
					if (matrix[currentCity][j] < cityLowest) {
						cityLowest = matrix[currentCity][j];
						indexLowest = j;
					}
				}
			}
			visited[indexLowest] = true;
			reply[i] = indexLowest;
			currentCity = indexLowest;
		}

		return reply;
	}

	public int[][] oldNearestNeighborRunArray(int[][] matrix) {
		Stack<Integer> stack = new Stack<Integer>();
		int lowest = Integer.MAX_VALUE, pathLength = 0, lastPlace = 0;
		int min = Integer.MAX_VALUE;
		int arrayLength = matrix[0].length;
		System.out.println("number of nodes: " + arrayLength);
		int[][] bestPathHolder = new int[arrayLength][arrayLength];

		for (int k = 0; k < arrayLength; k++) {
			bestPathHolder[k][0] = k;
			pathLength = 0;
			boolean[] visited = new boolean[arrayLength + 1];
			visited[k] = true;
			stack.push(k);
			int element, destination = 0, i, pathCounter = 1;
			min = Integer.MAX_VALUE;
			boolean minFlag = false;
			// System.out.print(k + "\t");

			while (!stack.isEmpty()) {
				element = (int) stack.peek();
				i = 0;
				min = Integer.MAX_VALUE;
				while (i < arrayLength) {
					if (matrix[element][i] > 1 && visited[i] == false) {
						if (min > matrix[element][i]) {
							min = matrix[element][i];
							destination = i;
							minFlag = true;

						}
					}

					i++;
				}
				if (minFlag) {
					visited[destination] = true;
					bestPathHolder[k][pathCounter] = destination;
					// System.out.println("pathHolder = " +
					// bestpathHolder[pathCounter] + "\tdestination = " + dst);
					stack.push(destination);
					// System.out.print("position: " + dst + ", distance: " +
					// min + "\t");
					// System.out.print(dst + "\t");
					minFlag = false;
					pathLength += min;
					lastPlace = (int) stack.pop();
					stack.push(destination);
					pathCounter++;
					continue;
				}
				stack.pop();
			}
			pathLength += matrix[bestPathHolder[k][0]][lastPlace];
			// System.out.println(bestPathHolder[0] + "\tDistance: " +
			// pathLength);
			if (pathLength <= lowest) {
				lowest = pathLength;
			}

		}
		System.out.println("Nearest Neighbor best run = " + lowest);
		return bestPathHolder;
	}

	public int oldNearestNeighborRun(int[][] matrix) {
		Stack<Integer> stack = new Stack<Integer>();
		int lowest = Integer.MAX_VALUE, pathLength = 0, lastPlace = 0;
		int min = Integer.MAX_VALUE;
		int arrayLength = matrix[0].length;
		System.out.println("number of nodes: " + arrayLength);
		int[] bestPathHolder = new int[arrayLength];

		for (int k = 0; k < arrayLength; k++) {
			bestPathHolder[0] = k;
			pathLength = 0;
			boolean[] visited = new boolean[arrayLength + 1];
			visited[k] = true;
			stack.push(k);
			int element, destination = 0, i, pathCounter = 1;
			min = Integer.MAX_VALUE;
			boolean minFlag = false;
			// System.out.print(k + "\t");

			while (!stack.isEmpty()) {
				element = (int) stack.peek();
				i = 0;
				min = Integer.MAX_VALUE;
				while (i < arrayLength) {
					if (matrix[element][i] > 1 && visited[i] == false) {
						if (min > matrix[element][i]) {
							min = matrix[element][i];
							destination = i;
							minFlag = true;

						}
					}

					i++;
				}
				if (minFlag) {
					visited[destination] = true;
					bestPathHolder[pathCounter] = destination;
					// System.out.println("pathHolder = " +
					// bestpathHolder[pathCounter] + "\tdestination = " + dst);
					stack.push(destination);
					// System.out.print("position: " + dst + ", distance: " +
					// min + "\t");
					// System.out.print(dst + "\t");
					minFlag = false;
					pathLength += min;
					lastPlace = (int) stack.pop();
					stack.push(destination);
					pathCounter++;
					continue;
				}
				stack.pop();
			}
			pathLength += matrix[bestPathHolder[0]][lastPlace];
			// System.out.println(bestPathHolder[0] + "\tDistance: " +
			// pathLength);
			if (pathLength <= lowest) {
				lowest = pathLength;
			}

		}
		System.out.println("Nearest Neighbor best run = " + lowest);
		return lowest;
	}

}
