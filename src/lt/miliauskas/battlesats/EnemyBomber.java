package lt.miliauskas.battlesats;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

public class EnemyBomber extends Enemy {

	protected RectF ovalFrame = new RectF();
	protected Paint bomberPaint = new Paint();
	protected int millisSinceLastBomb = 0;
	protected Random random = new Random();

	public EnemyBomber(BattleThread thread, PointF position, PointF velocity) {
		super(thread, position, velocity);
		bomberPaint.setAntiAlias(true);
		bomberPaint.setARGB(255, 128, 0, 128);
		health = BattleSats.BOMBER_HEALTH;
	}

	@Override
	public void updatePosition(long elapsed) {
		super.updatePosition(elapsed);
		
		millisSinceLastBomb += elapsed;
		if (millisSinceLastBomb > BattleSats.BOMBER_INTERVAL) {
			float dx = random.nextFloat() * 6.0f - 3.0f;
			float dy = random.nextFloat() * 6.0f - 3.0f;
			thread.addFlier(new EnemyBomb(thread, 1.0f, new PointF(position.x, position.y),
					new PointF(-position.x / 25.0f + dx, -position.y / 25.0f + dy)));
			millisSinceLastBomb -= BattleSats.BOMBER_INTERVAL;
		}
	}

	@Override
	public void draw(Canvas canvas) {
		ovalFrame.set(
				position.x - BattleSats.BOMBER_WIDTH, position.y - BattleSats.BOMBER_HEIGHT,
				position.x + BattleSats.BOMBER_WIDTH, position.y + BattleSats.BOMBER_HEIGHT);
		canvas.drawOval(ovalFrame, bomberPaint);
	}

}
