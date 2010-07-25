package lt.miliauskas.battlesats;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

public class LaserSentinel extends Flier {
	
	protected int HEIGHT = 10;
	protected int WIDTH = 5;
	
	public LaserSentinel(BattleThread thread, float mass, PointF position,
			PointF velocity) {
		super(thread, mass, position, velocity);
	}

	@Override
	public void draw(Canvas canvas) {
		PointF adjCoords = displayPosition();
		Paint p = new Paint();
		p.setARGB(255, 120, 180, 0);
		canvas.save();
		canvas.translate(adjCoords.x, adjCoords.y);
		canvas.rotate(35);
		canvas.drawRect(-WIDTH/2, -HEIGHT/2, WIDTH/2, HEIGHT/2, p);
		canvas.restore();
	}

}
