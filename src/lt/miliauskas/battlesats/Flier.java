package lt.miliauskas.battlesats;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.FloatMath;
import android.util.Log;

public abstract class Flier {

	/** Mass of the object */
	protected float mass = 1.0f;
	
	/** Position */
	protected PointF position;
	
	/** Velocity */
	protected PointF velocity;
	
	protected float health = 1.0f;
	
	protected BattleThread thread;
	
	public PointF getPosition() {
		return new PointF(position.x, position.y);
	}

	public Flier(BattleThread thread, float mass, PointF position, PointF velocity) {
		this.thread = thread;
		this.mass = mass;
		this.position = position;
		this.velocity = velocity;
	}
	
	public void updatePosition(long elapsed) {
		/* Update satellite coordinates */
//		for (int i = 0; i < elapsed; i++) {
			float r = position.length();
			float m = BattleSats.MASS_G * BattleSats.MASS_EARTH / (r * r);
			PointF dv = new PointF((m * position.x / r), (m * position.y / r));
			dv.negate();
			velocity.offset(dv.x, dv.y);
			position.offset(velocity.x * elapsed / 1000.0f, velocity.y * elapsed / 1000.0f);
		
			if (position.length() < thread.earthRadius) {
				destroy();
//				break;
			}
//		}
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
	
	public void hurt(double damage) {
		health -= damage;
		if (health < 0.0f)
			destroy();
	}
	
	public abstract void draw(Canvas canvas);

}
