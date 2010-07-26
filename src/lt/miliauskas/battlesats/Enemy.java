package lt.miliauskas.battlesats;

import android.graphics.PointF;

public abstract class Enemy extends Flier {

	public Enemy(BattleThread thread, float mass, PointF position,
			PointF velocity) {
		super(thread, mass, position, velocity);
	}

}
