import java.awt.CardLayout;
import java.awt.Container;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.JPanel;

public class GamePanel extends JPanel {
	private Container container;
	private CardLayout cardLayout;
	
	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	
	public GamePanel(Container container, String userName, String ip_addr, String port_no) {
		this.container = container;
		this.cardLayout = (CardLayout) container.getLayout();
		
		// socket 생성
		try {
			socket = new Socket(ip_addr, Integer.parseInt(port_no));
			
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(socket.getInputStream());

		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
	}
}
