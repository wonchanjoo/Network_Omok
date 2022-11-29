import java.awt.EventQueue;
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
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class OmokServer extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	JTextArea textArea;
	private JTextField txtPortNumber;

	private ServerSocket socket; // 서버소켓
	private Socket client_socket; // accept() 에서 생성된 client 소켓
	private Vector<UserService> UserVec = new Vector(); // 연결된 사용자를 저장할 벡터
	private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의
	public Vector<OmokRoom> RoomVector = new Vector<OmokRoom>();

	public int[][] board = new int[19][19]; // 오목판, 방마다 하나씩 가지고 있어야함
	
	public void board_setUp() {
		for(int i=0; i<19; i++) {
			for(int j=0; j<19; j++) {
				board[i][j] = 0;
			}
		}
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					OmokServer frame = new OmokServer();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public OmokServer() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 338, 440);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 300, 298);
		contentPane.add(scrollPane);

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		JLabel lblNewLabel = new JLabel("Port Number");
		lblNewLabel.setBounds(13, 318, 87, 26);
		contentPane.add(lblNewLabel);

		txtPortNumber = new JTextField();
		txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
		txtPortNumber.setText("30000");
		txtPortNumber.setBounds(112, 318, 199, 26);
		contentPane.add(txtPortNumber);
		txtPortNumber.setColumns(10);

		JButton btnServerStart = new JButton("Server Start");
		btnServerStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
				} catch (NumberFormatException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				AppendText("Chat Server Running..");
				btnServerStart.setText("Chat Server Running..");
				btnServerStart.setEnabled(false); // 서버를 더이상 실행시키지 못 하게 막는다
				txtPortNumber.setEnabled(false); // 더이상 포트번호 수정못 하게 막는다
				AcceptServer accept_server = new AcceptServer();
				accept_server.start();
			}
		});
		btnServerStart.setBounds(12, 356, 300, 35);
		contentPane.add(btnServerStart);
	}

	// 새로운 참가자 accept() 하고 user thread를 새로 생성한다.
	class AcceptServer extends Thread {
		@SuppressWarnings("unchecked")
		public void run() {
			while (true) { // 사용자 접속을 계속해서 받기 위해 while문
				try {
					AppendText("Waiting new clients ...");
					client_socket = socket.accept(); // accept가 일어나기 전까지는 무한 대기중
					AppendText("새로운 참가자 from " + client_socket);
					// User 당 하나씩 Thread 생성
					UserService new_user = new UserService(client_socket);
					UserVec.add(new_user); // 새로운 참가자 배열에 추가
					new_user.start(); // 만든 객체의 스레드 실행
					AppendText("현재 참가자 수 " + UserVec.size());
				} catch (IOException e) {
					AppendText("accept() error");
					// System.exit(0);
				}
			}
		}	
	}

	public void AppendText(String str) {
		// textArea.append("사용자로부터 들어온 메세지 : " + str+"\n");
		textArea.append(str + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	public void AppendObject(ChatMsg msg) {
		// textArea.append("사용자로부터 들어온 object : " + str+"\n");
		textArea.append("code = " + msg.code + "\n");
		textArea.append("id = " + msg.UserName + "\n");
		textArea.append("data = " + msg.data + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}
	
	// User 당 생성되는 Thread
	// Read One 에서 대기 -> Write All
	class UserService extends Thread {
		public final int black = 0;
		public final int white = 1;
		public final int view = 2;
		
		private InputStream is;
		private OutputStream os;

		private ObjectInputStream ois;
		private ObjectOutputStream oos;

		private Socket client_socket;
		private Vector user_vc;
		public String UserName = "";
		public String UserStatus;
		public Vector<UserService> player = new Vector<>();
		public long roomId;
		public boolean isBlack = false;
		public int role;

		public UserService(Socket client_socket) {
			// TODO Auto-generated constructor stub
			// 매개변수로 넘어온 자료 저장
			this.client_socket = client_socket;
			this.user_vc = UserVec;
			try {
				oos = new ObjectOutputStream(client_socket.getOutputStream());
				oos.flush();
				ois = new ObjectInputStream(client_socket.getInputStream());
			} catch (Exception e) {
				AppendText("userService error");
			}
		}
		
		//Point 좌표를 int[19][19] 배열 좌표로 변환
		private int getStoneX(int x) {
			int value = (x - 37) / 27;
			int rest = (x - 37) % 27;
			if(rest <= 13)
				return 37 + 27 * value;
			else {
				return 37 + 27 * (value + 1);
			}
		}
		
		private int getStoneY(int y) {
			int value = (y - 110) / 27;
			int rest = (y - 110) % 27;
			if(rest <= 13)
				return 110 + 27 * value;
			else 
				return 110 + 27 * (value + 1);
		}
		
		public void Login() {
			AppendText("새로운 참가자 " + UserName + " 입장.");
			String data = "";
			for (int i = 0; i < user_vc.size() - 1; i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				data += user.UserName + " ";
			}
			// 기존에 있던 유저들에게는 새로온 사람의 이름만 전송해주면 되고
			// 새로 접속한 유저에게는 모든 사람의 이름을 전송해야 한다. 
			WriteOne("211", data); // 새로 접속한 유저에게 모든 유저 정보 전송
			WriteAll("211", UserName); // 모든 유저에게 새로 접속한 유저 정보 전송
			
			// 새로 접속한 유저에게 방 리스트 전송
//			data = "";
//			for(int i = 0; i < RoomVector.size(); i++) {
//				OmokRoom o = RoomVector.elementAt(i);
//				String s = o.roomId + " " + o.roomName + " " + o.peopleCount;
//				data += s + "@";
//			}
//			WriteOne("210", data);
		}

		public void Logout() {
			UserVec.removeElement(this); // Logout한 현재 객체를 벡터에서 지운다
			String data = "";
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				data += user.UserName + " ";
			}
			WriteAll("211", data);
			AppendText("사용자 " + "[" + UserName + "] 퇴장. 현재 참가자 수 " + UserVec.size());
		}

		// 모든 User들에게 방송. 각각의 UserService Thread의 WriteONe() 을 호출한다.
		public void WriteAll(String code, String data) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				user.WriteOne(code, data);
			}
		}
		// 모든 User들에게 Object를 방송. 채팅 message와 image object를 보낼 수 있다
		public void WriteAllObject(Object ob) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user.UserStatus == "O")
					user.WriteOneObject(ob);
			}
		}

		// 나를 제외한 User들에게 방송. 각각의 UserService Thread의 WriteONe() 을 호출한다.
