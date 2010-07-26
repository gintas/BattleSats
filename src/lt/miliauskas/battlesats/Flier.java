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
	public PointF position;
	
	/** Velocity */
	protected PointF velocity;
	
	protected float health = 1.0f;
	
	protected BattleThread thread;

	public Flier(BattleThread thread, float mass, PointF position, PointF velocity) {
		this.thread = thread;
		this.mass = mass;
		this.position = position;
		this.velocity = velocity;
	}
	
	public void updatePosition(long elapsed) {
		float r = position.length();
		float m = BattleSats.MASS_G * BattleSats.MASS_EARTH / (r * r * r);
		float dv_x = m * position.x;
		float dv_y = m * position.y;
		velocity.offset(-dv_x, -dv_y);
		position.offset(velocity.x * elapsed / 1000.0f, velocity.y * elapsed / 1000.0f);
	
		if (position.length() < thread.earthRadius) {
			destroy();
		}
	}
	
	public void destroy() {
		thread.removeFlier(this);
	}
	
	public void hurt(double damage) {
		health -= damage;
		if (health < 0.0f)
			destroy();
	}
	
	public abstract void draw(Canvas canvas);
}