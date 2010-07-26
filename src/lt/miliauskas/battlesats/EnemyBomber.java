package lt.miliauskas.battlesats;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

public class EnemyBomber extends Enemy {

	protected RectF ovalFrame = new RectF();
	protected Paint bomberPaint = new Paint();
	protected int millisSinceLastBomb = 0;
	protected Random random = new Random();

	public EnemyBomber(BattleThread thread, PointF position, PointF velocity) {
		super(thread, position, velocity);
		bomberPaint.setARGB(255, 128, 0, 128);
		health = BattleSats.BOMBER_HEALTH;
	}

	@Override
	public void updatePosition(long elapsed) {
		super.updatePosition(elapsed);
		
		millisSinceLastBomb += elapsed;
		if (millisSinceLastBomb > 3000) {
			float dx = random.nextFloat() * 10.0f;
			float dy = random.nextFloat() * 10.0f;
			thread.addFlier(new EnemyBomb(thread, 1.0f, new PointF(position.x, position.y),
					new PointF(-position.x / 20.0f + dx, -position.y / 20.0f + dy)));
			// TODO: randomize velocity a little
			millisSinceLastBomb = 0;
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
