package lt.miliauskas.battlesats;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;

public class EnemyBomber extends Flier {

	protected RectF ovalFrame = new RectF();
	protected Paint bomberPaint = new Paint();
	protected int millisSinceLastBomb = 0;
	protected Random random = new Random();
	protected Flier target = null;
	protected LaserWeapon laser;

	public EnemyBomber(BattleThread thread, PointF position, PointF velocity) {
		super(thread, position, velocity);
		this.type = TYPE_ATTACKER;
		this.laser = new LaserWeapon(this, BattleSats.BOMBER_LASER_RANGE, BattleSats.BOMBER_LASER_DAMAGE, BattleSats.BOMBER_LASER_BEAM_RADIUS);
		bomberPaint.setAntiAlias(true);
		bomberPaint.setARGB(255, 128, 0, 128);
		health = BattleSats.BOMBER_HEALTH;
	}

	@Override
	public void updatePosition(long elapsed) {
		super.updatePosition(elapsed);
		
		laser.doDamage(elapsed, Flier.TYPE_FRIEND);
		
		// Drop bombs.
		millisSinceLastBomb += elapsed;
		if (millisSinceLastBomb > BattleSats.BOMBER_INTERVAL) {
			float dx = (random.nextFloat() - 0.5f) * 6.0f;
			float dy = (random.nextFloat() - 0.5f) * 6.0f;
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

		canvas.save();
		canvas.translate(position.x, position.y);
		laser.rotateCanvas(canvas);
		laser.draw(canvas);
		canvas.restore();
		
	}

}
