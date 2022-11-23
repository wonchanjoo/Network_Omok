import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.Vector;


public class ChatMsg implements Serializable {
	private static final long serialVersionUID = 1L;
	public String code;
	public String UserName;
	public String data;
	public Point point; // 바둑돌 놓을 때 사용
	public boolean isBlack;
	
	public int roonId;
	public String roomName;
	public String password;
	public int peopleCount;
	

	public ChatMsg(String UserName, String code, String msg) {
		this.code = code;
		this.UserName = UserName;
		this.data = msg;
	}
}