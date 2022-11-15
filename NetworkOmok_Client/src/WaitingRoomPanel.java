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
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class WaitingRoomPanel extends JPanel {
	public String userName;
	private OmokPanel omokPanel;
	private ChatPanel chatPanel;
	private WaitingRoomPanel waitingRoomPanel;
	private Container container;
	private CardLayout cardLayout;
	GamePanel gamePanel;
	
	private Socket socket; // 연결 소켓
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	
	public WaitingRoomPanel(Container container, String userName, String ip_addr, String port_no) {
		System.out.println(userName + " " + ip_addr + " " + port_no);
		waitingRoomPanel = this;
		this.userName = userName;
		this.container = container;
		this.cardLayout = (CardLayout) container.getLayout();
		
		try {
			socket = new Socket(ip_addr, Integer.parseInt(port_no)); // socket 생성
			
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(socket.getInputStream());
			
			// 로그인 메시지 전송
			ChatMsg loginMsg = new ChatMsg(userName, "100", "login");
			sendObject(loginMsg);
			
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
				gamePanel = new GamePanel(container, waitingRoomPanel);
				container.add(gamePanel, "gamePanel");
				cardLayout.show(container, "gamePanel");
				//버튼 누르면 방 접속처럼 작동하기 위한 임시 코드
				ChatMsg obj = new ChatMsg(userName, "201", "asdf");
				obj.roomId = "asdf";
				sendObject(obj);
			}
		});
		this.add(tempBtn);
	}
	
	/* -------------------- setter -------------------- */
	public void setOmokPanel(OmokPanel omokPanel) {
		this.omokPanel = omokPanel;
	}
	public void setChatPanel(ChatPanel chatPanel) {
		this.chatPanel = chatPanel;
	}
	
	/* -------------------- Send Method -------------------- */
	// Server에게 network로 Object 전송
	public void sendObject(Object obj) {
		try {
			oos.writeObject(obj);
			if(obj instanceof ChatMsg) {
				ChatMsg temp = (ChatMsg) obj;
				System.out.println(temp.UserName + " " + temp.code + " " + temp.data + " 전송");
			}
		} catch (Exception e) {
			System.out.println("sendObject error");
		}
	}
	
	// Server에게 400 채팅 메시지 전송
	public void sendChatMessage(String message) {
		try {
			ChatMsg chatMsg = new ChatMsg(userName, "400", message);
			oos.writeObject(chatMsg);
			System.out.println(userName + " 400 " + message + " 전송");
		} catch (Exception e) {
			System.out.println("sendChatMessage error");
		}
	}
	
	class ListenNetwork extends Thread {
		public void run() {
			// 서버로부터 메시지를 받는다.
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
					
					/* --------------- Code --------------- */
					switch (chatMsg.code) {
					// 무르기 요청
					case "302":
						int response = JOptionPane.showConfirmDialog(null, "무르기를 허용하시겠습니까?", "무르기", JOptionPane.YES_NO_OPTION);
						if (response == JOptionPane.YES_OPTION) { // 무르기 허용
							chatMsg = new ChatMsg(userName, "303", "YES");
							sendObject(chatMsg);
						} else { // 무르기 거절
							chatMsg = new ChatMsg(userName, "304", "NO");
							sendObject(chatMsg);
						}
						break;
					// 무르기 허용
					case "303":
						// 전에 놓은 바둑돌 취소하기
						break;
					// 채팅 메시지
					case "400":
						chatPanel.appendChatMessageLeft(chatMsg.data);
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
