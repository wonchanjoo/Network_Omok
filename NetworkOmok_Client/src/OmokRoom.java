import java.util.Vector;

public class OmokRoom {
	public String roomName;
	public int roomId;
	public int peopleCount = 0;
	public Vector<WaitingRoomPanel> userList = new Vector<>();
	
	public OmokRoom(int roomId) {
		this.roomId = roomId;
	}
}
