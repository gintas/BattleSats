package lt.miliauskas.battlesats;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

public class BattleSats extends Activity {

	/*
	 * Gameplay constants
	 */
	public static final float BOMB_HEALTH = 1.0f;
	public static final float BOMBER_HEALTH = 1000.0f;
	public static final float BOMBER_INTERVAL = 3000; // ms
	public static final float LASER_DAMAGE = 1.0f; // HP / ms
	public static final float LASER_RANGE = 100.0f;

	/*
	 * Physics constants
	 */
	public static final float MASS_G = 2.0f; // gravitational constant
	public static final float MASS_EARTH = 1000.0f;
	
	/*
	 * Constants for visuals
	 */
	public static final int EARTH_SIZE_QUOTIENT = 10;
	public static final int LASER_BEAM_RADIUS = 2;
	public static final float BOMB_RADIUS = 5.0f;
	public static final float BOMBER_WIDTH = 25.0f;
	public static final float BOMBER_HEIGHT = 10.0f;
	
	/** How many pixels to drag onscreen to give a flier a velocity of 1 */
	public static final float DRAG_VELOCITY_RATIO = 5.0f;
	
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