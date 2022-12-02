import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
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
	public List<Stone> stoneList = new ArrayList<>();
	public String data = "";
	
	public int currentBoardX;
	public int currentBoardY;
	
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
	
	public void DisplayOmok() {
		System.out.println("[" + this.roomName + "]" + "방의 현재 오목 배열");
		System.out.println("-------------------------------------");
		for(int i=0; i<19; i++) {
			for(int j=0; j<19; j++) {
				System.out.print(board[j][i]);
			}
			System.out.println();
		}
		System.out.println("-------------------------------------");
	}
	
	//오목 승자 판별
	public boolean CheckOmok(int blwh) {
		for(int i=0; i<15; i++) {
			for(int j=0; j<19; j++) {
				if(board[j][i]==blwh && board[j][i+1]==blwh && board[j][i+2]==blwh && board[j][i+3]==blwh && board[j][i+4]==blwh) return true;
			}
		}
		for(int i=0; i<19; i++) {
			for(int j=0; j<15; j++) {
				if(board[j][i]==blwh && board[j+1][i]==blwh && board[j+2][i]==blwh && board[j+3][i]==blwh && board[j+4][i]==blwh) return true;
			}
		}
		for(int i=0; i<15; i++) {
			for(int j=0; j<15; j++) {
				if(board[j][i]==blwh && board[j+1][i+1]==blwh && board[j+2][i+2]==blwh && board[j+3][i+3]==blwh && board[j+4][i+4]==blwh) return true;
			}
		}
		for(int i=0; i<15; i++) {
			for(int j=0; j<15; j++) {
				if(board[j][18-i]==blwh && board[j+1][17-i]==blwh && board[j+2][16-i]==blwh && board[j+3][15-i]==blwh && board[j+4][14-i]==blwh) return true;
			}
		}	
		return false;
	}
	
	public boolean omokGame(Point point, int role) {
		int boardX = (point.x-24)/27;
		int boardY = (point.y-97)/27;
		boolean samsam = false;
		
		if(board[boardX][boardY] != 0) {
			data = "location error";
			return false;
		}
		
		if(role == black) {
			samsam = SamSamRule(boardX, boardY);
		}
		
		if(samsam) {
			data = "삼삼입니다";
			return false;
		}
		
		currentBoardX = boardX;
		currentBoardY = boardY;
		
		if(role == black) {
			board[currentBoardX][currentBoardY] = 1;
			//winner = CheckOmok(1);
		}
		else if(role == white){
			board[currentBoardX][currentBoardY] = 2;
			//winner = CheckOmok(2);
		}
		return true;
	}
	
	public void deletePoint(Point point) {
		for(int i = stoneList.size() - 1; i >= 0; i--)
			if(stoneList.get(i).point == point) {
				stoneList.remove(i);
				break;
			}
		int boardX = (point.x - 24) / 27;
		int boardY = (point.y - 97) / 27;
		board[boardX][boardY] = 0;
	}
	
	public boolean SamSamRule(int boardX, int boardY) {
		int count = 0;
		count += find1(boardX, boardY);
		count += find2(boardX, boardY);
		count += find3(boardX, boardY);
		count += find4(boardX, boardY);
		System.out.println(count);
		if(count >= 2) return true;
		else return false;
	}
	
	//가로줄 검사
	private int find1(int boardX, int boardY) {
		int stone1 = 0;
		int stone2 = 0;
		int allStone = 0;
		int blink1 = 1;
		
		int xx = boardX - 1;
		boolean check = false;
		//왼쪽방향 탐색
		for(int i = xx; i > 0; i--) {
			//흰돌을 만나면 탐색 중지
			if(board[i][boardY] == 2) break;
			//흑돌이면
			if(board[i][boardY] == 1) {
				check = false;
				stone1++;
			}
			//빈 공간이면
			if(board[i][boardY] == 0) {
				if(check == false) {
					check = true;
				}
				else {
					blink1++;
					break;
				}
				
				if(blink1 == 1) blink1--;
				else break;
			}
			
		}
		
		//오른쪽방향 탐색
		xx = boardX + 1;
		int blink2 = blink1;
		if(blink1 == 1) blink1 = 0;
		check = false;
		for(int i = xx; i < 19; i++) {
			//흰돌을 만나면 탐색 중지
			if(board[i][boardY] == 2) break;
			//흑돌이면
			if(board[i][boardY] == 1) {
				check = false;
				stone2++;
			}
			//빈 공간이면
			if(board[i][boardY] == 0) {
				if(check == false) {
					check = true;
				}
				else {
					blink2++;
					break;
				}
				
				if(blink2 == 1) blink2--;
				else break;
			}
			
		}
		
		
		allStone = stone1 + stone2;
		if(allStone != 2) return 0;
		
		int left = stone1 + blink1;
		int right = stone2 + blink2;
		
		//놓은 돌을 기준으로 삼삼 가능성이 있는 돌들이 벽에 막힌 경우 삼삼이 아님
		if(boardX - left == 0 || boardX + right == 18) return 0;
		//놓은 돌을 기준으로 삼삼 가능성이 있는 돌들이 흰돌에 막힌 경우 삼삼이 아님
		else if(board[boardX - left - 1][boardY] == 2 || board[boardX + right + 1][boardY] == 2) return 0;
		else return 1;
	}

	private int find2(int boardX, int boardY) {
		int stone1 = 0;
		int stone2 = 0;
		int allStone = 0;
		int blink1 = 1;
		boolean check = false;

		//위쪽방향 탐색
		int yy = boardY - 1;
		for(int i = yy; i > 0; i--) {
			//흰돌을 만나면 탐색 중지
			if(board[boardX][i] == 2) break;
			//흑돌이면
			if(board[boardX][i] == 1) {
				check = false;
				stone1++;
			}
			//빈 공간이면
			if(board[boardX][i] == 0) {
				if(check == false) {
					check = true;
				}
				else {
					blink1++;
					break;
				}
				
				if(blink1 == 1) blink1--;
				else break;
			}
			
		}
		
		yy = boardY + 1;
		int blink2 = blink1;
		if(blink1 == 1) blink1 = 0;
		check = false;
		for(int i = yy; i < 19; i++) {
			//흰돌을 만나면 탐색 중지
			if(board[boardX][i] == 2) break;
			//흑돌이면
			if(board[boardX][i] == 1) {
				check = false;
				stone2++;
			}
			//빈 공간이면
			if(board[boardX][i] == 0) {
				if(check == false) {
					check = true;
				}
				else {
					blink2++;
					break;
				}
				
				if(blink2 == 1) blink2--;
				else break;
			}
			
		}
		
		
		allStone = stone1 + stone2;
		if(allStone != 2) return 0;
		
		int up = stone1 + blink1;
		int down = stone2 + blink2;
		
		//놓은 돌을 기준으로 삼삼 가능성이 있는 돌들이 벽에 막힌 경우 삼삼이 아님
		if(boardY - up == 0 || boardY + down == 18) return 0;
		//놓은 돌을 기준으로 삼삼 가능성이 있는 돌들이 흰돌에 막힌 경우 삼삼이 아님
		else if(board[boardX][boardY - up -1] == 2 || board[boardX][boardY + down +1] == 2) return 0;
		else return 1;
	}
	
	// 왼쪽 위 대각선 방향
	private int find3(int boardX, int boardY) {
		int stone1 = 0;
		int stone2 = 0;
		int allStone = 0;
		int blink1 = 1;
		boolean check = false;

		//왼쪽 위 방향 탐색
		int xx = boardX - 1;
		int yy = boardY - 1;
		while(true) {
			//벽을 만나면 탐색 중지
			if(xx == -1 || yy == -1) break;
			//흰돌을 만나면 탐색 중지
			if(board[xx][yy] == 2) break;
			//흑돌이면
			if(board[xx][yy] == 1) {
				check = false;
				stone1++;
			}
			//빈 공간이면
			if(board[xx][yy] == 0) {
				if(check == false) {
					check = true;
				}
				else {
					blink1++;
					break;
				}
				
				if(blink1 == 1) blink1--;
				else break;
			}
			xx--;
			yy--;
		}
		
		//오른쪽 아래 방향 탐색
		xx = boardX + 1;
		yy = boardY + 1;
		int blink2 = blink1;
		if(blink1 == 1) blink1 = 0;
		check = false;
		while(true) {
			//벽을 만나면 탐색 중지
			if(xx == 19 || yy == 19) break;
			//흰돌을 만나면 탐색 중지
			if(board[xx][yy] == 2) break;
			//흑돌이면
			if(board[xx][yy] == 1) {
				check = false;
				stone2++;
			}
			//빈 공간이면
			if(board[xx][yy] == 0) {
				if(check == false) {
					check = true;
				}
				else {
					blink2++;
					break;
				}
				
				if(blink2 == 1) blink2--;
				else break;
			}
			xx++;
			yy++;
		}
		
		allStone = stone1 + stone2;
		if(allStone != 2) return 0;
		
		int leftUp = stone1 + blink1;
		int rightDown = stone2 + blink2;
		
		//놓은 돌을 기준으로 삼삼 가능성이 있는 돌들이 벽에 막힌 경우 삼삼이 아님
		if(boardX - leftUp == 0 || boardY - leftUp == 0 || boardX + rightDown == 18 || boardY + rightDown == 18) return 0;
		//놓은 돌을 기준으로 삼삼 가능성이 있는 돌들이 흰돌에 막힌 경우 삼삼이 아님
		else if(board[boardX - leftUp - 1][boardY - leftUp - 1] == 2 || board[boardX + rightDown + 1][boardY + rightDown + 1] == 2) return 0;
		else return 1;
	}
	
	//오른쪽 위 대각선 방향 탐색
	private int find4(int boardX, int boardY) {
		int stone1 = 0;
		int stone2 = 0;
		int allStone = 0;
		int blink1 = 1;
		boolean check = false;

		//오른쪽 위 방향 탐색
		int xx = boardX + 1;
		int yy = boardY - 1;
		while(true) {
			//벽을 만나면 탐색 중지
			if(xx == 19 || yy == -1) break;
			//흰돌을 만나면 탐색 중지
			if(board[xx][yy] == 2) break;
			//흑돌이면
			if(board[xx][yy] == 1) {
				check = false;
				stone1++;
			}
			//빈 공간이면
			if(board[xx][yy] == 0) {
				if(check == false) {
					check = true;
				}
				else {
					blink1++;
					break;
				}
				
				if(blink1 == 1) blink1--;
				else break;
			}
			xx++;
			yy--;
		}
		
		//왼쪽 아래 방향 탐색
		xx = boardX - 1;
		yy = boardY + 1;
		int blink2 = blink1;
		if(blink1 == 1) blink1 = 0;
		check = false;
		while(true) {
			//벽을 만나면 탐색 중지
			if(xx == -1 || yy == 19) break;
			//흰돌을 만나면 탐색 중지
			if(board[xx][yy] == 2) break;
			//흑돌이면
			if(board[xx][yy] == 1) {
				check = false;
				stone2++;
			}
			//빈 공간이면
			if(board[xx][yy] == 0) {
				if(check == false) {
					check = true;
				}
				else {
					blink2++;
					break;
				}
				
				if(blink2 == 1) blink2--;
				else break;
			}
			xx--;
			yy++;
		}
		
		
		allStone = stone1 + stone2;
		if(allStone != 2) return 0;
		
		int rightUp = stone1 + blink1;
		int leftDown = stone2 + blink2;
		
		//놓은 돌을 기준으로 삼삼 가능성이 있는 돌들이 벽에 막힌 경우 삼삼이 아님
		if(boardX + rightUp == 18 || boardY - rightUp == 0 || boardX - leftDown == 0 || boardY + leftDown == 18) return 0;
		//놓은 돌을 기준으로 삼삼 가능성이 있는 돌들이 흰돌에 막힌 경우 삼삼이 아님
		else if(board[boardX + rightUp + 1][boardY - rightUp - 1] == 2 || board[boardX - leftDown - 1][boardY + leftDown + 1] == 2) return 0;
		else return 1;
	}
}
