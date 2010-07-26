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
			float angle = (float)Math.atan2(
					(double)(target.getPosition().x - position.x),
					(double)(target.getPosition().y - position.y));
			canvas.rotate(angle * 180.0f / (float)Math.PI);
		}
		canvas.drawRect(-WIDTH/2, -HEIGHT/2, WIDTH/2, HEIGHT/2, p);
		canvas.restore();
	}

	@Override
	public void updatePosition(double elapsed) {
		// TODO Auto-generated method stub
		super.updatePosition(elapsed);
		
		state = STATE_IDLE;
		target = thread.findNearestEnemy(position);
		if (target != null) {
			PointF target_pos = target.getPosition();
			float dist = new PointF(target_pos.x - position.x, target_pos.y - position.y).length();
			if (dist <= range) {
				state = STATE_FIRING;
				target.hurt(elapsed * damage);
			}
		}
	}

}
