package lt.miliauskas.battlesats;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

public class BattleSats extends Activity {
	
	/*
	 * Gameplay constants
	 */
	public static final float BOMB_HEALTH = 2.0f;
	public static final float LASER_DAMAGE = 2.0f; // HP / ms
	public static final float LASER_RANGE = 100.0f;

	/*
	 * Physics constants
	 */
	public static final float MASS_G = 15.0f; // gravitational constant
	public static final float MASS_EARTH = 1000.0f;
	public static final float MASS_SATELLITE = 1.0f;
	
	/*
	 * Constants for visuals
	 */
	public static final int EARTH_SIZE_QUOTIENT = 5;
	
	/** How many pixels to drag onscreen to give a flier a velocity of 1 */
	public static final float DRAG_VELOCITY_RATIO = 70.0f;
	
	BattleView battleView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // turn off the window's title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
    }
}