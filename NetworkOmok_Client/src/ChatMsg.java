import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.Vector;


public class ChatMsg implements Serializable {
	private static final long serialVersionUID = 1L;
	public String code; // 100:로그인, 400:로그아웃, 200:채팅메시지, 300:Image, 500: Mouse Event
	public String UserName;
	public String data;
	public MouseEvent mouse_e;
	public String roomId;
	public Vector player = new Vector();

	public ChatMsg(String UserName, String code, String msg) {
		this.code = code;
		this.UserName = UserName;
		this.data = msg;
	}
}