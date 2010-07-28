package lt.miliauskas.battlesats;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

public class EnemyBomb extends Enemy {
	
	protected Paint bombPaint = new Paint();
	
	public EnemyBomb(BattleThread thread, float mass, PointF position,
			PointF velocity) {
		super(thread, position, velocity);
		health = BattleSats.BOMB_HEALTH;
		bombPaint.setAntiAlias(true);
		bombPaint.setARGB(255, 255, 0, 0);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawCircle(position.x, position.y, BattleSats.BOMB_RADIUS, bombPaint);
	}

	@Override
	public void updatePosition(long elapsed) {
		super.updatePosition(elapsed);
		if (position.length() < BattleSats.EARTH_RADIUS) {
			thread.hurtEarth(BattleSats.BOMB_DAMAGE);
		}
	}

}
