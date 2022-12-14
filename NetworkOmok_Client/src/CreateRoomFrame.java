import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerNumberModel;

public class CreateRoomFrame extends JFrame{
	private WaitingRoomPanel waitingRoomPanel;
	private Font font;
	private Container container;
	private JTextField inputRoomName;
	private JCheckBox passwordCheckBox;
	private JTextField inputPassword;
	private JSpinner inputPeopleCount;
	
	public CreateRoomFrame(WaitingRoomPanel waitingRoomPanel) {
		this.waitingRoomPanel = waitingRoomPanel;
		this.setBounds(0, 0, 500, 400);
		Dimension frameSize = this.getSize();
		Dimension windowSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((windowSize.width - frameSize.width) / 2, (windowSize.height - frameSize.height) / 2);
	
		container = this.getContentPane();
		container.setLayout(null);
		
		font = new Font("AppleSDGothicNeoM00", Font.PLAIN, 15);
		
		// 방 이름
		JLabel roomName = new JLabel("방 이름");
		roomName.setFont(new Font("굴림", Font.BOLD, 15));
		roomName.setBounds(110, 50, 60, 30);
		container.add(roomName);
		
		inputRoomName = new JTextField();
		inputRoomName.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
		inputRoomName.setBounds(240, 50, 150, 30);
		container.add(inputRoomName);
		
		// 비밀번호
		JLabel password = new JLabel("비밀번호");
		password.setFont(new Font("굴림", Font.BOLD, 15));
		password.setBounds(110, 120, 80, 30);
		container.add(password);
		
		passwordCheckBox = new JCheckBox();
		passwordCheckBox.setBounds(190, 120, 30, 30);
		passwordCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED)
					inputPassword.setEnabled(true);
				else
					inputPassword.setEnabled(false);
			}
		});
		container.add(passwordCheckBox);
		
		inputPassword = new JTextField();
		inputPassword.setBounds(240, 120, 150, 30);
		inputPassword.setEnabled(false);
		container.add(inputPassword);
		
		// 인원 수
		JLabel peopleCount = new JLabel("인원 수");
		peopleCount.setFont(new Font("굴림", Font.BOLD, 15));
		peopleCount.setBounds(110, 190, 60, 30);
		container.add(peopleCount);
		
		int[] arr = new int[20];
		for(int i = 0; i <= 18; i++)
			arr[i] = i + 2;
		SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(2, 2, 20, 1);
		inputPeopleCount = new JSpinner(spinnerNumberModel);
		inputPeopleCount.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
		inputPeopleCount.setBounds(240, 190, 150, 30);
		container.add(inputPeopleCount);
		
		// 방 만들기 버튼
		JButton createBtn = new JButton("만들기");
		createBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
		createBtn.setBounds(220, 300, 80, 30);
		createBtn.addActionListener(new CreaetBtnClick());
		container.add(createBtn);
		
		this.setVisible(true);
	}
	
	class CreaetBtnClick implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String roomNameString = inputRoomName.getText();
			if(roomNameString.equals("")) {
				JOptionPane.showMessageDialog(null, "방 이름을 입력하세요.", "Message", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			String passwordString = null;
			if(passwordCheckBox.isSelected()) { // 체크박스가 선택되어 있으면
				passwordString = inputPassword.getText();
				if(passwordString.equals("")) {
					JOptionPane.showMessageDialog(null, "비밀번호를 입력하세요.", "Message", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			
			int count = (int) inputPeopleCount.getValue();
			
			waitingRoomPanel.createRoom(roomNameString, passwordString, count);
		}
	}
}
