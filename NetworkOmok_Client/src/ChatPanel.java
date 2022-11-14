import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class ChatPanel extends JPanel{
	private WaitingRoomPanel waitingRoomPanel;
	
	private JList<String> userList;
	
	private JTextPane textArea;
	private JTextField textInput;
	private JButton sendBtn;
	
	public ChatPanel(WaitingRoomPanel waitingRoomPanel) {
		this.waitingRoomPanel = waitingRoomPanel;
		this.setLayout(null);
		this.setBackground(Color.LIGHT_GRAY);
		
		// 접속자 리스트
		userList = new JList<String>();
		userList.setBounds(5, 5, 315, 230);
		this.add(userList);
		
		// 채팅창
		textArea = new JTextPane();
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
		JButton putBtn = new JButton("착수");
		putBtn.setBounds(20, 250, 80, 40);
		this.add(putBtn);
		
		JButton returnBtn = new JButton("무르기");
		returnBtn.setBounds(120, 250, 80, 40);
		returnBtn.addActionListener(new ReturnRequestAction());
		this.add(returnBtn);
		
		JButton abstentionBtn = new JButton("기권");
		abstentionBtn.setBounds(220, 250, 80, 40);
		abstentionBtn.addActionListener(new AbstentionRequestAction());
		this.add(abstentionBtn);
	}
	
	// 채팅창 좌측에 메시지 출력
	public void appendChatMessageLeft(String msg) {
		msg = msg.trim(); // 앞 뒤 공백 제거
		int len = textArea.getDocument().getLength();
		StyledDocument doc = textArea.getStyledDocument();
		SimpleAttributeSet left = new SimpleAttributeSet();
		StyleConstants.setAlignment(left, StyleConstants.ALIGN_LEFT);
		StyleConstants.setForeground(left, Color.BLACK);
		doc.setParagraphAttributes(doc.getLength(), 1, left, false);
		try {
			doc.insertString(doc.getLength(), msg+"\n", left );
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
			ChatMsg chatMsg = new ChatMsg(waitingRoomPanel.userName, "302", "return");
			waitingRoomPanel.sendObject(chatMsg);
		}
	}
	
	// 항복 요청 전송하는 액션 리스너
	class AbstentionRequestAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			ChatMsg chatMsg = new ChatMsg(waitingRoomPanel.userName, "305", "abstention");
			waitingRoomPanel.sendObject(chatMsg);
		}
	}
}
