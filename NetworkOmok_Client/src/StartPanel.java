import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class StartPanel extends JPanel {
	private Container container;
	private CardLayout cardLayout;
	
	private JTextField ipInput;
	private JTextField portInput;
	private JTextField userNameInput;
	private JButton startBtn;
	
	private Font font;
	
	public StartPanel(Container container) {
		this.container = container;
		this.cardLayout = (CardLayout) container.getLayout();
		this.setLayout(null);
		
		// "오목" 로고 JLabel
		JLabel omokLogoLabel = new JLabel("오목");
		font  = new Font("솔뫼 김대건 Medium", Font.PLAIN, 50);
		omokLogoLabel.setFont(font);
		omokLogoLabel.setBounds(450, 60, 100, 90);
		this.add(omokLogoLabel);
		
		// IP 입력
		JLabel ipLabel = new JLabel("IP Address");
		font = new Font("Gothic", Font.PLAIN, 15);
		ipLabel.setFont(font);
		ipLabel.setBounds(380, 190, 85, 30);
		this.add(ipLabel);
		
		ipInput = new JTextField();
		ipInput.setHorizontalAlignment(SwingConstants.CENTER);
		ipInput.setText("127.0.0.1");
		ipInput.setColumns(10);
		ipInput.setBounds(500, 190, 120, 33);
		this.add(ipInput);
		
		// Port 입력
		JLabel portLabel = new JLabel("Port Number");
		portLabel.setFont(font);
		portLabel.setBounds(380, 240, 85, 30);
		this.add(portLabel);
		
		portInput = new JTextField();
		portInput.setHorizontalAlignment(SwingConstants.CENTER);
		portInput.setText("30000");
		portInput.setColumns(10);
		portInput.setBounds(500, 240, 120, 30);
		this.add(portInput);
		
		// user name 입력
		JLabel userNameLabel = new JLabel("이름을 입력하세요.");
		font = new Font("솔뫼 김대건 Medium", Font.PLAIN, 20);
		userNameLabel.setFont(font);
		userNameLabel.setBounds(430, 340, 160, 40);
		this.add(userNameLabel);
		
		userNameInput = new JTextField();
		userNameInput.setHorizontalAlignment(SwingConstants.CENTER);
		userNameInput.setBounds(370, 390, 200, 40);
		this.add(userNameInput);

		// 시작 버튼
		startBtn = new JButton("시작");
		startBtn.setBounds(580, 390, 70, 40);
		startBtn.addActionListener(new StartBtnAction());
		this.add(startBtn);
		
	}
	
	class StartBtnAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String userName = userNameInput.getText().trim();
			if(userName.equals("")) { // 이름이 입력이 안 됐으면
				// 다이얼로그 보여주기?
				return;
			}
			String ip = ipInput.getText().trim();
			String port = portInput.getText().trim();
			cardLayout.show(container, "waitingRoomPanel"); // 대기실 Panel로 변경
		}
	}
}
