package lt.miliauskas.battlesats;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

public class LaserSentinel extends Flier {
	
	protected int HEIGHT = 10;
	protected int WIDTH = 5;
	
	protected float range = BattleSats.LASER_RANGE;
	protected float damage = BattleSats.LASER_DAMAGE;
	protected Flier target = null;
	protected LaserWeapon laser;
	
	protected Paint bodyPaint = new Paint();
	
	public LaserSentinel(BattleThread thread, PointF position, PointF velocity) {
		super(thread, position, velocity);
		this.type = TYPE_FRIEND;
		this.laser = new LaserWeapon(this, BattleSats.LASER_RANGE, BattleSats.LASER_DAMAGE, BattleSats.LASER_BEAM_RADIUS);
		this.health = BattleSats.LASER_SENTINEL_HEALTH;
		
		bodyPaint.setAntiAlias(true);
		bodyPaint.setARGB(255, 120, 180, 0);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.save();
		canvas.translate(position.x, position.y);

		laser.rotateCanvas(canvas);
		
		laser.draw(canvas);
		canvas.drawRect(-WIDTH/2, -HEIGHT/2, WIDTH/2, HEIGHT/2, bodyPaint);
		
		canvas.restore();
	}

	@Override
	public void updatePosition(long elapsed) {
		super.updatePosition(elapsed);
		laser.doDamage(elapsed, Flier.TYPE_ATTACKER);
	}

}
