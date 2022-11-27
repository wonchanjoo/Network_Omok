import java.util.Vector;

public class OmokRoom {
	public String roomName;
	public long roomId;
	public int peopleCount = 0;
	public Vector<String> player = new Vector<>();
	public Vector<String> viewer = new Vector<>();
	
	public OmokRoom(long roomId, String creater) {
		this.roomId = roomId;
		player.add(creater);
	}
}
