import java.util.Vector;

public class OmokRoom {
	public String roomName;
	public int roomId;
	public int peopleCount = 0;
	public Vector<String> player = new Vector<>();
	public Vector<String> viewer = new Vector<>();
	
	public OmokRoom(int roomId, String creater) {
		this.roomId = roomId;
		this.player.add(creater);
	}
}
