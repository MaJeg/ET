package test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class TestSendCommand {
	public static void main(String[] args){
		String bmlCommand="<bml id=\"track1\"><marc:fork id=\"track1_fork_1\"><speech id=\"hello0\" marc:volume=\"0.5024366977011296\" marc:articulate=\"1.0\" "
				+ "marc:file=\"C:\\Users\\jegou\\Documents\\modele_tdep\\TT_NARECASlang\\resource\\chunks\\128_1.wav\" /></marc:fork></bml>";
		
		byte[] commandBytes = bmlCommand.getBytes();
		DatagramSocket MARCSender=null;
		try {
			MARCSender = new DatagramSocket();
			DatagramPacket dp = new DatagramPacket(commandBytes, commandBytes.length,InetAddress.getLocalHost(),4010);
			MARCSender.send(dp);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		Thread recv = new ReceiveThread(4041);
		
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
