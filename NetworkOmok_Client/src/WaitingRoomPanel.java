import java.awt.CardLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JPanel;

public class WaitingRoomPanel extends JPanel {
	private Container container;
	private CardLayout cardLayout;
	GamePanel gamePanel;
	
	private Socket socket; // 연결 소켓
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	
	public WaitingRoomPanel(Container container, String userName, String ip_addr, String port_no) {
		this.container = container;
		this.cardLayout = (CardLayout) container.getLayout();
		
		// socket 생성
		try {
			socket = new Socket(ip_addr, Integer.parseInt(port_no));
			
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(socket.getInputStream());
			
			// 로그인 메시지 전송
			ChatMsg msg = new ChatMsg(userName, "100", "login");
			sendObject(msg);
			
			// 서버에게 메시지를 계속 받는 스레드 생성하고 실행
			ListenNetwork net = new ListenNetwork();
			net.start();
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
		
		// 일단 버튼 누르면 gamePanel로 넘어가도록
		JButton tempBtn = new JButton("start Game");
		tempBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				gamePanel = new GamePanel(container);
				container.add(gamePanel, "gamePanel");
				cardLayout.show(container, "gamePanel");
			}
		});
		this.add(tempBtn);
	}
	
	// Server에게 network로 Object 전송
	public void sendObject(Object obj) {
		try {
			oos.writeObject(obj);
		} catch (Exception e) {
	
		}
	}
	
	class ListenNetwork extends Thread {
		public void run() {
			while(true) {
				try {
					Object obj = null;
					String msg = null;
					ChatMsg chatMsg;
					try {
						obj = ois.readObject();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						break;
					}
					
					if(obj == null)
						break;
					
					if(obj instanceof ChatMsg) {
						chatMsg = (ChatMsg) obj;
						msg = String.format("[%s]\n%s", chatMsg.UserName,chatMsg.data);
					} else {
						continue;
					}
					
					// code
					switch (chatMsg.code) {
					case "200": 
						break;
					}
				} catch (IOException e) {
					try {
						ois.close();
						oos.close();
						socket.close();

						break;
					} catch (Exception ee) {
						break;
					}
				} // catch끝
			} // while 끝
		} // run 끝
	}
	
	
}
