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
	
	protected Paint bodyPaint = new Paint();
	protected Paint laserPaint = new Paint();
	
	public LaserSentinel(BattleThread thread, PointF position, PointF velocity) {
		super(thread, position, velocity);
		this.type = TYPE_FRIEND;
		
		bodyPaint.setAntiAlias(true);
		bodyPaint.setARGB(255, (state == STATE_IDLE) ? 120 : 255, 180, 0);
		laserPaint.setAntiAlias(true);
		laserPaint.setARGB(90, 0, 200, 200);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.save();
		canvas.translate(position.x, position.y);
		
		if (target != null) {
			float d_x = target.position.x - position.x;
			float d_y = target.position.y - position.y;
			float angle = (float)Math.atan2(d_x, d_y);
			canvas.rotate(-angle * 180.0f / (float)Math.PI);
			if (state == STATE_FIRING) {
				// Draw laser beam.
				canvas.drawRect(
						-BattleSats.LASER_BEAM_RADIUS, HEIGHT/2,
						BattleSats.LASER_BEAM_RADIUS, PointF.length(d_x, d_y),
						laserPaint);
			}
		}
		
		// Draw body.
		canvas.drawRect(-WIDTH/2, -HEIGHT/2, WIDTH/2, HEIGHT/2, bodyPaint);

		canvas.restore();
	}

	@Override
	public void updatePosition(long elapsed) {
		super.updatePosition(elapsed);
		
		state = STATE_IDLE;
		target = thread.findNearestFlier(position, TYPE_ENEMY);
		if (target != null) {
			float dist = PointF.length(target.position.x - position.x, target.position.y - position.y);
			if (dist <= range) {
				state = STATE_FIRING;
				target.hurt(elapsed * damage / 1000.0f);
			}
		}
	}

}
