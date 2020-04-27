import java.io.Serializable;
import java.util.ArrayList;

public class GameInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	int playerID;
	String message;
	char letter;	//client send to server
	ArrayList<Integer> positions;	//server send to client
}