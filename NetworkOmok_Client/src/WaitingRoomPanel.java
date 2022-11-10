import java.awt.CardLayout;
import java.awt.Container;

import javax.swing.JPanel;

public class WaitingRoomPanel extends JPanel {
	private Container container;
	private CardLayout cardLayout;
	
	public WaitingRoomPanel(Container container) {
		this.container = container;
		this.cardLayout = (CardLayout) container.getLayout();
		
	}
}
