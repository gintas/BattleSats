package lt.miliauskas.battlesats;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

public class EnemyBomb extends Enemy {

	public EnemyBomb(BattleThread thread, float mass, PointF position,
			PointF velocity) {
		super(thread, mass, position, velocity);
	}

	@Override
	public void draw(Canvas canvas) {
		PointF adjCoords = displayPosition();
		Paint p = new Paint();
		p.setARGB(255, 255, 0, 0);
		canvas.drawCircle(adjCoords.x, adjCoords.y, 5.0f, p);
	}

}
