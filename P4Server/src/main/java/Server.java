import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;

public class Server {
	
	int port;
	int gameTurns, restartCounter;
	int count = 1;	
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	TheServer server;
	private Consumer<Serializable> callback;
	
	
	Server(int port, Consumer<Serializable> call){
		this.port = port;
		callback = call;
		server = new TheServer();
		server.start();
	}
	
	
	public class TheServer extends Thread{
		public void run() {
			try(ServerSocket mysocket = new ServerSocket(port);){
			    System.out.println("Server is running on port: " + port);
			    callback.accept("Server is running on port: " + port);
			    while(true) {
					ClientThread c = new ClientThread(mysocket.accept(), count);
					callback.accept("client has connected to server: " + "client #" + count);
					clients.add(c);
					System.out.println("Server is connecting with " + count + " players");
					callback.accept("Server is connecting with " + count + " players");
					c.start();
					count++;
			    }
			}//end of try
			catch(Exception e) {
				callback.accept("Server socket did not launch");
			}
		}//end of run
	}
	
	class ClientThread extends Thread{
		
		Socket connection;
		int count;
		ObjectInputStream in;
		ObjectOutputStream out;
		
		ClientThread(Socket s, int count){
			this.connection = s;
			this.count = count;	
		}
		
		public void run(){
			try {
				in = new ObjectInputStream(connection.getInputStream());
				out = new ObjectOutputStream(connection.getOutputStream());
				connection.setTcpNoDelay(true);	
			}
			catch(Exception e) {
				System.out.println("Streams in and out are not open");
			}
			
			//send to new connect client
			sendToClient(count, "You are player " + count);
			
			//server receiving/listening from client
			while(true) {
				try {
					GameInfo gameInfoTemp = (GameInfo) in.readObject();
					displayeReceived(gameInfoTemp);
					if(gameInfoTemp.message != null) {
						if(gameInfoTemp.message.compareTo("Foods") == 0) {
							callback.accept("Player" + gameInfoTemp.playerID + " choose category: " + gameInfoTemp.message);
							sendToClient(count, "Your word length is 4");
						}
					}
					else if(gameInfoTemp.letter != '\u0000') {
						System.out.println("Player" + gameInfoTemp.playerID + " guess letter: " + gameInfoTemp.letter);
						callback.accept("Player" + gameInfoTemp.playerID + " guess letter: " + gameInfoTemp.letter);
					}
				}
			    catch(Exception e) {
			    	callback.accept("OOOOPPs...Something wrong with the socket from client: " + count + "....closing down!");
			    	updateMsgClients("Client #"+count+" has left the server!");
			    	clients.remove(this);
			    	break;
			    }
			}//end of while
		}
	}//end of client thread
	
	public void updateMsgClients(String message) {
		for(int i = 0; i < clients.size(); i++) {
			ClientThread t = clients.get(i);
			try {
				t.out.writeObject(message);
			}
			catch(Exception e) {
				System.out.println("Clients has left");
			}
		}
	}
	
	public void sendToClient(int clientID, String msg) {
		ClientThread t = clients.get(clientID - 1);
		GameInfo gameInfo = new GameInfo();
		gameInfo.message = msg;
		try {
			t.out.writeObject(gameInfo);
		}
		catch(Exception e) {
			System.out.println("updata clientID: " + clientID + " error");
		}
	}
	
	private void displayeReceived(GameInfo gi) {
		System.out.println("Player ID: " + gi.playerID);
		System.out.println("Player message: " + gi.message);
		System.out.println("Player guess letter: " + gi.letter);
	}
	
//	private void updateMorraInfoClients(MorraInfo morraInfo) {
//		for(int i = 0; i < clients.size(); i++) {
//			ClientThread t = clients.get(i);
//			try {
//				t.out.writeObject(morraInfo);
//			}
//			catch(Exception e) {
//				System.out.println("updata clients error");
//			}
//		}
//	}
//	
//	
//	private void callbackOnServer(MorraInfo morraInfo) {
//		callback.accept("Player " + morraInfo.playerID + " Choice: " + morraInfo.playerChoice);
//		callback.accept("Player " + morraInfo.playerID + " Guess: " + morraInfo.playerGuess);
//	}
//	
//	private void updateGameResult2(MorraInfo morraInfo1, MorraInfo morraInfo2) {
//		updateMsgClients("-----Round Result-----");
//		if(getGameResult(morraInfo1, morraInfo2) == 0){
//			updateMsgClients("Game Draw !");
//		}
//		else {
//			for (int i = 0; i < clients.size(); i++) {
//				if (i + 1 == getGameResult(morraInfo1, morraInfo2)) {
//					sendToClient(i + 1, "You Win");
//				}
//				else {
//					sendToClient(i + 1, "You Lose");
//				}
//			}
//		}
//		updateMorraInfoClients(morraInfo1);
//		updateMorraInfoClients(morraInfo2);
//	}
//	
//	public void updateGameResult(MorraInfo morraInfo1, MorraInfo morraInfo2) {
//		updateMsgClients("-----Round Result-----");
//		int sumChoices = morraInfo1.playerChoice + morraInfo2.playerChoice;
//		if(morraInfo1.playerGuess == sumChoices && morraInfo2.playerGuess == sumChoices) {
//			//They all guess correct
//			updateMsgClients("Game Draw");
//			callback.accept("Game Draw");
//		}
//		else if(morraInfo1.playerGuess == sumChoices) {
//			//Player1 guess correct
//			morraInfo1.point++;
//			sendToClient(morraInfo1.playerID, "You Win");
//			sendToClient(morraInfo2.playerID, "You Lose");
//			callback.accept("Player: " + morraInfo1.playerID + " Win");
//			callback.accept("Player: " + morraInfo2.playerID + " Lose");
//		}
//		else if(morraInfo2.playerGuess == sumChoices) {
//			//Player2 guess correct
//			morraInfo2.point++;
//			sendToClient(morraInfo2.playerID, "You Win");
//			sendToClient(morraInfo1.playerID, "You Lose");
//			callback.accept("Player: " + morraInfo1.playerID + " Lose");
//			callback.accept("Player: " + morraInfo2.playerID + " Win");
//		}
//		else {
//			//No player guess correct
//			updateMsgClients("Game Draw !");
//			callback.accept("Game Draw !");
//		}
//		callback.accept("Player: " + morraInfo1.playerID + " point: " + morraInfo1.point);
//		callback.accept("Player: " + morraInfo2.playerID + " point: " + morraInfo2.point);
//		updateMorraInfoClients(morraInfo1);
//		updateMorraInfoClients(morraInfo2);
//	}
//	
//	
//	public int getGameResult(MorraInfo morraInfo1, MorraInfo morraInfo2) {
//		int sumChoices = morraInfo1.playerChoice + morraInfo2.playerChoice;
//		if(morraInfo1.playerGuess == sumChoices && morraInfo2.playerGuess == sumChoices) {
//			//They all guess correct
//			return 0;
//		}
//		else if(morraInfo1.playerGuess == sumChoices) {
//			//Player1 guess correct
//			morraInfo1.point++;
//			return morraInfo1.playerID;
//		}
//		else if(morraInfo2.playerGuess == sumChoices) {
//			//Player2 guess correct
//			morraInfo2.point++;
//			return morraInfo2.playerID;
//		}
//		else {
//			//No player guess correct
//			return 0;
//		}
//	}
}