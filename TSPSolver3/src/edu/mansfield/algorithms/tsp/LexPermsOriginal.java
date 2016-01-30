package edu.mansfield.algorithms.tsp;
import java.util.Scanner;


public class LexPermsOriginal {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int[] array = {};
		int[] expansionArray;
		Scanner sc = new Scanner(System.in);
		String input = "";
		while (true) {
			System.out.print("Please put in the next number of the sequence or 'q' to indicate the end of the sequence: ");
			input = sc.nextLine();
			if (input.equals("q")) {
				break;
			}
			expansionArray = new int[array.length + 1];
			for (int i = 0; i < array.length; i++) {
				expansionArray[i] = array[i];
			}
			expansionArray[expansionArray.length - 1] = Integer
					.parseInt(input);
			array = expansionArray;
		}
		while (true) {
			System.out.println("Do you want to see the next permutation? y/n : ");
			input = sc.nextLine();
			if (input.equalsIgnoreCase("y")) {
				next_permutation(array);
				System.out.println(print(array));
			} else {
				endPermutes();
			}

			sc.close();
		}
	}

	public static String print(int[] array) {
		String str = "";
		for (int tmp : array) {
			str += " " + tmp;
		}
		str += "\n";
		return str;
	}

	public static void next_permutation(int[] array) {
		int i, j;
		for (i = array.length - 2; i >= 0; i--) {
			if (array[i] < array[i + 1])
				break;
		}
		if (i < 0) {
			System.out.println("End");
//			System.exit(0);
			return;
		}

		for (j = array.length - 1; j > i; j--) {
			if (array[j] > array[i])
				break;
		}

		swap(array, i++, j);

		for (j = array.length - 1; j > i; i++, j--) {
			swap(array, i, j);
		}
	}

	public static void swap(int[] array, int x, int y) {
		array[x] ^= array[y];
		array[y] ^= array[x];
		array[x] ^= array[y];
	}

	public static void endPermutes() {
		boolean endPermutations = true;
		while (endPermutations) {
			if (endPermutations) {
				System.out.println("We're done.");
	//			System.exit(0);
			}
		}
	}

}
