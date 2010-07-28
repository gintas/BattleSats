package lt.miliauskas.battlesats;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.FloatMath;

public class Tracer {
	
	public static final float INTERVAL_K = 0.2f / 1000.0f; // factor for step calculation
	public static final int ITERATIONS = 150;
	
	private Paint tracePaint = new Paint();
	private PointF initialPosition = new PointF();
	private PointF initialVelocity = new PointF();
	private PointF tempPosition = new PointF();
	private PointF tempVelocity = new PointF();
	private PointF prevTempPosition = new PointF();

	public Tracer() {
		tracePaint.setARGB(100, 0, 127, 255);
		tracePaint.setStrokeWidth(3.0f);
	}
	
	/*
	 * Draw a trace of a satellite if it were launched now.
	 */
	public void drawTrace(Canvas canvas) {
		synchronized (this) {
			prevTempPosition.set(initialPosition);
			tempPosition.set(initialPosition);
			tempVelocity.set(initialVelocity);
		}
		
		for (int i = 0; i < ITERATIONS; i++) {
			float r = tempPosition.length();
			float m = BattleSats.MASS_EARTH / (r * r * r);
			float dv_x = m * tempPosition.x;
			float dv_y = m * tempPosition.y;
			float k = FloatMath.sqrt(r*r*r) * INTERVAL_K;
			tempVelocity.offset(-dv_x * k, -dv_y * k);
			tempPosition.offset(tempVelocity.x * k, tempVelocity.y * k);
		
			if (tempPosition.length() < BattleSats.EARTH_RADIUS)
				break;

			canvas.drawLine(prevTempPosition.x, prevTempPosition.y,
					tempPosition.x, tempPosition.y, tracePaint);
			prevTempPosition.set(tempPosition);
			
			if (i > 10 && PointF.length(tempPosition.x - initialPosition.x, tempPosition.y - initialPosition.y) < 5.0f)
				break;
		}
	}

	public void set(PointF p, PointF v) {
		synchronized (this) {
			initialPosition.set(p);
			initialVelocity.set(v);
		}
	}

}
