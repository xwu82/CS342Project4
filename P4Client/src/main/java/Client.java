import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;

import javafx.scene.control.Button;



public class Client extends Thread{
	
	Socket socketClient;
	
	ObjectOutputStream out;
	ObjectInputStream in;
	
	private Consumer<Serializable> callback;
	
	private String ip;
	int port;
	
	Client(String ip, int port, Consumer<Serializable> call){
		this.ip = ip;
		this.port = port;
		callback = call;
	}
	
	public void run() {
		
		//try to make a connection to server
		try {
			socketClient= new Socket(ip, port);
		    out = new ObjectOutputStream(socketClient.getOutputStream());
		    in = new ObjectInputStream(socketClient.getInputStream());
		    socketClient.setTcpNoDelay(true);
		}
		catch(Exception e) {
			System.out.println("Cannot connect to server");
		}
		
		//receive data
		while(true) {
			//try: if server closed
			try {
				GameInfo gameInfoTemp = (GameInfo) in.readObject();
				if(gameInfoTemp.message != null) {	//client received message
					String m = gameInfoTemp.message;
					if (gameInfoTemp.chance == 0) {
						
					}
					callback.accept(gameInfoTemp);
					System.out.println("Client received message: " + m);
				}
				else if(gameInfoTemp.letter != '\u0000' && gameInfoTemp.positions.size() != 0) {
					System.out.println("Client guessed correct");
					callbackGameInfo(gameInfoTemp);
				}
				else {
					System.out.println("GameInfo error");
				}
			}
			catch(Exception e) {
				System.out.println("Server closed");
				// e.printStackTrace();
				break;
			}
		}
	
    }
	
	//send char/letter to server
	public void send(int playerID, int Hchance, char letter) {
		GameInfo newGameInfo = new GameInfo();
		newGameInfo.playerID = playerID;
		newGameInfo.chance = Hchance;
		newGameInfo.letter = letter;
		try {
			out.writeObject(newGameInfo);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//send String/message to server
	public void send(int playerID, int Hchance, String s) {
		GameInfo gameInfo = new GameInfo();
		gameInfo.playerID = playerID;
		gameInfo.chance = Hchance;
		gameInfo.message = s;
		try {
			out.writeObject(gameInfo);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void callbackGameInfo(GameInfo gameInfo) {
		callback.accept("You have correctly guessed in position:" + displayLetterPosition(gameInfo.positions));
	}
	
	private String displayLetterPosition(ArrayList<Integer> positions) {
		String s = null;
		for(Integer i : positions) {
			s = " " + String.valueOf(i);
		}
		return s;
	}
	
	public void closeSocket() {
		try {
			socketClient.close();
		} catch (IOException e) {
			System.out.println("Socket alreadly closed");
		}
	}

}