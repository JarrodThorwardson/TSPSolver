package edu.mansfield.algorithms.tsp.thor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class MatrixGeneration {

	public static void main(String[] args) throws IOException {
		int cities = 20;
		int version = 5;
		int noGo = 100000;
		Random rng = new Random();
		StringBuilder matrixString = new StringBuilder();
		int[][] cityMatrix = new int[cities][cities];
		int mirror = 0;

		BufferedWriter outPut = new BufferedWriter(new FileWriter(cities + ".txt" + "mk" + version));
		
		
		for (int i=0;i<cities; i++){
			for (int j=0;j<cities;j++){
				if (i==j){
					cityMatrix[i][j] = noGo;
				} else{
					mirror =  rng.nextInt(49) + 1;
					cityMatrix[i][j] = mirror;
					cityMatrix[j][i] = mirror;
				}
			}
		}
		
		for (int[] array : cityMatrix){
			for (int distance : array){
				matrixString.append(distance + ",");
			}
			matrixString.append("\n");
		}
		
		outPut.write(matrixString.toString());
		outPut.close();
	}
	
}
