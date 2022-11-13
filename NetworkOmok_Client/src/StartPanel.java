import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class StartPanel extends JPanel {
	private Container container;
	private CardLayout cardLayout;
	
	private WaitingRoomPanel waitingRoomPanel;
	
	private JTextField ipInput;
	private JTextField portInput;
	private JTextField userNameInput;
	private JButton startBtn;
	
	private Font font;
	
	public StartPanel(Container container) {
		this.setSize(1000, 600);
		this.container = container;
		this.cardLayout = (CardLayout) container.getLayout();
		this.setLayout(null);
		
		// "오목" 로고 JLabel
		JLabel omokLogoLabel = new JLabel("오목");
		font  = new Font("솔뫼 김대건 Medium", Font.PLAIN, 50);
		omokLogoLabel.setFont(font);
		omokLogoLabel.setBounds(400, 60, 100, 90);
		this.add(omokLogoLabel);
		
		// IP 입력
		JLabel ipLabel = new JLabel("IP Address");
		font = new Font("Gothic", Font.PLAIN, 15);
		ipLabel.setFont(font);
		ipLabel.setBounds(330, 190, 85, 30);
		this.add(ipLabel);
		
		ipInput = new JTextField();
		ipInput.setHorizontalAlignment(SwingConstants.CENTER);
		ipInput.setText("127.0.0.1");
		ipInput.setColumns(10);
		ipInput.setBounds(450, 190, 120, 33);
		this.add(ipInput);
		
		// Port 입력
		JLabel portLabel = new JLabel("Port Number");
		portLabel.setFont(font);
		portLabel.setBounds(330, 240, 85, 30);
		this.add(portLabel);
		
		portInput = new JTextField();
		portInput.setHorizontalAlignment(SwingConstants.CENTER);
		portInput.setText("30000");
		portInput.setColumns(10);
		portInput.setBounds(450, 240, 120, 30);
		this.add(portInput);
		
		// user name 입력
		JLabel userNameLabel = new JLabel("이름을 입력하세요.");
		font = new Font("솔뫼 김대건 Medium", Font.PLAIN, 20);
		userNameLabel.setFont(font);
		userNameLabel.setBounds(380, 340, 160, 40);
		this.add(userNameLabel);
		
		userNameInput = new JTextField();
		userNameInput.setHorizontalAlignment(SwingConstants.CENTER);
		userNameInput.setBounds(320, 390, 200, 40);
		this.add(userNameInput);

		// 시작 버튼
		startBtn = new JButton("시작");
		startBtn.setBounds(530, 390, 70, 40);
		startBtn.addActionListener(new StartBtnAction());
		this.add(startBtn);
		
	}
	
	class StartBtnAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String userName = userNameInput.getText().trim();
			if(userName.equals("")) { // 이름 입력이 안 됐으면
				JOptionPane.showMessageDialog(null, "이름을 입력하세요!", "Message", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String ip_addr = ipInput.getText().trim();
			String port_no = portInput.getText().trim();
			
			waitingRoomPanel = new WaitingRoomPanel(container, userName, ip_addr, port_no);
			container.add(waitingRoomPanel, "waitingRoomPanel");
			cardLayout.show(container, "waitingRoomPanel"); // 대기실 Panel로 변경
		}
	}
}
