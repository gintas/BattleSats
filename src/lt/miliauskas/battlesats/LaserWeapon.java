package lt.miliauskas.battlesats;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

public class LaserWeapon {
	
	public static final int STATE_IDLE = 0;
	public static final int STATE_FIRING = 1;	

	protected Flier flier;
	protected float range;
	protected float damagePerSecond; 
	protected float beamRadius;
	protected Flier target = null;
	protected int state = STATE_IDLE;
	
	protected Paint laserPaint = new Paint();	
	
	public LaserWeapon(Flier flier, float range, float damagePerSecond, float beamRadius) {
		this.flier = flier;
		this.range = range;
		this.damagePerSecond = damagePerSecond;
		this.beamRadius = beamRadius;
		laserPaint.setAntiAlias(true);
		laserPaint.setARGB(90, 0, 200, 200);
	}
	
	/*
	 * Rotates canvas to prepare for drawing the laser.
	 * 
	 * Call this before calling draw().
	 * 
	 */
	public void rotateCanvas(Canvas canvas) {
		// XXX Separating rotateCanvas from draw is bug-prone.
		if (target != null) {
			float d_x = target.position.x - flier.position.x;
			float d_y = target.position.y - flier.position.y;
			float angle = (float)Math.atan2(d_x, d_y);
			canvas.rotate(-angle * 180.0f / (float)Math.PI);
		}
	}
	
	public void draw(Canvas canvas) {
		if (state == STATE_FIRING) {
			// Draw laser beam.
			float d_x = target.position.x - flier.position.x;
			float d_y = target.position.y - flier.position.y;
			// TODO: use drawLine with a set stroke width
			canvas.drawRect(
					-beamRadius, 0,
					beamRadius,	PointF.length(d_x, d_y),
					laserPaint);
		}
	}
	
	public void doDamage(long elapsed, int enemyType) {
		state = STATE_IDLE;
		// TODO: try to hold onto current target before looking for a new one
		target = flier.thread.findNearestFlier(flier.position, enemyType);
		if (target != null) {
			float dist = PointF.length(target.position.x - flier.position.x, target.position.y - flier.position.y);
			if (dist <= range) {
				state = STATE_FIRING;
				target.hurt(elapsed * damagePerSecond / 1000.0f);
			}
		}
	}

}