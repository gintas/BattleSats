package lt.miliauskas.battlesats;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

public class LaserSentinel extends Flier {
	
	public static final int STATE_IDLE = 0;
	public static final int STATE_FIRING = 1;
	
	protected int HEIGHT = 10;
	protected int WIDTH = 5;
	
	protected float range = BattleSats.LASER_RANGE;
	protected float damage = BattleSats.LASER_DAMAGE;
	protected Flier target = null;
	
	protected int state = STATE_IDLE;
	
	public LaserSentinel(BattleThread thread, float mass, PointF position,
			PointF velocity) {
		super(thread, mass, position, velocity);
	}

	@Override
	public void draw(Canvas canvas) {
		PointF adjCoords = displayPosition();
		Paint p = new Paint();
		p.setARGB(255, (state == STATE_IDLE) ? 120 : 255, 180, 0);
		canvas.save();
		canvas.translate(adjCoords.x, adjCoords.y);
		
		if (target != null) {
			PointF targetPos = target.getPosition();
			PointF d = new PointF(targetPos.x - position.x, targetPos.y - position.y);
			float angle = (float)Math.atan2(d.x, d.y);
			canvas.rotate(angle * 180.0f / (float)Math.PI);
			if (state == STATE_FIRING) {
				// Draw laser beam.
				Paint laserPaint = new Paint();
				laserPaint.setARGB(128, 0, 200, 200);
				canvas.drawRect(-2, -HEIGHT/2, 2, -d.length(), laserPaint);
			}
		}
		canvas.drawRect(-WIDTH/2, -HEIGHT/2, WIDTH/2, HEIGHT/2, p);
		canvas.restore();
	}

	@Override
	public void updatePosition(long elapsed) {
		super.updatePosition(elapsed);
		
		state = STATE_IDLE;
		target = thread.findNearestEnemy(position);
		if (target != null) {
			PointF target_pos = target.getPosition();
			float dist = new PointF(target_pos.x - position.x, target_pos.y - position.y).length();
			if (dist <= range) {
				state = STATE_FIRING;
				target.hurt(elapsed * damage / 1000.0f);
			}
		}
	}

}
