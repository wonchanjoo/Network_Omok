import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class OmokPanel extends JPanel {
	private WaitingRoomPanel waitingRoomPanel;
	
	private boolean isBlack = false;
	private boolean status = false;
	
	private List<JLabel> blacks = new ArrayList<>();
	private List<JLabel> whites = new ArrayList<>();
	
	
	private ImageIcon icon;
	private Image img;
	private Image resizeImg;
	private ImageIcon blackIcon;
	private Image blackImg;
	private Image resizeBlackImg;
	private ImageIcon whiteIcon;
	private Image whiteImg;
	private Image resizeWhiteImg;
	private Font font;
	
	public OmokPanel(WaitingRoomPanel waitingRoomPanel) {
		this.waitingRoomPanel = waitingRoomPanel;
		this.setLayout(null);
		this.setBackground(Color.WHITE);
		this.setVisible(true);
		font = new Font("솔뫼 김대건 Medium", Font.PLAIN, 20);
		
		// 바둘돌 Lable 생성
		blackIcon = new ImageIcon("images\\흑돌.png");
		blackImg = blackIcon.getImage();
		resizeBlackImg = blackImg.getScaledInstance(27, 27, Image.SCALE_SMOOTH);
		
		whiteIcon = new ImageIcon("images\\백돌.png");
		whiteImg = whiteIcon.getImage();
		resizeWhiteImg = whiteImg.getScaledInstance(27, 27, Image.SCALE_SMOOTH);
		
		// 흑돌 Player1
		icon = new ImageIcon("images\\흑돌.png");
		img = icon.getImage();
		resizeImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
		ImageIcon blackStoneImg = new ImageIcon(resizeImg);
		JLabel blackStoneLabel = new JLabel(blackStoneImg);
		blackStoneLabel.setBounds(40, 30, 50, 50);
		this.add(blackStoneLabel);
		
		JLabel blackPlayerName = new JLabel("player1");
		blackPlayerName.setFont(font);
		blackPlayerName.setBounds(100, 30, 100, 50);
		this.add(blackPlayerName);
		
		// 백돌 Player2
		icon = new ImageIcon("images\\백돌.png");
		img = icon.getImage();
		resizeImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
		ImageIcon whiteStoneImg = new ImageIcon(resizeImg);
		JLabel whiteStoneLabel = new JLabel(whiteStoneImg);
		whiteStoneLabel.setBounds(330, 30, 50, 50);
		this.add(whiteStoneLabel);
		
		JLabel whitePlayerName = new JLabel("player2");
		whitePlayerName.setFont(font);
		whitePlayerName.setBounds(390, 30, 100, 50);
		this.add(whitePlayerName);
		
		
		this.addMouseListener(new omokTableClickListener());
	}
	
	/* --------------- Getter / Setter --------------- */
	public boolean getIsBlack() {
		return isBlack;
	}
	public void setIsBlack(boolean isBlack) {
		this.isBlack = isBlack;
	}
	public boolean getStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// 오목판 배경에 그리기
		ImageIcon omokTableImg = new ImageIcon("images\\오목판.png");
		g.drawImage(omokTableImg.getImage(), 25, 100, 507, 507, this);
	}
	
	// 서버로부터 전달된 좌표에 흑돌 / 백돌 표시
	public void putStone(int x, int y, boolean isBlack) {
		if(isBlack) {
			JLabel blackStone = new JLabel(new ImageIcon(resizeBlackImg));
			blackStone.setBounds(x, y, 27, 27);
			OmokPanel.this.add(blackStone);
		}
		else {
			JLabel whiteStone = new JLabel(new ImageIcon(resizeWhiteImg));
			whiteStone.setBounds(x, y, 27, 27);
			OmokPanel.this.add(whiteStone);
		}
	}
	
	class omokTableClickListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			super.mousePressed(e);
//			System.out.println(e.getPoint());
//			
//			int x = e.getX();
//			int y = e.getY();
//			if(x < 37 || x > 521)
//				return;
//			if(y < 109 || y > 593)
//				return;
//			
//			x = getStoneX(x);
//			y = getStoneY(y);
//			
//			System.out.println(x + ", " + y);
//			stone.setLocation(x - 13, y - 13);
//			OmokPanel.this.add(stone);
//			OmokPanel.this.repaint();
			// status가 true인 경우에만 마우스 좌표를 전송한다.
			if(status)
				waitingRoomPanel.sendMouseEvent(e, isBlack);
		}
		
		private int getStoneX(int x) {
			int value = (x - 37) / 27;
			int rest = (x - 37) % 27;
			if(rest <= 13)
				return 37 + 27 * value;
			else {
				return 37 + 27 * (value + 1);
			}
		}
		
		private int getStoneY(int y) {
			int value = (y - 110) / 27;
			int rest = (y - 110) % 27;
			if(rest <= 13)
				return 110 + 27 * value;
			else 
				return 110 + 27 * (value + 1);
		}
	}
}
