package edu.mansfield.algorithms.jsch;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;


public class JschConnector {
	public String userName;
	public String password;
	public String hostIP;
	int port;

	public JschConnector(String userName, String password, String hostIP){
		this.userName = userName;
		this.password = password;
		this.hostIP = hostIP;
		this.port = 22;		
	}
/*
 * Thanks to stackOverflow user World for explanation on how to establish an ssh connection with JSch.
 */
	public ChannelSftp makeConnection(){
		JSch jsch = new JSch();
		try {
			Session session = jsch.getSession(userName, hostIP, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            System.out.println("Establishing Connection...");
            session.connect();
            System.out.println("Connection established.");
            System.out.println("Crating SFTP Channel.");
            ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            System.out.println("SFTP Channel created.");
            
            return sftpChannel;
            
		} catch (JSchException e) {
			System.out.println("Error in makeConnection.");
			e.printStackTrace();
		}
		
		
		
		return null;
		
	}
}
