import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.Font;

public class ChatPanel extends JPanel{
	private WaitingRoomPanel waitingRoomPanel;
	
	private JList<String> userList;
	public DefaultListModel<String> roomUserModel;
	
	private JTextPane textArea;
	private JTextField textInput;
	private JButton sendBtn;
	public JButton putBtn;
	public JButton returnBtn;
	public JButton abstentionBtn;
	
	public ChatPanel(WaitingRoomPanel waitingRoomPanel) {
		this.waitingRoomPanel = waitingRoomPanel;
		this.setLayout(null);
		this.setBackground(Color.LIGHT_GRAY);
		
		// 접속자 리스트
			roomUserModel = new DefaultListModel<>();
			userList = new JList<String>(roomUserModel);
			userList.setBounds(5, 5, 315, 230);
			this.add(userList);
			
			// 채팅창
			textArea = new JTextPane();
			textArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
			textArea.setBounds(5, 300, 315, 270);
			this.add(textArea);
			
			textInput = new JTextField();
			textInput.setBounds(5, 575, 250, 25);
			this.add(textInput);
				
			sendBtn = new JButton("전송");
			sendBtn.setBounds(257, 573, 65, 30);
			this.add(sendBtn);
			
			ChatMessageSendAction chatMessageSendAction = new ChatMessageSendAction();
			textInput.addActionListener(chatMessageSendAction);
			sendBtn.addActionListener(chatMessageSendAction);
			
			// 버튼 3개
			putBtn = new JButton("착수");
			putBtn.setBounds(20, 250, 80, 40);
			putBtn.setEnabled(false);
			putBtn.addActionListener(new PutAction());
			this.add(putBtn);
			
			returnBtn = new JButton("무르기");
			returnBtn.setBounds(120, 250, 80, 40);
			returnBtn.setEnabled(false);
			returnBtn.addActionListener(new ReturnRequestAction());
			this.add(returnBtn);
			
			abstentionBtn = new JButton("기권");
			abstentionBtn.setBounds(220, 250, 80, 40);
			abstentionBtn.setEnabled(false);
			abstentionBtn.addActionListener(new AbstentionRequestAction());
			this.add(abstentionBtn);
	}
	
	// 채팅창 좌측에 메시지 출력
	public void appendChatMessageLeft(String userName, String msg) {
		msg = msg.trim(); // 앞 뒤 공백 제거
		String str = String.format("[%s] %s", userName, msg);
		int len = textArea.getDocument().getLength();
		StyledDocument doc = textArea.getStyledDocument();
		SimpleAttributeSet left = new SimpleAttributeSet();
		StyleConstants.setAlignment(left, StyleConstants.ALIGN_LEFT);
		StyleConstants.setForeground(left, Color.BLACK);
		doc.setParagraphAttributes(doc.getLength(), 1, left, false);
		try {
			doc.insertString(doc.getLength(), str+"\n", left );
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		len = textArea.getDocument().getLength();
		textArea.setCaretPosition(len);
	}
	// 채팅창 우측에 메시지 출력
	public void appendChatMessageRight(String msg) {
		msg = msg.trim(); // 앞 뒤 공백 제거
		StyledDocument doc = textArea.getStyledDocument();
		SimpleAttributeSet right = new SimpleAttributeSet();
		StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);
		doc.setParagraphAttributes(doc.getLength(), 1, right, false);
		try {
			doc.insertString(doc.getLength(), msg + "\n", right);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		int len = textArea.getDocument().getLength();
		textArea.setCaretPosition(len);
	}
	
	// 착수 버튼 클릭 했을 때 waitingRoomPanel에게 좌표 전달 해달라고 요청하는 액션 리스너
	class PutAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			waitingRoomPanel.sendMousePoint();
		}
	}
	
	// 엔터 / 전송 버튼 클릭 했을 때 채팅 전송하는 액션 리스너
	class ChatMessageSendAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == sendBtn || e.getSource() == textInput) {
				String msg = null;
				msg = textInput.getText();
				appendChatMessageRight(msg); // 채팅창 우측에 메시지 출력
				waitingRoomPanel.sendChatMessage(msg); // 메시지 전송
				textInput.setText(""); // 입력창 초기화
				textInput.requestFocus(); // 포커스 주기
			}
		}
	}
	
	// 무르기 요청 전송하는 액션 리스너
	class ReturnRequestAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			waitingRoomPanel.returnRequest();
		}
	}
	
	// 항복 요청 전송하는 액션 리스너
	class AbstentionRequestAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			int response = JOptionPane.showConfirmDialog(waitingRoomPanel.mainFrame, "기권 하시겠습니까?","기권", JOptionPane.YES_NO_OPTION);
			if (response == JOptionPane.YES_OPTION) { // 기권
				ChatMsg chatMsg = new ChatMsg(waitingRoomPanel.userName, "320", "abstention");
				chatMsg.roomId = waitingRoomPanel.roomId;
				waitingRoomPanel.sendObject(chatMsg);
			} else { // 기권 취소
				
			}
		}
	}
}
