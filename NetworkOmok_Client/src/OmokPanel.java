import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class OmokPanel extends JPanel {
	private WaitingRoomPanel waitingRoomPanel;
	private Font font;
	public JLabel bStone; // 클릭할 때 보여주기용 바둑돌
	public JLabel wStone; // 클릭할 때 보여주기용 바둑돌
	public Point point;
	public JLabel blackPlayerName;
	public JLabel whitePlayerName;
	
	private boolean isBlack = false;
	private boolean status = false;
	public JLabel oldStone;
	
	private ImageIcon icon;
	private Image img;
	private Image resizeImg;
	private ImageIcon blackIcon;
	private Image blackImg;
	
	private Image resizeBlackImg;
	private ImageIcon whiteIcon;
	private Image whiteImg;
	private Image resizeWhiteImg;
	
	
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
		
		bStone = new JLabel(new ImageIcon(resizeBlackImg));
		wStone = new JLabel(new ImageIcon(resizeWhiteImg));
		bStone.setSize(27, 27);
		wStone.setSize(27, 27);
		
		
		// 흑돌 Player1
		icon = new ImageIcon("images\\흑돌.png");
		img = icon.getImage();
		resizeImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
		ImageIcon blackStoneImg = new ImageIcon(resizeImg);
		JLabel blackStoneLabel = new JLabel(blackStoneImg);
		blackStoneLabel.setBounds(40, 30, 50, 50);
		this.add(blackStoneLabel);
		
		blackPlayerName = new JLabel("player1");
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
		
		whitePlayerName = new JLabel("player2");
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
		OmokPanel.this.repaint();
	}
	
	// 클릭한 위치에 바둑돌이 보이도록
	class omokTableClickListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			super.mousePressed(e);
			
			// status가 true일 때만 클릭할 수 있다.
			if(!status) return; 
			
			int x = e.getX();
			int y = e.getY();
			if(x < 37 || x > 521)
				return;
			if(y < 109 || y > 593)
				return;
			
			// 최종 좌표
			x = getStoneX(x);
			y = getStoneY(y);
			
			if(isBlack) {
				bStone.setLocation(x - 13, y - 13);
				OmokPanel.this.add(bStone);
			}
			else {
				wStone.setLocation(x - 13, y - 13);
				OmokPanel.this.add(wStone);
			}
			
			point = new Point(x - 13, y - 13);
			OmokPanel.this.repaint();
		}
		
		// 오목판 x 좌표 계산
		private int getStoneX(int x) {
			int value = (x - 37) / 27;
			int rest = (x - 37) % 27;
			if(rest <= 13)
				return 37 + 27 * value;
			else {
				return 37 + 27 * (value + 1);
			}
		}
		
		// 오목판 y 좌표 계산
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
