package lt.miliauskas.battlesats;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

public abstract class Flier {

	/** Mass of the object */
	protected double mass = 1.0f;
	
	/** Position */
	protected PointF position;
	
	/** Velocity */
	protected PointF velocity;
	
	protected BattleThread thread;
	
	public Flier(BattleThread thread, float mass, PointF position, PointF velocity) {
		this.thread = thread;
		this.mass = mass;
		this.position = position;
		this.velocity = velocity;
	}
	
	public void updatePosition() {
		/* Update satellite coordinates */
		// TODO: elapsed
		float r2 = (position.x * position.x + position.y * position.y);
		float f = (float)(BattleSats.MASS_G * BattleSats.MASS_EARTH * mass / r2);
		float r = (float)Math.sqrt(r2);
		PointF dv = new PointF((position.x / r * f), (position.y / r * f));
		dv.negate();
		velocity.offset(dv.x, dv.y);
		position.offset(velocity.x, velocity.y);
		
		if (position.length() < thread.earthRadius) {
			destroy();
		}
	}
	
	public void destroy() {
		thread.removeFlier(this);
	}
	
	protected PointF displayPosition() {
		return thread.toDisplayCoords(position); 
	}
	
	public boolean isEnemy() {
		return false;
	}
	
	public abstract void draw(Canvas canvas);

}
