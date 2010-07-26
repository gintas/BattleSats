package lt.miliauskas.battlesats;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.FloatMath;

public abstract class Flier {

	/** Position */
	public PointF position;
	
	/** Velocity */
	protected PointF velocity;
	
	protected float health = 1.0f;
	
	protected BattleThread thread;

	public Flier(BattleThread thread, PointF position, PointF velocity) {
		this.thread = thread;
		this.position = new PointF(position.x, position.y);
		this.velocity = new PointF(velocity.x, velocity.y);
	}
	
	public void updatePosition(long elapsed) {
		float r = position.length();
		float m = BattleSats.MASS_EARTH / (r * r * r);
		float dv_x = m * position.x;
		float dv_y = m * position.y;
		velocity.offset(-dv_x * elapsed / 1000.0f, -dv_y * elapsed / 1000.0f);
		position.offset(velocity.x * elapsed / 1000.0f, velocity.y * elapsed / 1000.0f);
	
		if (position.length() < BattleSats.EARTH_RADIUS) {
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
	
	/*
	 * Calculate parameters for an object in stationary orbit.
	 */
	public static void stationaryOrbitParams(float radius, float phase, boolean clockwise, PointF outPos, PointF outVelocity) {
		outPos.x = FloatMath.cos(phase) * radius;
		outPos.y = FloatMath.sin(phase) * radius;
		float v = FloatMath.sqrt(BattleSats.MASS_EARTH / radius);
		outVelocity.x = v * FloatMath.sin(phase) * (clockwise ? -1 : 1);
		outVelocity.y = v * FloatMath.cos(phase) * (clockwise ? 1 : -1);
	}
	
	public abstract void draw(Canvas canvas);
}