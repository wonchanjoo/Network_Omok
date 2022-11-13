import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextPane;

public class ChatPanel extends JPanel{
	private JList<String> userList;
	private JTextPane textArea;
	
	public ChatPanel() {
		this.setLayout(null);
		this.setBackground(Color.LIGHT_GRAY);
		
		// 접속자 리스트
		userList = new JList<String>();
		userList.setBounds(5, 5, 315, 230);
		this.add(userList);
		
		// 채팅창
		textArea = new JTextPane();
		textArea.setBounds(5, 305, 315, 300);
		this.add(textArea);
		
		// 버튼 3개
		JButton putBtn = new JButton("착수");
		putBtn.setBounds(20, 250, 80, 40);
		this.add(putBtn);
		
		JButton returnBtn = new JButton("무르기");
		returnBtn.setBounds(120, 250, 80, 40);
		this.add(returnBtn);
		
		JButton abstentionBtn = new JButton("기권");
		abstentionBtn.setBounds(220, 250, 80, 40);
		this.add(abstentionBtn);
	}
}
