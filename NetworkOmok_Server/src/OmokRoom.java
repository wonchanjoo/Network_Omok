import java.awt.Point;
import java.util.Vector;

public class OmokRoom {
	public final int black = 0;
	public final int white = 1;
	
	public String roomName;
	public long roomId;
	public int peopleCount = 0;
	public int status = 0;
	public String password;
	public Vector<String> player = new Vector<>();
	public Vector<String> viewer = new Vector<>();
	public int[][] board = new int[19][19]; // 오목판, 방마다 하나씩 가지고 있어야함
	public String data = "";
	
	public OmokRoom(long roomId, String creater) {
		this.roomId = roomId;
		this.player.add(creater);
		initOmok();
	}
	
	//오목 배열 초기화
	public void initOmok() {
		for(int i=0; i<19; i++) {
			for(int j=0; j<19; j++) {
				board[i][j] = 0;
			}
		}
	}
	
	//오목 승자 판별, static으로 하는 것을 더 추천
	public boolean CheckOmok(int blwh) {
		for(int i=0; i<19; i++) {
			for(int j=0; j<19; j++) {
				if(board[i][j]==blwh && board[i][j+1]==blwh && board[i][j+2]==blwh && board[i][j+3]==blwh && board[i][j+4]==blwh) return true;
				else if(board[i][j]==blwh && board[i+1][j]==blwh && board[i+2][j]==blwh && board[i+3][j]==blwh && board[i+4][j]==blwh) return true;
				else if(board[i][j]==blwh && board[i+1][j+1]==blwh && board[i+2][j+2]==blwh && board[i+3][j+3]==blwh && board[i+4][j+4]==blwh) return true;
				else if(board[i][j]==blwh && board[i+1][j-1]==blwh && board[i+2][j-2]==blwh && board[i+3][j-3]==blwh && board[i+4][j-4]==blwh) return true;
			}
		}
		return false;
	}
	
	public boolean omokGame(Point point, int role) {
		int boardX = (point.x-37)/27;
		int boardY = (point.y-110)/27;
		
		if(board[boardX][boardY] != 0) {
			data = "location error";
			return false;
		}
		
		if(role == black) {
			board[boardX][boardY] = 1;
			//winner = CheckOmok(1);
		}
		else if(role == white){
			board[boardX][boardY] = 2;
			//winner = CheckOmok(2);
		}
		
		return true;
	}

}
