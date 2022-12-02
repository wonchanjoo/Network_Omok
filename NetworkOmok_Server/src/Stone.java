import java.awt.Point;

public class Stone {
	public final int black = 0;
	public final int white = 1;
	
	public int role;
	public Point point;
	
	public Stone(int role, Point point) {
		this.role = role;
		this.point = point;
	}
}