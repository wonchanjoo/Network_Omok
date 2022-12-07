import java.awt.EventQueue;
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
	public Vector<OmokRoom> RoomVector = new Vector<OmokRoom>(); // Room을 벡터로 관리

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
		textArea.append(str + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	public void AppendObject(ChatMsg msg) {
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
		public String UserName = ""; // 유저 이름
		//public Vector<UserService> player = new Vector<>();
		public long roomId = -1; // room id의 초기값은 -1
		public int role = -1; // 역할(0,1,2)의 초기값은 -1

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
		
		public void Login() {
			AppendText("새로운 참가자 " + UserName + " 입장.");
			String data = "";
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				data += user.UserName + " ";
			} 
			WriteAllObject(new ChatMsg("SERVER", "211", data));
		}

		public void Logout() {
			long logoutUserRoomId = this.roomId;
			UserVec.removeElement(this); // Logout한 현재 객체를 벡터에서 지운다
			String data = "";
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				data += user.UserName + " ";
			}
			WriteAllObject(new ChatMsg("SERVER", "211", data));
			AppendText("사용자 " + "[" + UserName + "] 퇴장. 현재 참가자 수 " + UserVec.size());
		}
		
		public void GameOver() throws IOException {
			// 게임 끝난 방 지우기
			for(int i=0; i<RoomVector.size(); i++) {
				OmokRoom o = RoomVector.get(i);
				if(o.roomId == this.roomId) {
					RoomVector.removeElementAt(i);
					break;
				}
			}
			
			//보내기
			ChatMsg obj = new ChatMsg("SERVER", "325", "GameOver");
			obj.roomId = this.roomId;
			this.roomId = -1;
			this.role = -1;
			WriteAllObject(obj);
		}
		
		// 모든 User들에게 Object를 방송. 채팅 message와 image object를 보낼 수 있다
		public void WriteAllObject(Object ob) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				user.WriteOneObject(ob);
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
		
		// 바둑알 보내주기(viewer에게)
		public void SendAllPoint(long id) {
			OmokRoom findRoom = null;
			
			// 방 찾기
			for(int i=0; i<RoomVector.size(); i++) {
				OmokRoom o = RoomVector.elementAt(i);
				if(o.roomId == id) {
					findRoom = o;
					break;
				}
			}
			
			if(findRoom == null) {
				System.out.println("방을 찾을 수 없음!");
				return;
			}
			
			for(int i = 0; i < findRoom.stoneList.size(); i++) {
				Point p = findRoom.stoneList.get(i).point;
				ChatMsg obj = new ChatMsg("SERVER", "301", "point");
				obj.roomId = id;
				obj.point = p;
				obj.role = findRoom.stoneList.get(i).role;
				
				WriteOneObject(obj);
			}
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
						Login();
					}
					else if(cm.code.matches("200")) { //방 만들기
						msg = String.format("[%s]님이 [%s]방을 만들었습니다.", cm.UserName, cm.data);
						AppendText(msg); // server 화면에 출력
						cm.roomId = System.currentTimeMillis();
						msg = String.format("roomId : [%s], roomName : [%s] 방이 새로 만들어졌습니다.", cm.roomId, cm.roomName);
						AppendText(msg); // server 화면에 출력
						OmokRoom omokRoom = new OmokRoom(cm.roomId, cm.UserName);
						omokRoom.peopleCount = cm.peopleCount;
						omokRoom.roomName = cm.roomName;
						omokRoom.roomId = cm.roomId;
						
						// 만들어진 방에 비밀번호가 설정되어 있는지
						if(cm.password != null) {
							omokRoom.isPassword = true;
							omokRoom.password = cm.password;
							cm.isPassword = true;
						}
						
						RoomVector.add(omokRoom);
						
						// 방 만들었다는 정보를 전체 유저에게 보내준다. 
						for (int i = 0; i < user_vc.size(); i++) {
							UserService user = (UserService) user_vc.elementAt(i);
							if(user != this)
								user.WriteOneObject(cm);
						}
						
						// 처음 들어온 사람은 흑돌이라고 전송해준다
						ChatMsg obj = new ChatMsg("server", "201", cm.UserName);
						this.role = obj.black; // 흑돌
						obj.role = this.role;
						obj.roomId = cm.roomId;
						this.roomId = cm.roomId;
						WriteOneObject(obj);
						
						ChatMsg obj1 = new ChatMsg("server", "400", "다른 참가자가 들어올 때 까지 잠시만 기다려 주세요...");
						obj.roomId = cm.roomId;
						WriteOneObject(obj1);
					}
					// 방 접속
					else if(cm.code.matches("201")) { 
						OmokRoom findRoom = null;
						
						for(int i=0; i<RoomVector.size(); i++) {
							OmokRoom omokRoom = (OmokRoom) RoomVector.elementAt(i);
							if(cm.roomId == omokRoom.roomId) { // 클라이언트가 보낸 roomId를 비교해 해당 방을 찾는다
								if(omokRoom.isPassword && cm.password.equals(omokRoom.password)) {
									findRoom = omokRoom;
									break;
								}
								else if(!omokRoom.isPassword) {
									findRoom = omokRoom; // 찾은 방 저장
									break;
								}
								else break;
							}
						} // for문 끝
						
						if(findRoom == null) { //비밀번호가 맞지 않는 방
							WriteOneObject(new ChatMsg("SERVER", "202", "비밀번호가 맞지 않습니다."));
							continue;
						}
						
						// player size = 1 이면 이번에 접속한 유저가 백돌
						if(findRoom.player.size() == 1) {
							ChatMsg obj = new ChatMsg(UserName, "201", findRoom.player.get(0) + " " + cm.UserName);
							obj.role = obj.white;
							obj.roomId = cm.roomId;
							this.role = obj.white;
							this.roomId = cm.roomId;
							System.out.println("role : " + this.role);
							WriteOneObject(obj);
							findRoom.player.add(UserName); // player 리스트에 추가
						}
						// player size == 2 이면 이번에 접속한 유저는 관전자
						else if(findRoom.player.size() == 2) {
							// (인원 수 - 플레이어 수) = 남은 인원 수가 현재 관전자 수보다 작으면 들어갈 수 없음
							if((findRoom.peopleCount - 2) <= findRoom.viewer.size()) {
								ChatMsg obj = new ChatMsg("SERVER", "202", "방이 꽉 찼습니다!");
								WriteOneObject(obj);
								continue;
							}
							// 관전자로 접속 가능
							else {
								findRoom.viewer.add(cm.UserName);
								ChatMsg obj = new ChatMsg("SERVER", "201", findRoom.player.get(0) + " " + findRoom.player.get(1));
								obj.roomId = cm.roomId;
								obj.role = view;
								this.role = obj.watch;
								this.roomId = cm.roomId;
								WriteOneObject(obj);
								// 바둑알 전부 보내주기!
								SendAllPoint(cm.roomId);
							}
						}
						msg = String.format("[%s]님이 [%s]방에 접속하셨습니다.", cm.UserName, cm.data);
						AppendText(msg); // server 화면에 출력
						
						// roomId가 같은 유저들에게 새로운 유저 접속 정보 전송
						for (int i = 0; i < user_vc.size(); i++) {
							UserService user = (UserService) user_vc.elementAt(i);
							if(user!=this && roomId == user.roomId) {
								msg = "[" + UserName + "]님이 입장 하였습니다.\n";
								user.WriteOneObject(new ChatMsg("server", "400", msg)); //change
							}
						}
						
						// player가 2명이고, 관전자가 없으면 300 게임 시작 전송
						if(findRoom.player.size() == 2 && findRoom.viewer.size() == 0) { //게임 시작
							ChatMsg obj = new ChatMsg("server", "400", "게임 시작!!"); //게임 시작 메시지를 방에 있는 모든 object에게 뿌림
							for (int i = 0; i < user_vc.size(); i++) {
								UserService user = (UserService) user_vc.elementAt(i);
								if(findRoom.roomId == user.roomId) {
									user.WriteOneObject(obj);
								}
							}
							
							findRoom.status = 1;
							obj = new ChatMsg("server", "300", findRoom.player.elementAt(0) + " " + findRoom.player.elementAt(1));
							obj.roomId = findRoom.roomId;
							obj.roomStatus = findRoom.status;
							
							WriteAllObject(obj);
							
							AppendText("[" + roomId + "]방 게임 시작!!");
							AppendText("현재 [" + roomId + "]방에 있는 플레이어 수 : " + findRoom.player.size());
						}
					} // 201 방 접속 끝
					
					// 210 방 목록 전송(처음 클라이언트가 접속할 때 방 목록 뿌려주기용)
					else if (cm.code.matches("210")) {
						for(int i=0; i<RoomVector.size(); i++) {
							ChatMsg obj = new ChatMsg("server", "210", "방 목록");
							OmokRoom omokRoom = (OmokRoom) RoomVector.elementAt(i);
							obj.roomId = omokRoom.roomId;
							obj.roomName = omokRoom.roomName;
							obj.peopleCount = omokRoom.peopleCount;
							obj.roomStatus = omokRoom.status;
							obj.isPassword = omokRoom.isPassword;
							WriteOneObject(obj);
						}
					}
					
					// 301 마우스 좌표
					else if (cm.code.matches("301")) { //MouseEvent
						OmokRoom findRoom = null;
						//findRoom.omokGame 함수의 반환값
						boolean validOmok = false;
						//승자 판별 변수
						boolean win = false;
						
						for(int i=0; i<RoomVector.size(); i++) {
							OmokRoom omokRoom = (OmokRoom) RoomVector.elementAt(i);
							if(cm.roomId == omokRoom.roomId) { // 클라이언트가 보낸 roomId를 비교해 해당 방을 찾는다
								findRoom = omokRoom; // 찾은 방 저장
							}
						} // for문 끝
						
						validOmok = findRoom.omokGame(cm.point, this.role);
						
						//findRoom.data에 착수 거부 이유를 가지고 있음
						if(validOmok == false) {
							WriteOneObject(new ChatMsg("SERVER", "302", findRoom.data));
							findRoom.data = null;
							continue;
						}
						
						//Console 창에 오목 배열 띄우기
						//System.out.println("role(0: 흑돌, 1: 흰돌) : " + cm.role);
						//findRoom.DisplayOmok();
						
						//좌표를 모든 참가자에게 뿌려준다.
						ChatMsg obj = new ChatMsg(cm.UserName, "301", "좌표");
						obj.point = cm.point;
						obj.role = cm.role;
						obj.roomId = cm.roomId;
						for (int i = 0; i < user_vc.size(); i++) {
							UserService user = (UserService) user_vc.elementAt(i);
							if(cm.roomId == user.roomId) {
								user.WriteOneObject(obj);
							}
						}
						
						// 바둑돌 좌표 리스트에 좌표 추가
						findRoom.stoneList.add(new Stone(cm.role, cm.point));
						
						//승자 판별...
						if(role == black) {
							win = findRoom.CheckOmok(1);
						}
						else if(role == white){
							win = findRoom.CheckOmok(2);
						}
						
						if(win) {
							String winner = this.UserName;
							String loser = "";
							oos.writeObject(new ChatMsg("server", "321", "Win")); // 승자한테 이겼다고 전송
							for(int i = 0; i < findRoom.player.size(); i++) {
								if(!findRoom.player.elementAt(i).equals(this.UserName)) {
									loser = findRoom.player.elementAt(i);
									break;
								}
							}
							
							for (int i = 0; i < user_vc.size(); i++) {
								UserService user = (UserService) user_vc.elementAt(i);
								if(loser.equals(user.UserName)) {
									user.WriteOneObject(new ChatMsg("SERVER", "322", "Lose")); // 게임 패배 전송
								}
								else if(user!=this && cm.roomId == user.roomId) { // 게임 종료 전송
									user.WriteOneObject(new ChatMsg("SERVER", "323", "Game Over"));
								}
							}
							GameOver();
						}
						
					}
					else if (cm.code.matches("310")) { //무르기 요청
						String str = "[" + cm.UserName + "]님이 무르기를 요청하셨습니다.";
						AppendText(str);
						// 관전자를 제외하고, 플레이어 한명에게만 전송해야함
						String name = "";
						OmokRoom r = null;
						
						// 무르기 요청한 방 찾기
						for(int i = 0; i < RoomVector.size(); i++) {
							OmokRoom o = RoomVector.get(i);
							if(o.roomId == cm.roomId) {
								r = o;
								break;
							}
						}
						
						// 무르기 요청한 사람의 상대방 찾기
						if(r.player.get(0).equals(cm.UserName))
							name = r.player.get(1);
						else
							name = r.player.get(0);
						
						// 상대방 UserService 찾아서 그대로 전송
						for (int i = 0; i < user_vc.size(); i++) {
							UserService user = (UserService) user_vc.elementAt(i);
							if(user!=this && roomId == user.roomId && user.UserName.equals(name)) {
								user.WriteOneObject(cm);
								break;
							}
						}
					}
					else if (cm.code.matches("311")) { //무르기 허용
						String str = "[" + cm.UserName + "]님이 무르기를 허용하셨습니다.";
						AppendText(str);
						
						System.out.println("point = " + cm.point);
						
						for(int i=0; i<RoomVector.size(); i++) {
							OmokRoom o = RoomVector.get(i);
							if(o.roomId == cm.roomId) {
								o.deletePoint(cm.point);
								break;
							}
						}
						
						for (int i = 0; i < user_vc.size(); i++) {
							UserService user = (UserService) user_vc.elementAt(i);
							if(roomId == user.roomId) {
								user.WriteOneObject(cm);
							}
						}
					}
					else if (cm.code.matches("312")) { //무르기 거절
						String str = "[" + cm.UserName + "]님이 무르기를 거절하셨습니다.";
						AppendText(str);
						// 관전자를 제외하고, 플레이어 한명에게만 전송해야함
						String name = "";
						OmokRoom r = null;
						
						// 무르기 요청한 방 찾기
						for(int i = 0; i < RoomVector.size(); i++) {
							OmokRoom o = RoomVector.get(i);
							if(o.roomId == cm.roomId) {
								r = o;
								break;
							}
						}
						
						// 무르기 요청한 사람의 상대방 찾기
						if(r.player.get(0).equals(cm.UserName))
							name = r.player.get(1);
						else
							name = r.player.get(0);
						
						// 상대방 UserService 찾아서 그대로 전송
						for (int i = 0; i < user_vc.size(); i++) {
							UserService user = (UserService) user_vc.elementAt(i);
							if(user!=this && roomId == user.roomId && user.UserName.equals(name)) {
								user.WriteOneObject(cm);
								break;
							}
						}
					}
					else if (cm.code.matches("320")) { //항복
						String str = "[" + cm.UserName + "]님이 항복하셨습니다.";
						AppendText(str);
						for (int i = 0; i < user_vc.size(); i++) {
							UserService user = (UserService) user_vc.elementAt(i);
							if(cm.roomId == user.roomId) {
								user.WriteOneObject(cm);
							}
						}
						GameOver();
					}
					else if (cm.code.matches("400")) { //chatting
						msg = String.format("[%s] %s", cm.UserName, cm.data);
						AppendText(msg); // server 화면에 출력
						for (int i = 0; i < user_vc.size(); i++) {
							UserService user = (UserService) user_vc.elementAt(i);
							if (user != this && cm.roomId == user.roomId) {
								ChatMsg obj = new ChatMsg(UserName, "400", cm.data);
								obj.roomId = cm.roomId;
								user.WriteOneObject(obj);
							}
						}
					}
					else if (cm.code.matches("410")) { //방 접속자 목록...
						OmokRoom findRoom = null;
						String data = "";
						for(int i=0; i<RoomVector.size(); i++) {
							OmokRoom omokRoom = (OmokRoom) RoomVector.elementAt(i);
							if(cm.roomId == omokRoom.roomId) { // 클라이언트가 보낸 roomId를 비교해 해당 방을 찾는다
								findRoom = omokRoom; // 찾은 방 저장
								break;
							}
						}
						
						// player
						for(int j=0; j<findRoom.player.size(); j++) {
							data += findRoom.player.elementAt(j) + " ";
						}
						// viewer
						for(int j=0; j<findRoom.viewer.size(); j++) {
							data += findRoom.viewer.elementAt(j) + " ";
						}
						
						for(int i=0; i<user_vc.size(); i++) {
							UserService u = (OmokServer.UserService) user_vc.get(i);
							if(u.roomId == cm.roomId)
								//u.WriteOne("410", data);
								u.WriteOneObject(new ChatMsg("SERVER", "410", data));
						}
					}

//					else if (cm.code.matches("600")) { // logout
//						Logout();
//						break;
//					}
				else { // 300, 500, ... 기타 object는 모두 방송한다.
						WriteAllObject(cm);
					} 
				} catch (IOException e) {
					AppendText("ois.readObject() error");
					try {
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
