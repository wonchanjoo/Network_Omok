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
	public OmokPanel omokPanel;
	public ChatPanel chatPanel;
	
	public GamePanel(Container container, WaitingRoomPanel waitingRoomPanel) {
		this.waitingRoomPanel = waitingRoomPanel;
		this.container = container;
		this.cardLayout = (CardLayout) container.getLayout();
		
		this.setSize(900, 650);
		this.setLayout(new BorderLayout());
		this.setBackground(Color.YELLOW);
		
		splitPane();
	}
	
	public void splitPane() {
		JSplitPane omokPane = new JSplitPane();
		this.add(omokPane, BorderLayout.CENTER);
		omokPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		omokPane.setDividerLocation(550); // 550을 기준으로 나눈다(프레임 사이즈 가로 900)
		omokPane.setEnabled(false); // 기준을 움직일 수 없도록

		
		omokPanel = new OmokPanel(waitingRoomPanel);
		chatPanel = new ChatPanel(waitingRoomPanel);
		omokPane.setLeftComponent(omokPanel); // 왼쪽에 OmokPanel
		omokPane.setRightComponent(chatPanel); // 오른쪽에 ChatPanel
		// waitingRoomPanel에서 소켓 통신을 하기 때문에 omokPanel과 chatPanel을 전달해줘야 된다
		// ex) 서버로부터 메시지 오면 채팅창에 append text, 바둑돌 놓기
		waitingRoomPanel.setOmokPanel(omokPanel);
		waitingRoomPanel.setChatPanel(chatPanel);
	}
}
