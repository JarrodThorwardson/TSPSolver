package edu.mansfield.algorithms.jsch;

public class JschTest {

	public static void main(String[] args) {
		JschConnector jsch = new JschConnector("userName", "Password", "TestIP");
		jsch.makeConnection();
	}

}
