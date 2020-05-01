import java.io.Serializable;
import java.util.ArrayList;

public class GameInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	int playerID;
	String message;
	char letter;
	ArrayList<Integer> positions;	//multiple matches in String, such "letter"
	
	int chance;  // how many times you can get wrong, up to 6
		
}