import java.util.Vector;

public class OmokRoom {
	public String roomName;
	public int roomId;
	public int currentPeoples = 0;
	public String[] player = new String[2];
	public Vector<String> viewer = new Vector<>();
	
	public OmokRoom(int roomId, String[] player) {
		this.roomId = roomId;
		this.player = player;
	}
}
