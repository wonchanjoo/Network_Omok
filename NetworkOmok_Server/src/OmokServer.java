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
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;

		private ObjectInputStream ois;
		private ObjectOutputStream oos;

		private Socket client_socket;
		private Vector user_vc;
		public String UserName = "";
		public String UserStatus;
		public Vector<UserService> player = new Vector();
		public int roomId;
		public boolean isBlack = false;

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
			//WriteOne("Welcome to Java chat server\n");
			//WriteOne(UserName + "님 환영합니다.\n"); // 연결된 사용자에게 정상접속을 알림
			//String msg = "[" + UserName + "]님이 입장 하였습니다.\n";
			//WriteOthers(msg); // 아직 user_vc에 새로 입장한 user는 포함되지 않았다.
		}

		public void Logout() {
			String msg = "[" + UserName + "]님이 퇴장 하였습니다.\n";
			UserVec.removeElement(this); // Logout한 현재 객체를 벡터에서 지운다
			WriteAll(msg); // 나를 제외한 다른 User들에게 전송
			AppendText("사용자 " + "[" + UserName + "] 퇴장. 현재 참가자 수 " + UserVec.size());
		}

		// 모든 User들에게 방송. 각각의 UserService Thread의 WriteONe() 을 호출한다.
		public void WriteAll(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user.UserStatus == "O")
					user.WriteOne(str);
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
		public void WriteOthers(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user != this && user.UserStatus == "O")
					user.WriteOne(str);
			}
		}

		// Windows 처럼 message 제외한 나머지 부분은 NULL 로 만들기 위한 함수
		public byte[] MakePacket(String msg) {
			byte[] packet = new byte[BUF_LEN];
			byte[] bb = null;
			int i;
			for (i = 0; i < BUF_LEN; i++)
				packet[i] = 0;
			try {
				bb = msg.getBytes("euc-kr");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (i = 0; i < bb.length; i++)
				packet[i] = bb[i];
			return packet;
		}

		// UserService Thread가 담당하는 Client 에게 1:1 전송
		public void WriteOne(String msg) {
			try {
				// dos.writeUTF(msg);
//				byte[] bb;
//				bb = MakePacket(msg);
//				dos.write(bb, 0, bb.length);
				ChatMsg obcm = new ChatMsg("SERVER", "200", msg);
				oos.writeObject(obcm);
			} catch (IOException e) {
				AppendText("dos.writeObject() error");
				try {
//					dos.close();
//					dis.close();
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
						UserStatus = "O"; // Online 상태
						Login();
					}
					else if(cm.code.matches("200")) { //방 만들기
						msg = String.format("[%s]님이 [%s]방을 만들었습니다.", cm.UserName, cm.data);
						AppendText(msg); // server 화면에 출력
						OmokRoom omokRoom = new OmokRoom(cm.roomId, cm.UserName);
						omokRoom.currentPeoples = cm.peopleCount;
						omokRoom.roomName = cm.roomName;
						RoomVector.add(omokRoom);
						this.roomId = omokRoom.roomId;
						
						for (int i = 0; i < user_vc.size(); i++) {
							UserService user = (UserService) user_vc.elementAt(i);
							user.oos.writeObject(cm);
						}
						
						this.isBlack = true;
						ChatMsg obj = new ChatMsg("server", "201", "흑돌");
						obj.isBlack = true;
						oos.writeObject(obj);
					}
					else if(cm.code.matches("201")) { // 방 접속
						int exist = 0;
						//isBlack = cm.isBlack; //change
						
						OmokRoom currentRoom = null;
						
						for(int i=0; i<RoomVector.size(); i++) {
							OmokRoom omokRoom = (OmokRoom) RoomVector.elementAt(i);
							if(omokRoom.player.size() == 2) {
								exist = 1;
								break;
							}
							if(cm.roomId == omokRoom.roomId) { //currentPeoples 대신 player 수로 해야함(Vector)
								omokRoom.player.add(cm.UserName);
								//omokRoom.currentPeoples++;
								this.roomId = omokRoom.roomId;
								currentRoom = omokRoom;
								break;
							}
						}
						if(exist==1) continue;
						msg = String.format("[%s]님이 [%s]방에 접속하셨습니다.", cm.UserName, cm.data);
						AppendText(msg); // server 화면에 출력
						
						//this.player.add(this);
						//this.roomId = cm.roomId;
						
						for (int i = 0; i < user_vc.size(); i++) {
							UserService user = (UserService) user_vc.elementAt(i);
							if(user!=this && roomId == user.roomId) {
								//this.isBlack = false;
								//user.player.add(this);
								//this.player.add(user);
								msg = "[" + UserName + "]님이 입장 하였습니다.\n";
								user.oos.writeObject(new ChatMsg("server", "400", msg)); //change
							}
						}
						
						if(currentRoom.player.size() == 2) { //게임 시작
							this.isBlack = false;
							ChatMsg obj = new ChatMsg("server", "201", "백돌");
							obj.isBlack = false;
							oos.writeObject(obj);
		
							obj = new ChatMsg("server", "400", "게임 시작!!"); //게임 시작 메시지를 방에 있는 모든 object에게 뿌림
							for (int i = 0; i < user_vc.size(); i++) {
								UserService user = (UserService) user_vc.elementAt(i);
								if(roomId == user.roomId) {
									user.oos.writeObject(obj);
								}
							}
							
							obj = new ChatMsg("server", "300", player.elementAt(0).UserName + " " +player.elementAt(1).UserName);
							
							for (int i = 0; i < user_vc.size(); i++) {
								UserService user = (UserService) user_vc.elementAt(i);
								if(roomId == cm.roomId) {
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
							AppendText("현재 [" + roomId + "]방에 있는 플레이어 수 : " + this.player.size());
						}
						else {

							ChatMsg obj = new ChatMsg("server", "400", "다른 참가자가 들어올 때 까지 잠시만 기다려 주세요...");
							for (int i = 0; i < user_vc.size(); i++) {
								UserService user = (UserService) user_vc.elementAt(i);
								if(roomId == user.roomId) {
									user.oos.writeObject(obj);
								}
							}
							this.isBlack = true;
							obj = new ChatMsg("server", "201", "흑돌");
							//obj1.roomId = this.roomId;
							//obj1.player.add(this);
							obj.isBlack = true;
							oos.writeObject(obj);
							AppendText("현재 [" + roomId + "]방에 있는 플레이어 수 : " + this.player.size());
						}
					}
					else if (cm.code.matches("301")) { //MouseEvent
						int boardX = (cm.point.x-37)/27;
						int boardY = (cm.point.y-110)/27;
						boolean winner = false;
				
						//착수 거부, 33도 여기서 처리?
						if(board[boardX][boardY] != 0) {
							ChatMsg msg1 = new ChatMsg("server", "302", "location error");
							oos.writeObject(msg1);
							continue;
						}
						
						String blwr = cm.isBlack ? "Black" : "White";

						System.out.println(blwr + " : boardX = " + boardX + " boardY = " + boardY);
						
						if(this.isBlack) {
						//if(cm.isBlack) {
							board[boardX][boardY] = 1;
							winner = CheckOmok(1);
						}
						else {
							board[boardX][boardY] = 2;
							winner = CheckOmok(2);
						}
						
						//좌표를 모든 참가자에게 뿌려준다.
						ChatMsg msg1 = new ChatMsg(cm.UserName, "301", "좌표");
						msg1.isBlack = cm.isBlack;
						msg1.point = cm.point;
						for (int i = 0; i < user_vc.size(); i++) {
							UserService user = (UserService) user_vc.elementAt(i);
							if(roomId == user.roomId) {
								user.oos.writeObject(msg1);
							}
						}
						
						if(winner) {
							//ChatMsg msg2 = new ChatMsg();
							oos.writeObject(new ChatMsg("server", "321", "Win"));
							for (int i = 0; i < user_vc.size(); i++) {
								UserService user = (UserService) user_vc.elementAt(i);
								if(user!=this && roomId == user.roomId) {
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
						String[] args = msg.split(" "); // 단어들을 분리한다.
						if (args.length == 1) { // Enter key 만 들어온 경우 Wakeup 처리만 한다.
							UserStatus = "O";
						} else if (args[1].matches("/exit")) {
							Logout();
							break;
						} else if (args[1].matches("/list")) {
							WriteOne("User list\n");
							WriteOne("Name\tStatus\n");
							WriteOne("-----------------------------\n");
							for (int i = 0; i < user_vc.size(); i++) {
								UserService user = (UserService) user_vc.elementAt(i);
								WriteOne(user.UserName + "\t" + user.UserStatus + "\n");
							}
							WriteOne("-----------------------------\n");
						}
//						else if (args[1].matches("/sleep")) {
//							UserStatus = "S";
//						} else if (args[1].matches("/wakeup")) {
//							UserStatus = "O";
//						} else if (args[1].matches("/to")) { // 귓속말
//							for (int i = 0; i < user_vc.size(); i++) {
//								UserService user = (UserService) user_vc.elementAt(i);
//								if (user.UserName.matches(args[2]) && user.UserStatus.matches("O")) {
//									String msg2 = "";
//									for (int j = 3; j < args.length; j++) {// 실제 message 부분
//										msg2 += args[j];
//										if (j < args.length - 1)
//											msg2 += " ";
//									}
//									// /to 빼고.. [귓속말] [user1] Hello user2..
//									user.WritePrivate(args[0] + " " + msg2 + "\n");
//									//user.WriteOne("[귓속말] " + args[0] + " " + msg2 + "\n");
//									break;
//								}
//							}
//						}
						else { // 일반 채팅 메시지
							UserStatus = "O";
							//WriteAll(msg + "\n"); // Write All
							for (int i = 0; i < user_vc.size(); i++) {
								UserService user = (UserService) user_vc.elementAt(i);
								if (user != this && user.UserStatus == "O" && roomId == user.roomId)
									user.oos.writeObject(new ChatMsg(UserName, "400", cm.data));
							}
						}
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
