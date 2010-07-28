package lt.miliauskas.battlesats;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

public class Explosion extends Flier {
	
	public static final int EXPLOSION_LENGTH = 500; // ms
	public static final float MIN_RADIUS = 2.0f; // ms
	public static final float MAX_RADIUS = 5.0f; // ms
	public static final float WAVE_LENGTH = 2.0f; // ms
	
	protected PointF initialPosition = new PointF();
	private int lifetime = 0;
	private Paint explosionPaint = new Paint();
	private Paint holePaint = new Paint();

	public Explosion(BattleThread thread, PointF position, PointF velocity) {
		super(thread, position, velocity);
		initialPosition.set(position);
		explosionPaint.setARGB(255, 255, 255, 0);
		explosionPaint.setAntiAlias(true);
		holePaint.setARGB(255, 0, 0, 0);
		holePaint.setAntiAlias(true);
	}

	@Override
	public void updatePosition(long elapsed) {
		super.updatePosition(elapsed);
		position.set(initialPosition); // stay put
		lifetime += elapsed;
		
		if (lifetime > EXPLOSION_LENGTH) {
			thread.removeFlier(this);
		}
	}

	@Override
	public void draw(Canvas canvas) {
		float radius = MIN_RADIUS + (MAX_RADIUS - MIN_RADIUS) * lifetime / EXPLOSION_LENGTH;
		canvas.drawCircle(position.x, position.y, radius, explosionPaint);
		canvas.drawCircle(position.x, position.y, radius - WAVE_LENGTH, holePaint);
	}

}
