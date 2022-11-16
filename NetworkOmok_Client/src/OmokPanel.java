import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class OmokPanel extends JPanel {
	public boolean isBlack = true;
	private JLabel stone;
	
	private ImageIcon icon;
	private Image img;
	private Image resizeImg;
	private Font font;
	
	public OmokPanel(WaitingRoomPanel waitingRoomPanel) {
		this.setLayout(null);
		this.setBackground(Color.WHITE);
		this.setVisible(true);
		font = new Font("솔뫼 김대건 Medium", Font.PLAIN, 20);
		
		// 오목판
//		ImageIcon omokTableImg = new ImageIcon("images\\오목판.png");
//		JLabel omokTableLabel = new JLabel(omokTableImg);
//		omokTableLabel.setBounds(25, 100, 500, 500);
//		omokTableLabel.addMouseListener(new omokTableClickListener());
//		this.add(omokTableLabel);
		
		// 바둘돌
		if(isBlack)
			icon = new ImageIcon("images\\흑돌.png");
		else
			icon = new ImageIcon("images\\백돌.png");
		img = icon.getImage();
		resizeImg = img.getScaledInstance(26, 26, Image.SCALE_SMOOTH); // 26 X 26으로 
		stone = new JLabel(new ImageIcon(resizeImg));
		stone.setSize(26, 26);
		
		
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
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		// 오목판 배경에 그리기
		ImageIcon omokTableImg = new ImageIcon("images\\오목판.png");
		g.drawImage(omokTableImg.getImage(), 25, 100, 507, 507, this);
	}
	
	class omokTableClickListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			super.mousePressed(e);
			System.out.println(e.getPoint());
			
			int x = e.getX();
			int y = e.getY();
			if(x < 37 || x > 521)
				return;
			if(y < 109 || y > 593)
				return;
			
			x = getStoneX(x);
			y = getStoneY(y);
			
			System.out.println(x + ", " + y);
			stone.setLocation(x - 13, y - 13);
			OmokPanel.this.add(stone);
			OmokPanel.this.repaint();
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
