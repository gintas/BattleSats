package lt.miliauskas.battlesats;

import android.graphics.PointF;

public abstract class Enemy extends Flier {

	public Enemy(BattleThread thread, PointF position,
			PointF velocity) {
		super(thread, position, velocity);
	}

}
