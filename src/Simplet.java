/**
 *
 */
//package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * @author Roderic Moitie
 *
 */
public class Simplet {
	/** socket to communicate with the server */
	private Socket mySocket;
	/** stream used to send messages */
	private PrintStream writer;
	/** stream used to read messages */
	private BufferedReader reader;

	/**
	 * Initialize the client: connect to some server
	 * @param serverName
	 * @param serverPort
	 */
	public Simplet(String serverName, int serverPort) {
		try {
			// ouvre la socket
			mySocket = new Socket(serverName, serverPort);
			// initialise les flots en lecture et ecriture
			writer = new PrintStream(mySocket.getOutputStream());
			reader = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * first example: try to send hello to the server
	 */
	public void sendMessage(String message) {
		writer.println(message);
	}

	/**
	 * read a message from the server
	 * @return
	 */
	public String getMessage() {
		String res = null;
		try {
			res = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	/**
	 * Try to understand the message
	 * then do something with the message
	 * @param message
	 */
	public void handleMessage(String message) {
		// ne pas traiter le messages vides
		if (message != null && message.length()>0) {
			/* StringTokenizer est une classe tres pratique de java.util (voir doc)
			 * elle permet de traiter un message mot par mot
			 */
			StringTokenizer token = new StringTokenizer(message);
			// premier mot : type de message
			String ordre = token.nextToken();
			// tester si ordre vaut DEPART (test d'egalite sans prendre en compte la casse)
			if (ordre.equalsIgnoreCase("id")) {
				System.out.println ("------------------------------------\n");
				System.out.println ("Information returned by the game server after initialisation "+
									"(2 players connected to the game server)");
				// current player's ID
				int curPlayerId = Integer.parseInt(token.nextToken());
				// number of players
				int nbPlayers = Integer.parseInt(getMessage())+1;
				System.out.println("Player's ID is "+curPlayerId);
				for (int i=0; i< nbPlayers; i++) {
					String msg = getMessage();
					System.out.println(msg);
				}
				int nbPlanet = Integer.parseInt(getMessage());
				for (int i=0; i< nbPlanet; i++) {
					String msg = getMessage();
					System.out.println("Planet"+(i+1)+" is defined by "+msg);
				}
				System.out.println ("------------------------------------\n");
			}
		}

		sendMessage("fin_tour");
	}

	public void doConnect() {
		String message = "Hello Simplet";
		sendMessage(message);
		// Getting and printing answer, should be like  : ID n
		String answer = getMessage();
		System.out.println ("Answer from server is : "+answer);

		// ... and process it
		handleMessage(answer);
	}

	public void beStupid() {
		String str=getMessage();
		System.out.println(str);
		if(str.equalsIgnoreCase("FIN_MODIFICATIONS")) {
			sendMessage("fin_tour");
		}
	}
	
	public static void main(String[] args) {
		// creation du client
		// le serveur doit etre lance sur la meme machine avant le client
		Simplet client = new Simplet("localhost", 31331);
		client.doConnect();
		while((true==true)==(true==true)) {
			client.beStupid();
		}
	}
}
