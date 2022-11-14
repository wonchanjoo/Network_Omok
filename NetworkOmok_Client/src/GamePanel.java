import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.JPanel;
import javax.swing.JSplitPane;


public class GamePanel extends JPanel {
	private Container container;
	private CardLayout cardLayout;
	private WaitingRoomPanel waitingRoomPanel;
	
	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	
	public GamePanel(Container container, WaitingRoomPanel waitingRoomPanel) {
		this.waitingRoomPanel = waitingRoomPanel;
		
		this.setSize(900, 650);
		this.setLayout(new BorderLayout());
		this.setBackground(Color.YELLOW);
		this.container = container;
		this.cardLayout = (CardLayout) container.getLayout();
		
		splitPane();
	}
	
	public void splitPane() {
		JSplitPane omokPane = new JSplitPane();
		this.add(omokPane, BorderLayout.CENTER);
		omokPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		omokPane.setDividerLocation(550); // 550을 기준으로 나눈다(프레임 사이즈 가로 900)
		omokPane.setEnabled(false); // 기준을 움직일 수 없도록

		omokPane.setLeftComponent(new OmokPanel(waitingRoomPanel)); // 왼쪽에 OmokPanel
		omokPane.setRightComponent(new ChatPanel(waitingRoomPanel)); // 오른쪽에 ChatPanel
		
//		omokPane.addMouseMotionListener(new MouseAdapter() { // 오목판 좌표 +-13
//			@Override
//			public void mouseMoved(MouseEvent e) {
//				System.out.println(e.getX() + " " + e.getY());
//			}
//		});
	}
}
