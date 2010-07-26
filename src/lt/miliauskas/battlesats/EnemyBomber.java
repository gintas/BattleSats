package lt.miliauskas.battlesats;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

public class EnemyBomber extends Enemy {

	protected RectF ovalFrame = new RectF();
	protected Paint bomberPaint = new Paint();

	public EnemyBomber(BattleThread thread, float mass, PointF position,
			PointF velocity) {
		super(thread, mass, position, velocity);
		bomberPaint.setARGB(255, 128, 0, 128);
	}

	@Override
	public void draw(Canvas canvas) {
		ovalFrame.set(
				position.x - BattleSats.BOMBER_WIDTH, position.y - BattleSats.BOMBER_HEIGHT,
				position.x + BattleSats.BOMBER_WIDTH, position.y + BattleSats.BOMBER_HEIGHT);
		canvas.drawOval(ovalFrame, bomberPaint);
	}

}
