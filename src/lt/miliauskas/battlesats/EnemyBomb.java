package lt.miliauskas.battlesats;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Vibrator;

public class EnemyBomb extends Enemy {
	
	protected Paint bombPaint = new Paint();
	
	public EnemyBomb(BattleThread thread, float mass, PointF position,
			PointF velocity) {
		super(thread, mass, position, velocity);
		health = BattleSats.BOMB_HEALTH;
		bombPaint.setARGB(255, 255, 0, 0);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawCircle(position.x, position.y, 5.0f, bombPaint);
	}

	@Override
	public void updatePosition(long elapsed) {
		super.updatePosition(elapsed);
		if (position.length() < thread.earthRadius) {
			//thread.vibrate();
			// TODO: decrease earth health
		}
	}

}
