import java.util.Vector;

public class OmokRoom {
	public String roomName;
	public long roomId;
	public int peopleCount = 0;
	public Vector<WaitingRoomPanel> userList = new Vector<>();
	

	public OmokRoom(long roomId) {
		this.roomId = roomId;
	}
}
