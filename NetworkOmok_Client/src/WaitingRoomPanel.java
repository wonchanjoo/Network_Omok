import java.awt.CardLayout;
import java.awt.Color;
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

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Font;
import java.awt.Graphics;

public class WaitingRoomPanel extends JPanel {
	public long roomId;
	public String userName;
	private OmokPanel omokPanel;
	private ChatPanel chatPanel;
	private JFrame mainFrame;
	private WaitingRoomPanel waitingRoomPanel;
	private Container container;
	private CardLayout cardLayout;
	GamePanel gamePanel;
	
	public Vector<OmokRoom> omokRooms = new Vector<>();
	public JList<String> roomList;
	public DefaultListModel<String> roomModel; // JList에 보이는 실제 대기실 데이터
	public JList<String> allUserList;
	public DefaultListModel<String> allUserModel;
	private CreateRoomFrame createRoomFrame;
	
	private Socket socket; // 연결 소켓
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	
	public WaitingRoomPanel(Container container, JFrame mainFrame, String userName, String ip_addr, String port_no) {
		waitingRoomPanel = this;
		this.mainFrame = mainFrame;
		this.userName = userName;
		this.container = container;
		this.cardLayout = (CardLayout) container.getLayout();
		this.setSize(900, 650);
		this.setLayout(null);
		
		Color backgroundColor = new Color(233, 211, 180);
		// 방 리스트 생성
		JLabel roomListLabel = new JLabel("방 목록");
		roomListLabel.setFont(new Font("솔뫼 김대건 Medium", Font.PLAIN, 40));
		roomListLabel.setHorizontalAlignment(JLabel.CENTER);
		roomListLabel.setOpaque(true);
		roomListLabel.setBackground(backgroundColor);
		roomListLabel.setBounds(130, 30, 200, 50);
		this.add(roomListLabel);
		
		roomModel = new DefaultListModel<String>();
		roomList = new JList<String>(roomModel);
		roomList.setOpaque(true);
		roomList.setBackground(backgroundColor);
		roomList.setFont(new Font("맑은 고딕", Font.PLAIN, 22));
		roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // 리스트가 하나만 선택될 수 있도록
		roomList.setBounds(40, 105, 400, 470);
		roomList.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); // 리스트 경계선 생성
		roomList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public int getHorizontalAlignment() {
				return CENTER;
			}
		});
		this.add(roomList);
		
		// 접속자 리스트 생성
		JLabel userListLabel = new JLabel("접속자");
		userListLabel.setFont(new Font("솔뫼 김대건 Medium", Font.PLAIN, 40));
		userListLabel.setHorizontalAlignment(JLabel.CENTER);
		userListLabel.setOpaque(true);
		userListLabel.setBackground(backgroundColor);
		userListLabel.setBounds(600, 30, 200, 50);
		this.add(userListLabel);
		
		allUserModel = new DefaultListModel<String>();
		allUserList = new JList<String>(allUserModel);
		allUserList.setOpaque(true);
		allUserList.setBackground(backgroundColor);
		allUserList.setFont(new Font("맑은 고딕", Font.PLAIN, 22));
		allUserList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		allUserList.setBounds(535, 105, 320, 400);
		allUserList.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		this.add(allUserList);
		
		// 방 만들기 버튼 생성
		JButton createRoomBtn = new JButton("방 만들기");
		createRoomBtn.setBounds(550, 520, 130, 50);
		createRoomBtn.addActionListener(new CreateRoomBtnClick());
		this.add(createRoomBtn);
		
		// 방 접속 버튼 생성
		JButton enterRoomBtn = new JButton("방 접속");
		enterRoomBtn.setBounds(710, 520, 130, 50);
		this.add(enterRoomBtn);
		
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
		
		// 방 접속 버튼 누르면
		enterRoomBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int selectedIndex = roomList.getSelectedIndex();
				if(selectedIndex == -1) {
					JOptionPane.showMessageDialog(mainFrame, "방을 선택하세요!", "error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				OmokRoom selectedRoom = omokRooms.get(selectedIndex);
				ChatMsg chatMsg = new ChatMsg(userName, "201", "방 접속");
				chatMsg.roomId = selectedRoom.roomId; // roomId
				sendObject(chatMsg); // roomId와 함께 방 접속 메시지 전송 
			}
		});
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		ImageIcon omokTableImg = new ImageIcon("images\\waitingroom_background.png");
		g.drawImage(omokTableImg.getImage(), 0, 0, this);
	}
	
	class CreateRoomBtnClick implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// 방 정보 입력 받을 JFrame 띄우기
			createRoomFrame = new CreateRoomFrame(waitingRoomPanel);
		}
	}
	
	// 방 만들기 Frame에서 "만들기" 버튼을 누르면 실행되는 함수
	// 방 정보를 받아와서 (인자로 받아온다) 서버에게 방을 만든다고 전송한다. 
	public void createRoom(String roomName, String password, int peopleCount) {
		createRoomFrame.dispose(); // 프레임 닫기
		
		gamePanel = new GamePanel(container, waitingRoomPanel); // GamePanel 생성
		container.add(gamePanel, "gamePanel");
		cardLayout.show(container, "gamePanel"); // GamePanel로 패널 전환
		
		ChatMsg chatMsg = new ChatMsg(userName, "200", "방 만들기");
		chatMsg.UserName = userName;
		chatMsg.roomName = roomName;
		chatMsg.password = password;
		chatMsg.peopleCount = peopleCount;
		sendObject(chatMsg);
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
			if(obj instanceof ChatMsg) {
				oos.writeObject(obj);
			}
		} catch (Exception e) {
			System.out.println("sendObject error");
		}
	}
	
	// Server에게 400 채팅 메시지 전송
	public void sendChatMessage(String message) {
		try {
			ChatMsg chatMsg = new ChatMsg(userName, "400", message);
			chatMsg.roomId = gamePanel.roomId;
			oos.writeObject(chatMsg);
		} catch (Exception e) {
			System.out.println("sendChatMessage error");
		}
	}
	
	// Server에게 301 마우스 좌표 전송
	public void sendMousePoint() {
		// OmokPanel의 보여주기용 바둑돌 떼어내기
		if(omokPanel.role == omokPanel.black) // 흑돌이면
			omokPanel.remove(omokPanel.bStone); // 흑돌 떼어내기
		else //  백돌이면
			omokPanel.remove(omokPanel.wStone); // 백돌 떼어내기
		omokPanel.repaint();
		
		// OmokPanel의 멤버 변수에서 좌표를 읽어와 전송해야 된다. 
		try {
			ChatMsg chatMsg = new ChatMsg(userName, "301", "point");
			chatMsg.point = new Point(omokPanel.oldStone.getX(), omokPanel.oldStone.getY());
			chatMsg.role = omokPanel.role;
			chatMsg.roomId = gamePanel.roomId;
			oos.writeObject(chatMsg);
			omokPanel.setStatus(false);
		} catch (Exception e) {
			System.out.println("sendMousePoint error");
		}
	}
	
	class ListenNetwork extends Thread {
		public ListenNetwork() {
			// 방 목록 달라는 메시지 전송
			ChatMsg roomlist = new ChatMsg(userName, "210", "roomList");
			sendObject(roomlist);
		}
		
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
					} else {
						continue;
					}
					
					/* --------------- Code --------------- */
					switch (chatMsg.code) {
					// 어느 방이 만들어졌을 때 서버로부터 방 정보가 전송된다. 
					case "200":
						long roomId = chatMsg.roomId; // 방 ID
						String roomName = chatMsg.roomName; // 방 이름
						int peopleCount = chatMsg.peopleCount; // 방 인원 수
						
						OmokRoom newRoom = new OmokRoom(roomId); // 새로운 방 만들기
						newRoom.roomName = roomName;
						newRoom.peopleCount = peopleCount;
						omokRooms.add(newRoom); // 방 리스트에 추가
						
						String roomStr = String.format("%10s   %d/%d", chatMsg.roomName, 1, chatMsg.peopleCount);
						roomModel.addElement(roomStr); // 리스트 모델에 추가
						break;
					// 게임 방에 접속
					case "201":
						roomId = chatMsg.roomId;
						System.out.println("201 room Id = " + roomId);
						
						for(int i=0; i<omokRooms.size(); i++) {
							OmokRoom o = omokRooms.get(i);
							if(o.roomId == chatMsg.roomId)
								o.userList.add(waitingRoomPanel); // 해당 방에 나 자신 추가
						}
						
						gamePanel = new GamePanel(container, waitingRoomPanel); // GamePanel 생성
						gamePanel.roomId = chatMsg.roomId; // GamePanel의 roomId 설정
						container.add(gamePanel, "gamePanel");
						
						omokPanel = gamePanel.omokPanel;
						chatPanel = gamePanel.chatPanel;
						omokPanel.role = chatMsg.role;
						
						cardLayout.show(container, "gamePanel"); // GamePanel로 전환
						break;
					// 게임 방에 접속 할 수 없는 경우
					case "202":
						JOptionPane.showMessageDialog(mainFrame, chatMsg.data, "error", JOptionPane.ERROR_MESSAGE);
						break;
					// 서버로부터 전체 방 목록 전송된 경우
					case "210":
						// 서버가 처음부터 방 목록 새로 보내주는 경우 다 지우고 다시 받아야 함
						if(chatMsg.data.equals("init")) {
							omokRooms.removeAllElements();
							roomModel.removeAllElements();
						}
						
						long roomId2 = chatMsg.roomId; // 방 ID
						String roomName2 = chatMsg.roomName; // 방 이름
						int peopleCount2 = chatMsg.peopleCount; // 방 인원 수
						
						OmokRoom newRoom2 = new OmokRoom(roomId2); // 새로운 방 만들기
						newRoom2.roomName = roomName2;
						newRoom2.peopleCount = peopleCount2;
						omokRooms.add(newRoom2); // 방 리스트에 추가
						
						String roomStr2 = String.format("%10s   %d/%d", chatMsg.roomName, 1, chatMsg.peopleCount);
						roomModel.addElement(roomStr2); // 리스트 모델에 추가
						
						repaint();
						break;
					// 서버로부터 전체 접속자 목록 전송된 경우
					// 접속자 이름이 띄어쓰기로 나누어진 하나의 문자열로 전송된다. 
					case "211":
						String str = chatMsg.data;
						StringTokenizer userSt = new StringTokenizer(str);
						allUserModel.removeAllElements();
						while(userSt.hasMoreTokens()) {
							String user = userSt.nextToken();
							allUserModel.addElement(user);
						}
						break;
					// 게임 시작
					case "300":
						// 흑돌 백돌 이름 가져와서 화면에 표시
						String userNames = chatMsg.data;
						StringTokenizer st = new StringTokenizer(userNames);
						omokPanel.blackPlayerName.setText(st.nextToken());
						omokPanel.whitePlayerName.setText(st.nextToken());
						
						if(omokPanel.role == omokPanel.black) { // 흑돌인 경우
							omokPanel.setStatus(true); // status를 true로 설정해 바둑돌을 놓을 수 있는 상태로 변경한다.
							chatPanel.putBtn.setEnabled(true); // 착수 버튼 활성화
						}
						
						chatPanel.returnBtn.setEnabled(true);
						chatPanel.abstentionBtn.setEnabled(true);
						break;
					// 서버로부터 계산된 마우스 이벤트
					case "301":
						omokPanel.putStone(chatMsg.point.x, chatMsg.point.y, chatMsg.role);
						if(chatMsg.role == omokPanel.role) // 내가 보낸 좌표면
							omokPanel.setStatus(false); // 내 차례가 아니므로 false
						else { // 상대방이 보낸 좌표면
							omokPanel.setStatus(true); // 내 차례므로 true
							chatPanel.putBtn.setEnabled(true); // 착수 버튼 활성화
						}
						break;
					// 착수 거부
					case "302":
						JOptionPane.showMessageDialog(mainFrame, "해당 위치에 착수할 수 없습니다.", "Message", JOptionPane.ERROR_MESSAGE);
						omokPanel.setStatus(true);
						chatPanel.putBtn.setEnabled(true);
						break;
					// 무르기 요청
					case "310":
						int response = JOptionPane.showConfirmDialog(mainFrame, "무르기를 허용하시겠습니까?", "무르기", JOptionPane.YES_NO_OPTION);
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
						omokPanel.remove(omokPanel.oldStone);
						break;
					// 기권
					case "313":
						int response4 = JOptionPane.showConfirmDialog(mainFrame, chatMsg.UserName + "님이 기권하셨습니다.\n 게임을 종료하시겠습니까?", "게임 승리", JOptionPane.YES_NO_OPTION);
						if(response4 == JOptionPane.YES_OPTION)
							System.exit(0);
						else
							cardLayout.show(container, "waitingRoomPanel");
						break;
					// 게임 승리
					case "321":
						int response2 = JOptionPane.showConfirmDialog(mainFrame, "게임 승리!\n 게임을 종료하시겠습니까?", "게임 승리", JOptionPane.YES_NO_OPTION);
						if (response2 == JOptionPane.YES_OPTION) { // 게임 종료
							System.exit(0);
						} else { // 대기실로 이동
							cardLayout.show(container, "waitingRoomPanel");
						}
						break;
					// 게임 패배
					case "322":
						int response3 = JOptionPane.showConfirmDialog(mainFrame, "게임 패배!\n 게임을 종료하시겠습니까?", "게임 패배", JOptionPane.YES_NO_OPTION);
						if (response3 == JOptionPane.YES_OPTION) { // 게임 종료
							System.exit(0);
						} else { // 대기실로 이동
							cardLayout.show(container, "waitingRoomPanel");
						}
						break;
					// 채팅 메시지
					case "400":
						chatPanel.appendChatMessageLeft(chatMsg.UserName, chatMsg.data);
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
