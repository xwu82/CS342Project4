import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Consumer;

public class Server {
	
	private ArrayList<String> foods = new ArrayList<String>() { 
        { 
            add("hamburger"); 
            add("chips"); 
            add("pizza");
            //add("french fries");	//TODO:
            //add("coca-cola");	//TODO:
        } 
    };
	
	int port;
	int count = 1;	
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	ArrayList<String> answers = new ArrayList<String>();
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
					answers.add("dummy");	//add dummy for keep the size same as clients
					answers.ensureCapacity(clients.size());	//increasing answers size with clients size 
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
			sendMsgToClient(count, "You are player " + count);
			
			//server receiving/listening from client
			while(true) {
				try {
					GameInfo gameInfoTemp = (GameInfo) in.readObject();
					displayeReceived(gameInfoTemp);
					//server received category name, send client random item from this category
					if(gameInfoTemp.message != null) {
						if(gameInfoTemp.message.compareTo("Foods") == 0) {
							int playerID = gameInfoTemp.playerID;
							Random rand = new Random(); 
					        String word = foods.get(rand.nextInt(foods.size())); 
							callback.accept("Player" + gameInfoTemp.playerID + " choose category: " + gameInfoTemp.message
									+ "; Word is " + word);
							sendMsgToClient(playerID, "Your word length is " + word.length());
							answers.set(playerID - 1, word);
						}
					}
					//server received a letter guessed from client
					else if(gameInfoTemp.letter != '\u0000') {
						char letter = gameInfoTemp.letter;
						System.out.println("Player" + gameInfoTemp.playerID + " guess letter: " + letter);
						callback.accept("Player" + gameInfoTemp.playerID + " guess letter: " + letter);
						parseLetter(gameInfoTemp);
					}
				}
			    catch(Exception e) {
			    	callback.accept("OOOOPPs...Something wrong with the socket from client: " + count + "....closing down!");
			    	callback.accept("Client #"+count+" has left the server!");
			    	//TODO: causing bug when some client finish playing and exit
			    	// 		game, should only close socket, but may cause storage 
			    	//		overload, since clients always increasing
			    	//clients.remove(this);
			    	e.printStackTrace(); 
			    	break;
			    }
			}//end of while
		}
	}//end of client thread
	
	private void parseLetter(GameInfo gameInfo) {
		int playerID = gameInfo.playerID;
		char letter = gameInfo.letter;
		//correct guess
		if(answers.get(playerID - 1).indexOf(letter) != -1)
			sendResultToClient(playerID, "You have correctly guessed: " + letter, gameInfo, true);
		//wrong guess
		else {
			sendResultToClient(playerID, "Better luck next time: " + letter, gameInfo, false);
		}
	}
	
	public void sendMsgToClient(int clientID, String message) {
		ClientThread t = clients.get(clientID - 1);
		GameInfo gameInfo = new GameInfo();
		gameInfo.message = message;
		try {
			t.out.writeObject(gameInfo);
		}
		catch(Exception e) {
			System.out.println("updata clientID: " + clientID + " error");
		}
	}
	
	//send String 
	public void sendResultToClient(int clientID, String msg, GameInfo gameInfo, boolean correctness) {
//		System.out.println("clientID: " + playerID + ", " + word);
//		System.out.println("answers size: " + answers.size());
		ClientThread t = clients.get(clientID - 1);
		GameInfo newgameInfo = new GameInfo();
		newgameInfo.playerID = gameInfo.playerID;
		newgameInfo.message = msg;
		newgameInfo.letter = gameInfo.letter;
		newgameInfo.positions = new ArrayList<Integer>();
		//update position
		int i = answers.get(gameInfo.playerID - 1).indexOf(gameInfo.letter);
		while(i >= 0) {
		     newgameInfo.positions.add(i);
		     i = answers.get(gameInfo.playerID - 1).indexOf(gameInfo.letter, i + 1);
		}
		//send to client
		try {
			t.out.writeObject(newgameInfo);
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
}