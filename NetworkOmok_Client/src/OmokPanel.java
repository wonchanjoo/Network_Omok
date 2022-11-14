import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.PointerInfo;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class OmokPanel extends JPanel {
	private Font font;
	
	public OmokPanel(WaitingRoomPanel waitingRoomPanel) {
		this.setLayout(null);
		this.setBackground(Color.WHITE);
		font = new Font("솔뫼 김대건 Medium", Font.PLAIN, 20);
		
		// 오목판
		ImageIcon omokTableImg = new ImageIcon("images\\오목판.png");
		JLabel omokTableLabel = new JLabel(omokTableImg);
		omokTableLabel.setBounds(25, 100, 500, 500);
		this.add(omokTableLabel);
		
		// 흑돌 Player1
		ImageIcon icon = new ImageIcon("images\\흑돌.png");
		Image img = icon.getImage();
		Image resizeImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
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
		
	}
}
