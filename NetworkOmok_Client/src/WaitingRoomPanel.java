import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;
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
				
				//버튼 누르면 방 접속처럼 작동하기 위한 임시 코드, 방 접속 201 전송
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
	
	// Server에게 301 마우스 좌표 전송
	public void sendMousePoint() {
		// OmokPanel의 보여주기용 바둑돌 떼어내기
		if(omokPanel.getIsBlack()) // 흑돌이면
			omokPanel.remove(omokPanel.bStone); // 흑돌 떼어내기
		else //  백돌이면
			omokPanel.remove(omokPanel.wStone); // 백돌 떼어내기
		omokPanel.repaint();
		
		// OmokPanel의 멤버 변수에서 좌표를 읽어와 전송해야 된다. 
		try {
			ChatMsg chatMsg = new ChatMsg(userName, "301", "point");
			chatMsg.point = omokPanel.point;
			chatMsg.isBlack = omokPanel.getIsBlack();
			oos.writeObject(chatMsg);
			omokPanel.setStatus(false);
			System.out.println(userName + " 301 " + chatMsg.point + " 전송");
		} catch (Exception e) {
			System.out.println("sendMousePoint error");
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
					// 백돌인지 흑돌인지 data에 들어있다.
					case "201":
						omokPanel.setIsBlack(chatMsg.isBlack);
						break;
					// 게임 시작
					case "300":
						// 흑돌 백돌 이름 가져와서 화면에 표시
						String userNames = chatMsg.data;
						StringTokenizer st = new StringTokenizer(userNames);
						omokPanel.whitePlayerName.setText(st.nextToken());
						omokPanel.blackPlayerName.setText(st.nextToken());
						
						if(omokPanel.getIsBlack()) // 흑돌인 경우
							omokPanel.setStatus(true); // status를 true로 설정해 바둑돌을 놓을 수 있는 상태로 변경한다.
						
						if(!omokPanel.getIsBlack()) // 백돌인 경우
							chatPanel.putBtn.setEnabled(false); // 착수 버튼 비활성화
						break;
					// 서버로부터 계산된 마우스 이벤트
					case "301":
						omokPanel.putStone(chatMsg.point.x, chatMsg.point.y, chatMsg.isBlack);
						if(chatMsg.isBlack == omokPanel.getIsBlack()) // 내가 보낸 좌표면
							omokPanel.setStatus(false); // 내 차례가 아니므로 false
						else { // 상대방이 보낸 좌표면
							omokPanel.setStatus(true); // 내 차례므로 true
							chatPanel.putBtn.setEnabled(true); // 착수 버튼 활성화
						}
						break;
					// 착수 거부
					case "302":
						JOptionPane.showMessageDialog(null, "해당 위치에 착수할 수 없습니다.", "Message", JOptionPane.ERROR_MESSAGE);
						chatPanel.putBtn.setEnabled(true);
						break;
					// 무르기 요청
					case "310":
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
					case "311":
						// 전에 놓은 바둑돌 취소하기
						break;
					// 게임 승리
					case "321":
						break;
					// 게임 패배
					case "322":
						break;
					// 채팅 메시지
					case "400":
						chatPanel.appendChatMessageLeft(chatMsg.data);
						break;
					// 게임 초대
					case "500":
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
