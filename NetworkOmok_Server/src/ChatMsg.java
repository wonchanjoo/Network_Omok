import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.Vector;


public class ChatMsg implements Serializable {
	private static final long serialVersionUID = 1L;
	public final int black = 0;
	public final int white = 1;
	public final int watch = 2;
	
	public String code;
	public String UserName;
	public String data;
	public Point point; // 바둑돌 놓을 때 사용
	//public boolean isBlack;
	public int role;
	
	public long roomId;
	public String roomName;
	public String password;
	public int peopleCount;
	

	public ChatMsg(String UserName, String code, String msg) {
		this.code = code;
		this.UserName = UserName;
		this.data = msg;
	}
}