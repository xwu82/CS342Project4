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
    
    private ArrayList<String>  animals = new ArrayList<String>() {
    	{
    		add("kangaroo");
    		add("trilobita");
    		add("squirrel");
    	}
    };
    
    private ArrayList<String> states = new ArrayList<String>() {
    	{
    		add("oklahoma");
    		add("illinois");
    		add("nevada");
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
		boolean foodpass;
		boolean animalpass;
		boolean statespass;
		int foodfail;
		int animalfail;
		int statesfail;
		
		ClientThread(Socket s, int count){
			this.connection = s;
			this.count = count;	
			foodpass = false;
			animalpass = false;
			statespass = false;
			foodfail = 0;
			animalfail = 0;
			statesfail = 0;
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
					        System.out.println("test: " + gameInfoTemp.chance);
							callback.accept("Player" + gameInfoTemp.playerID + " choose category: " + gameInfoTemp.message
									+ "; Word is " + word);
							callback.accept("Player" + gameInfoTemp.playerID + " have " + gameInfoTemp.chance  + " chance.");
							sendMsgToClient(playerID, "Your word length is " + word.length());
							sendMsgToClient(playerID, "You have " + gameInfoTemp.chance  + " chance");
							answers.set(playerID - 1, word);
						}
						
						else if (gameInfoTemp.message.compareTo("Animals") == 0) {
							int playerID = gameInfoTemp.playerID;
							Random rand = new Random();
							String word = animals.get(rand.nextInt(animals.size()));
							callback.accept("Player" + gameInfoTemp.playerID + " choose category: " + gameInfoTemp.message
									+ "; Word is " + word);
							callback.accept("Player" + gameInfoTemp.playerID + " have " +  gameInfoTemp.chance  + " chance.");
							sendMsgToClient(playerID, "Your word length is " + word.length());
							sendMsgToClient(playerID, "You have " + gameInfoTemp.chance  + " chance");
							answers.set(playerID - 1, word);
						}
						
						else if (gameInfoTemp.message.compareTo("U.S. States") == 0) {
							int playerID = gameInfoTemp.playerID;
							Random rand = new Random();
							String word = states.get(rand.nextInt(states.size()));
							callback.accept("Player" + gameInfoTemp.playerID + " choose category: " + gameInfoTemp.message
									+ "; Word is " + word);
							callback.accept("Player" + gameInfoTemp.playerID + " have " +  gameInfoTemp.chance  + " chance.");
							sendMsgToClient(playerID, "Your word length is " + word.length());
							sendMsgToClient(playerID, "You have " + gameInfoTemp.chance  + " chance");
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
		if(answers.get(playerID - 1).indexOf(letter) != -1) {
			sendResultToClient(playerID, "You have correctly guessed: " + letter, gameInfo, true);
			callback.accept("right guess");
		}
		//wrong guess
		else {
			if ((gameInfo.chance - 1) > 0) {
				sendResultToClient(playerID, "Better luck next time: " + letter + " \nYou only have " + (gameInfo.chance -1) + " chance.", gameInfo, false);
				callback.accept("wrong guess: only have " + (gameInfo.chance -1) + " chance.");
			}
			else if ((gameInfo.chance - 1) == 0) {
				sendResultToClient(playerID, "Better luck next time: " + letter + " \nYou have no chance, Game Over", gameInfo, false);
				callback.accept("Player" + playerID + " lost game.");
			}
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
		//update chance
		if (correctness == true) {
			newgameInfo.chance = gameInfo.chance;
		}
		else if (correctness == false) {
			newgameInfo.chance = gameInfo.chance - 1;
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