package lt.miliauskas.battlesats;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

public class BattleSats extends Activity {

	/*
	 * Gameplay constants
	 */
	public static final float BOMB_HEALTH = 1.5f;
	public static final float BOMB_DAMAGE = 5.0f;
	public static final float BOMBER_HEALTH = 1000.0f;
	public static final float BOMBER_INTERVAL = 3000; // ms
	public static final float LASER_DAMAGE = 1.0f; // HP / ms
	public static final float LASER_RANGE = 100.0f;
	public static final int EARTH_RADIUS = 30;
	public static final float EARTH_HEALTH = 100.0f;
	public static final int USER_SATELLITES = 5;

	/*
	 * Physics constants
	 */
	public static final float MASS_EARTH = 50000.0f;
	
	/*
	 * Constants for visuals
	 */
	public static final int LASER_BEAM_RADIUS = 2;
	public static final float BOMB_RADIUS = 5.0f;
	public static final float BOMBER_WIDTH = 25.0f;
	public static final float BOMBER_HEIGHT = 10.0f;
	public static final float HEALTH_BAR_HEIGHT = 10.0f;
	public static final float HEALTH_BAR_PADDING = 10.0f;
	
	/** How many pixels to drag onscreen to give a flier a velocity of 1 */
	public static final float DRAG_VELOCITY_RATIO = 8.0f;
	
	/*
     * State-tracking constants
     */
    public static final int STATE_LOSE = 1;
    public static final int STATE_PAUSE = 2;
    public static final int STATE_READY = 3;
    public static final int STATE_RUNNING = 4;
    public static final int STATE_WIN = 5;
	
	BattleView battleView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // turn off the window's title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        battleView = (BattleView)findViewById(R.id.battle);
        BattleThread thread = battleView.getThread();

        Log.i("BattleSats", "onCreate");
        if (savedInstanceState == null) {
            // we were just launched: set up a new game
            thread.setMode(BattleThread.STATE_READY);
        } else {
            // we are being restored: resume a previous game
            thread.restoreState(savedInstanceState);
            Log.w(this.getClass().getName(), "SIS is nonnull");
        }
    }
    
    /**
     * Invoked when the Activity loses user focus.
     */
    @Override
    protected void onPause() {
        super.onPause();
        battleView.getThread().pause(); // pause game when Activity pauses
        Log.i("BattleSats", "onPause");
    }

    /**
     * Notification that something is about to happen, to give the Activity a
     * chance to save state.
     * 
     * @param outState a Bundle into which this Activity should save its state
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // just have the View's thread save its state into our Bundle
        super.onSaveInstanceState(outState);
        battleView.getThread().saveState(outState);
        Log.w(this.getClass().getName(), "SIS called");
    }

}