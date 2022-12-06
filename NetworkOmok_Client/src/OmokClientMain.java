import java.awt.CardLayout;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.MenuBar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class OmokClientMain extends JFrame {
	private CardLayout cardLayout;
	private Container container;
	
	private StartPanel startPanel;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					OmokClientMain mainFrameClientMain = new OmokClientMain();
					mainFrameClientMain.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		});
	}
	
	public OmokClientMain() {
		this.setTitle("오목");
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		cardLayout = new CardLayout();
		container = this.getContentPane();
		container.setLayout(cardLayout);
		
		startPanel = new StartPanel(container, this);
		
		container.add(startPanel, "startPanel");
		
		this.setSize(900, 650);
		this.setLocation(500, 150);
		this.setResizable(false); // 사이즈를 조절할 수 없도록
	}
}