//		public void WriteOthers(String str) {
//			for (int i = 0; i < user_vc.size(); i++) {
//				UserService user = (UserService) user_vc.elementAt(i);
//				if (user != this && user.UserStatus == "O")
//					user.WriteOne(str);
//			}
//		}

		// UserService Thread가 담당하는 Client 에게 1:1 전송
		public void WriteOne(String code, String data) {
			try {
				ChatMsg obcm = new ChatMsg("SERVER", code, data);
				oos.writeObject(obcm);
			} catch (IOException e) {
				AppendText("dos.writeObject() error");
				try {
					ois.close();
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Logout(); // 에러가난 현재 객체를 벡터에서 지운다
			}
		}

		// 귓속말 전송
		public void WritePrivate(String msg) {
			try {
				ChatMsg obcm = new ChatMsg("귓속말", "200", msg);
				oos.writeObject(obcm);
			} catch (IOException e) {
				AppendText("dos.writeObject() error");
				try {
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Logout(); // 에러가난 현재 객체를 벡터에서 지운다
			}
		}
		
		public void WriteOneObject(Object ob) {
			try {
			    oos.writeObject(ob);
			} 
			catch (IOException e) {
				AppendText("oos.writeObject(ob) error");		
				try {
					ois.close();
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;				
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Logout();
			}
		}
		
		//오목 승자 판별
		private boolean CheckOmok(int blwh) {
			for(int i=0; i<19; i++) {
				for(int j=0; j<19; j++) {
					if(board[i][j]==blwh && board[i][j+1]==blwh && board[i][j+2]==blwh && board[i][j+3]==blwh && board[i][j+4]==blwh) return true;
					else if(board[i][j]==blwh && board[i+1][j]==blwh && board[i+2][j]==blwh && board[i+3][j]==blwh && board[i+4][j]==blwh) return true;
					else if(board[i][j]==blwh && board[i+1][j+1]==blwh && board[i+2][j+2]==blwh && board[i+3][j+3]==blwh && board[i+4][j+4]==blwh) return true;
					else if(board[i][j]==blwh && board[i+1][j-1]==blwh && board[i+2][j-2]==blwh && board[i+3][j-3]==blwh && board[i+4][j-4]==blwh) return true;
				}
			}
			return false;
		}
		
		public void run() {
			while (true) { // 사용자 접속을 계속해서 받기 위해 while문
				try {
					Object obcm = null;
					String msg = null;
					ChatMsg cm = null;
					if (socket == null)
						break;
					
					try {
						obcm = ois.readObject();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
					
					if (obcm == null)
						break;
					if (obcm instanceof ChatMsg) {
						cm = (ChatMsg) obcm;
						AppendObject(cm);
					} else
						continue;
					
					if (cm.code.matches("100")) { //login
						UserName = cm.UserName;
						//UserStatus = "O"; // Online 상태
						Login();
					}
					else if(cm.code.matches("200")) { //방 만들기
						msg = String.format("[%s]님이 [%s]방을 만들었습니다.", cm.UserName, cm.data);
						AppendText(msg); // server 화면에 출력
						cm.roomId = System.currentTimeMillis();
						OmokRoom omokRoom = new OmokRoom(cm.roomId, cm.UserName);
						omokRoom.peopleCount = cm.peopleCount;
						omokRoom.roomName = cm.roomName;
						omokRoom.password = cm.password;
						omokRoom.roomId = cm.roomId;
						this.roomId = cm.roomId;
						RoomVector.add(omokRoom);
						
						// 방 만들었다는 정보를 전체 유저에게 보내준다. 
						for (int i = 0; i < user_vc.size(); i++) {
							UserService user = (UserService) user_vc.elementAt(i);
							user.oos.writeObject(cm);
						}
						
						// 임시로 처음 들어온 사람은 흑돌이라고 전송해준다
						this.isBlack = true;
						ChatMsg obj = new ChatMsg("server", "201", "흑돌");
						obj.role = obj.black; // 흑돌
						obj.roomId = roomId;
						oos.writeObject(obj);
					}
					// 방 접속
					else if(cm.code.matches("201")) { 
						OmokRoom currentRoom = null;
						OmokRoom findRoom = null;
						
						for(int i=0; i<RoomVector.size(); i++) {
							OmokRoom omokRoom = (OmokRoom) RoomVector.elementAt(i);
							// task : 비밀번호 처리도 해야함
							if(cm.roomId == omokRoom.roomId) { // 클라이언트가 보낸 roomId를 비교해 해당 방을 찾는다
								findRoom = omokRoom; // 찾은 방 저장
							}
						} // for문 끝
						
						this.roomId = cm.roomId;
						
						// player size = 0 이면 이번에 접속한 유저가 흑돌
						if(findRoom.player.size() == 0) {
							ChatMsg obj = new ChatMsg(UserName, "201", "흑돌");
							obj.role = obj.black;
							obj.roomId = findRoom.roomId;
							WriteOneObject(obj);
							findRoom.player.add(UserName); // player 리스트에 추가
						}
						// player size = 1 이면 이번에 접속한 유저가 백돌
						else if(findRoom.player.size() == 1) {
							ChatMsg obj = new ChatMsg(UserName, "201", "백돌");
							obj.role = obj.white;
							obj.roomId = findRoom.roomId;
							WriteOneObject(obj);
							findRoom.player.add(UserName); // player 리스트에 추가
						}
						
						msg = String.format("[%s]님이 [%s]방에 접속하셨습니다.", cm.UserName, cm.data);
						AppendText(msg); // server 화면에 출력
						
						// roomId가 같은 유저들에게 새로운 유저 접속 정보 전송
						for (int i = 0; i < user_vc.size(); i++) {
							UserService user = (UserService) user_vc.elementAt(i);
							if(user!=this && roomId == user.roomId) {
								msg = "[" + UserName + "]님이 입장 하였습니다.\n";
								user.oos.writeObject(new ChatMsg("server", "400", msg)); //change
							}
						}
						
						// player가 2명이 300 게임 시작 전송
						if(findRoom.player.size() == 2) { //게임 시작
							ChatMsg obj = new ChatMsg("server", "400", "게임 시작!!"); //게임 시작 메시지를 방에 있는 모든 object에게 뿌림
							for (int i = 0; i < user_vc.size(); i++) {
								UserService user = (UserService) user_vc.elementAt(i);
								if(findRoom.roomId == user.roomId) {
									user.oos.writeObject(obj);
								}
							}
							
							obj = new ChatMsg("server", "300", findRoom.player.elementAt(0) + " " + findRoom.player.elementAt(1));
							
							for (int i = 0; i < user_vc.size(); i++) {
								UserService user = (UserService) user_vc.elementAt(i);
								if(findRoom.roomId == cm.roomId) {
									user.oos.writeObject(obj);
								}
							}
							
							//방에서 관리...
							for(int i=0; i<19; i++) {
								for(int j=0; j<19; j++) {
									board[i][j] = 0;
								}
							}
							
							AppendText("[" + roomId + "]방 게임 시작!!");
							//AppendText("현재 [" + roomId + "]방에 있는 플레이어 수 : " + currentRoom.player.size());
							AppendText("현재 [" + roomId + "]방에 있는 플레이어 수 : " + findRoom.player.size());
						}
						else { // 아직 player가 2명이 안 된 경우
							ChatMsg obj = new ChatMsg("server", "400", "다른 참가자가 들어올 때 까지 잠시만 기다려 주세요...");
							for (int i = 0; i < user_vc.size(); i++) {
								UserService user = (UserService) user_vc.elementAt(i);
								if(roomId == user.roomId) {
									user.oos.writeObject(obj);
								}
							}
							AppendText("현재 [" + roomId + "]방에 있는 플레이어 수 : " + this.player.size());
						}
					} // 201 방 접속 끝
					// 210 방 목록 전송(처음 클라이언트가 접속할 때 방 목록 뿌려주기용)
					else if (cm.code.matches("210")) { 
						ChatMsg obj = new ChatMsg("server", "210", "방 목록");
						for(int i=0; i<RoomVector.size(); i++) {
							OmokRoom omokRoom = (OmokRoom) RoomVector.elementAt(i);
							obj.roomId = omokRoom.roomId;
							obj.password = omokRoom.password;
							obj.roomName = omokRoom.roomName;
							obj.peopleCount = omokRoom.peopleCount;
							oos.writeObject(obj);
						}
					}
					// 301 마우스 좌표
					else if (cm.code.matches("301")) { //MouseEvent
						int boardX = (cm.point.x-37)/27;
						int boardY = (cm.point.y-110)/27;
						boolean winner = false;
				
						//착수 거부, 33도 여기서 처리? -> 클라이언트가 바둑돌 위치를 가지고 있지 않아서 서버에서 처리해야될듯 합니당
						if(board[boardX][boardY] != 0) {
							ChatMsg msg1 = new ChatMsg("server", "302", "location error");
							oos.writeObject(msg1);
							continue;
						}
						
						//String blwr = cm.isBlack ? "Black" : "White";

						//System.out.println(blwr + " : boardX = " + boardX + " boardY = " + boardY);
						
						if(this.role == black) {
							board[boardX][boardY] = 1;
							winner = CheckOmok(1);
						}
						else if(this.role == white){
							board[boardX][boardY] = 2;
							winner = CheckOmok(2);
						}
						
						//좌표를 모든 참가자에게 뿌려준다.
						ChatMsg msg1 = new ChatMsg(cm.UserName, "301", "좌표");
						msg1.point = cm.point;
						msg1.role = cm.role;
						msg1.roomId = cm.roomId;
						for (int i = 0; i < user_vc.size(); i++) {
							UserService user = (UserService) user_vc.elementAt(i);
							if(cm.roomId == user.roomId) {
								user.oos.writeObject(msg1);
							}
						}
						
						if(winner) {
							//ChatMsg msg2 = new ChatMsg();
							oos.writeObject(new ChatMsg("server", "321", "Win"));
							for (int i = 0; i < user_vc.size(); i++) {
								UserService user = (UserService) user_vc.elementAt(i);
								if(user!=this && cm.roomId == user.roomId) {
									user.oos.writeObject(new ChatMsg("server", "322", "lose"));
									break;
								}
							}
						}
						
					}
					else if (cm.code.matches("310")) { //무르기 요청
						String str = "[" + cm.UserName + "]님이 무르기를 요청하셨습니다.";
						AppendText(str);
						for (int i = 0; i < user_vc.size(); i++) {
							UserService user = (UserService) user_vc.elementAt(i);
							if(user!=this && roomId == user.roomId) {
								user.oos.writeObject(cm);
							}
						}
					}
					else if (cm.code.matches("311")) { //무르기 허용
						String str = "[" + cm.UserName + "]님이 무르기를 허용하셨습니다.";
						AppendText(str);
						for (int i = 0; i < user_vc.size(); i++) {
							UserService user = (UserService) user_vc.elementAt(i);
							if(user!=this && roomId == user.roomId) {
								user.oos.writeObject(cm);
							}
						}
					}
					else if (cm.code.matches("312")) { //무르기 거절
						String str = "[" + cm.UserName + "]님이 무르기를 거절하셨습니다.";
						AppendText(str);
						for (int i = 0; i < user_vc.size(); i++) {
							UserService user = (UserService) user_vc.elementAt(i);
							if(user!=this && roomId == user.roomId) {
								user.oos.writeObject(cm);
							}
						}
					}
					else if (cm.code.matches("313")) { //항복
						String str = "[" + cm.UserName + "]님이 항복하셨습니다.";
						AppendText(str);
						for (int i = 0; i < user_vc.size(); i++) {
							UserService user = (UserService) user_vc.elementAt(i);
							if(user!=this && roomId == user.roomId) {
								user.oos.writeObject(cm);
							}
						}
						//게임 종료! 이긴 사람에게는 321, 진 사람에게는 322 프로토콜 전달, 관전자에게는 323
					}
					else if (cm.code.matches("324")) { //제한 시간 종료
						String str = "[" + cm.UserName + "]님의 제한 시간이 종료되었습니다.";
						AppendText(str);
						for (int i = 0; i < user_vc.size(); i++) {
							UserService user = (UserService) user_vc.elementAt(i);
							if(user!=this && roomId == user.roomId) {
								user.oos.writeObject(cm);
							}
						}
					}
					
					else if (cm.code.matches("400")) { //chatting
						msg = String.format("[%s] %s", cm.UserName, cm.data);
						AppendText(msg); // server 화면에 출력
							UserStatus = "O";
							//WriteAll(msg + "\n"); // Write All
							for (int i = 0; i < user_vc.size(); i++) {
								UserService user = (UserService) user_vc.elementAt(i);
								if (user != this && cm.roomId == user.roomId) {
									ChatMsg obj = new ChatMsg(UserName, "400", cm.data);
									obj.roomId = cm.roomId;
									user.oos.writeObject(obj);
								}
							}
						//}
					}
					else if (cm.code.matches("500")) { //게임 초대
						String str = "[" + cm.UserName + "]님이 [" + cm.data + "]님을 초대하셨습니다.";
						AppendText(str);
						for (int i = 0; i < user_vc.size(); i++) {
							UserService user = (UserService) user_vc.elementAt(i);
							if(cm.data.equals(user.UserName)) {
								user.oos.writeObject(cm);
							}
						}
					}
					else if (cm.code.matches("501")) { //게임 초대 승인
						String str = "[" + cm.UserName + "]님이 초대를 승인하셨습니다.";
						AppendText(str);
						for (int i = 0; i < user_vc.size(); i++) {
							UserService user = (UserService) user_vc.elementAt(i);
							if(cm.data.equals(user.UserName)) {
								this.roomId = user.roomId;
								this.player.add(this);
								this.player.add(user);
								user.player.add(this);
							}
						}
					}
					else if (cm.code.matches("502")) { //게임 초대 거절
						String str = "[" + cm.UserName + "]님이 초대를 거절하셨습니다.";
						AppendText(str);
						for (int i = 0; i < user_vc.size(); i++) {
							UserService user = (UserService) user_vc.elementAt(i);
							if(cm.data.equals(user.UserName)) {
								user.oos.writeObject(cm);
							}
						}
					}
					else if (cm.code.matches("600")) { // logout
						Logout();
						break;
					} else { // 300, 500, ... 기타 object는 모두 방송한다.
						WriteAllObject(cm);
					} 
				} catch (IOException e) {
					AppendText("ois.readObject() error");
					try {
//						dos.close();
//						dis.close();
						ois.close();
						oos.close();
						client_socket.close();
						Logout(); // 에러가난 현재 객체를 벡터에서 지운다
						break;
					} catch (Exception ee) {
						break;
					} // catch문 끝
				} // 바깥 catch문끝
			} // while
		} // run
	}

}
