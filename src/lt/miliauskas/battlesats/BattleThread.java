package lt.miliauskas.battlesats;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.SurfaceHolder;

public class BattleThread extends Thread {

	/*
	 * State-tracking constants
	 */
	public static final int STATE_LOSE = 1;
	public static final int STATE_PAUSE = 2;
	public static final int STATE_READY = 3;
	public static final int STATE_RUNNING = 4;
	public static final int STATE_WIN = 5;

	/** Handle to the surface manager object we interact with */
	private SurfaceHolder mSurfaceHolder;
	
    /** Used to figure out elapsed time between frames */
    private long mLastTime;
    
    /** Handle to the application context, used to e.g. fetch Drawables. */
    private Context mContext;

	/** The state of the game. One of READY, RUNNING, PAUSE, LOSE, or WIN */
	private int mMode;

	/** Indicate whether the surface has been created & is ready to draw */
	private boolean mRun = false;
	
	/** Visual scale factor */
	public float mVisualScale = 1.0f;
	
	/** Canvas height */
	private int mCanvasHeight = 1;

	/** Canvas width */
	private int mCanvasWidth = 1;
		
	/** The drawable to use as the background of the animation canvas */
	private Bitmap mBackgroundImage;
	
	private Drawable mEarth;
	
	private float earthHealth;
	private int reserveSatellites;
	
	private List<Flier> fliers = new LinkedList<Flier>();
	private List<Flier> newFliers = new LinkedList<Flier>();
	private List<Flier> deadFliers = new LinkedList<Flier>();

    private RectF healthBarFull = new RectF();
    private RectF healthBarEmpty = new RectF();
	private Paint healthBarPaint = new Paint();
	private Paint healthBarEmptyPaint = new Paint();
	private Paint reserveSatellitesNumPaint = new Paint();

	private Vibrator vibrator;

    public BattleThread(SurfaceHolder surfaceHolder, Context context) {
        mSurfaceHolder = surfaceHolder;

        mContext = context;
        
		vibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);

        Resources res = context.getResources();
        // load background image as a Bitmap instead of a Drawable b/c
        // we don't need to transform it and it's faster to draw this way
        mBackgroundImage = BitmapFactory.decodeResource(res,
                R.drawable.stars);
 
        mEarth = res.getDrawable(R.drawable.bluemarble);
        
		healthBarPaint.setARGB(150, 0, 255, 0);
		healthBarPaint.setAntiAlias(true);
		healthBarEmptyPaint.setARGB(50, 0, 255, 0);
		healthBarEmptyPaint.setAntiAlias(true);
		reserveSatellitesNumPaint.setARGB(200, 120, 120, 0);
		reserveSatellitesNumPaint.setAntiAlias(true);
		reserveSatellitesNumPaint.setTextSize(30);
		reserveSatellitesNumPaint.setStyle(Paint.Style.FILL);
		reserveSatellitesNumPaint.setTextAlign(Paint.Align.RIGHT);
    }
    
    public void hurtEarth(float damage) {
        synchronized (mSurfaceHolder) {
        	earthHealth -= damage;
        	if (earthHealth <= 0) {
        		setState(STATE_LOSE);
        	}
        }
    }
    
    /**
     * Initializes and starts the game.
     */
    public void doStart() {
        synchronized (mSurfaceHolder) {
        	addInitialFliers();
            mLastTime = System.currentTimeMillis();
            earthHealth = BattleSats.EARTH_HEALTH;
            reserveSatellites = BattleSats.USER_SATELLITES;
            setState(STATE_RUNNING);
        }
    }

    /**
     * Pauses the physics update & animation.
     */
    public void pause() {
        synchronized (mSurfaceHolder) {
            if (mMode == STATE_RUNNING) setState(STATE_PAUSE);
        }
    }
    
    /**
     * Resumes from a pause.
     */
    public void unpause() {
        // Move the real time clock up to now
        synchronized (mSurfaceHolder) {
            mLastTime = System.currentTimeMillis() + 100;
        }
        setState(STATE_RUNNING);
    }

    /**
     * Restores game state from the indicated Bundle.
     * 
     * @param savedState Bundle containing the game state
     */
    public synchronized void restoreState(Bundle savedState) {
        synchronized (mSurfaceHolder) {
            setState(STATE_PAUSE);
            // TODO
        }
    }
    
    /**
     * Dump game state to the provided Bundle. Typically called when the
     * Activity is being suspended.
     * 
     * @return Bundle with this view's state
     */
    public Bundle saveState(Bundle map) {
        synchronized (mSurfaceHolder) {
        	// TODO
        }
        return map;
    }

    private void addInitialFliers() {
    	PointF p = new PointF();
    	PointF v = new PointF();
    	
    	Flier.stationaryOrbitParams(100.0f, 0.0f, false, p, v);
		addFlier(new LaserSentinel(this, p, v));
    	Flier.stationaryOrbitParams(100.0f, (float)Math.PI / 2.0f, false, p, v);
		addFlier(new LaserSentinel(this, p, v));
    	Flier.stationaryOrbitParams(100.0f, (float)Math.PI, false, p, v);
		addFlier(new LaserSentinel(this, p, v));
    	Flier.stationaryOrbitParams(100.0f, (float)Math.PI * 3.0f / 2.0f, false, p, v);
		addFlier(new LaserSentinel(this, p, v));

    	Flier.stationaryOrbitParams(400.0f, (float)Math.PI * 0.45f, true, p, v);
		addFlier(new EnemyBomber(this, p, v));
    	Flier.stationaryOrbitParams(400.0f, (float)Math.PI * 0.5f, true, p, v);
		addFlier(new EnemyBomber(this, p, v));
    	Flier.stationaryOrbitParams(400.0f, (float)Math.PI * 0.55f, true, p, v);
		addFlier(new EnemyBomber(this, p, v));
    	Flier.stationaryOrbitParams(400.0f, (float)Math.PI * 1.75f, true, p, v);
		addFlier(new EnemyBomber(this, p, v));
    }
	
	@Override
	public void run() {
		doStart();
		while (mRun) {
			Canvas c = null;
			try {
				c = mSurfaceHolder.lockCanvas(null);
				synchronized (mSurfaceHolder) {
					if (mMode == STATE_RUNNING) updatePhysics();
					doDraw(c);
				}
			} finally {
				// do this in a finally so that if an exception is thrown
				// during the above, we don't leave the Surface in an
				// inconsistent state
				if (c != null) {
					mSurfaceHolder.unlockCanvasAndPost(c);
				}
			}
		}
	}

    /**
     * Used to signal the thread whether it should be running or not.
     * Passing true allows the thread to run; passing false will shut it
     * down if it's already running. Calling start() after this was most
     * recently called with false will result in an immediate shutdown.
     * 
     * @param b true to run, false to shut down
     */
    public void setRunning(boolean b) {
        mRun = b;
    }
    
    /**
     * Sets the game mode. That is, whether we are running, paused, in the
     * failure state, in the victory state, etc.
     * 
     * @see #setState(int, CharSequence)
     * @param mode one of the STATE_* constants
     */
    public void setState(int mode) {
        synchronized (mSurfaceHolder) {
            setState(mode, null);
        }
    }
    
    /**
     * Sets the game mode. That is, whether we are running, paused, in the
     * failure state, in the victory state, etc.
     * 
     * @param mode one of the STATE_* constants
     * @param message string to add to screen or null
     */
    public void setState(int mode, CharSequence message) {
        /*
         * This method optionally can cause a text message to be displayed
         * to the user when the mode changes. Since the View that actually
         * renders that text is part of the main View hierarchy and not
         * owned by this thread, we can't touch the state of that View.
         * Instead we use a Message + Handler to relay commands to the main
         * thread, which updates the user-text View.
         */
        synchronized (mSurfaceHolder) {
            mMode = mode;
            // TODO: reflect state in UI
        }
    }

    public void addFlier(Flier f) {
    	// TODO: check for dupes
    	synchronized (newFliers) {
    		newFliers.add(f);
    	}
    }
    
    public void removeFlier(Flier f) {
    	// TODO: check for dupes
		synchronized (deadFliers) {
			deadFliers.add(f);
		}
    }
    
    private void drawHealthBar(Canvas canvas) {
		// Draw health of Earth
		float healthBarWidth = mCanvasWidth / 2;
		float top = mCanvasHeight - BattleSats.HEALTH_BAR_HEIGHT - BattleSats.HEALTH_BAR_PADDING;
		float bottom = mCanvasHeight - BattleSats.HEALTH_BAR_PADDING;
		float left = (mCanvasWidth / 2) - (healthBarWidth / 2);
		float right = left + healthBarWidth * earthHealth / BattleSats.EARTH_HEALTH;
		healthBarFull.set(left, top, right, bottom);
		canvas.drawRect(healthBarFull, healthBarPaint);
		
		if (earthHealth < BattleSats.EARTH_HEALTH) {
			healthBarEmpty.set(
					right, top,
					(mCanvasWidth / 2) + (healthBarWidth / 2), bottom);
			canvas.drawRect(healthBarEmpty, healthBarEmptyPaint);
		}
    }
    
	private void doDraw(Canvas canvas) {
		// Draw background.
		canvas.drawBitmap(mBackgroundImage, 0, 0, null);
		
		canvas.save();
		
		// Prepare for drawing objects: set the center of the screen to be (0, 0);
		canvas.translate(mCanvasWidth / 2, mCanvasHeight / 2);
		canvas.scale(mVisualScale, mVisualScale);

		// Draw Earth
		mEarth.setBounds(
				-BattleSats.EARTH_RADIUS, -BattleSats.EARTH_RADIUS,
				BattleSats.EARTH_RADIUS, BattleSats.EARTH_RADIUS);
		mEarth.draw(canvas);
		
		synchronized (fliers) {
			for (Flier flier : fliers) {
				flier.draw(canvas);
			}
		}

		canvas.restore();
		// Draw HUD items.
		drawHealthBar(canvas);
		canvas.drawText(Integer.toString(reserveSatellites),
				mCanvasWidth - BattleSats.HEALTH_BAR_PADDING,
				mCanvasHeight - BattleSats.HEALTH_BAR_PADDING,
				reserveSatellitesNumPaint);
	}

	private void updatePhysics() {
		long now = System.currentTimeMillis();
		long elapsed = now - mLastTime;
		
        // Do nothing if mLastTime is in the future.
        // This allows the game-start to delay the start of the physics
        // by 100ms or whatever.
        if (mLastTime > now) return;

		for (Flier flier : fliers) {
			flier.updatePosition(elapsed);
		}
		
        mLastTime = now;
        
		// Add new fliers, purge dead ones.
		synchronized (newFliers) {
			for (Flier flier : newFliers) {
				fliers.add(flier);
			}
			newFliers.clear();
		}
		synchronized (deadFliers) {
			for (Flier flier : deadFliers) {
				fliers.remove(flier);
			}
			deadFliers.clear();
		}
	}

	public PointF toInternalCoords(PointF p) {
		return new PointF((p.x - mCanvasWidth / 2) / mVisualScale, (p.y - mCanvasHeight / 2) / mVisualScale);
	}
	
	public Enemy findNearestEnemy(PointF p) {
		float nearest_d = 1000000.0f;
		Enemy nearest = null;
		for (Flier flier : fliers) {
			if (flier instanceof Enemy) {
				float d = PointF.length(flier.position.x - p.x, flier.position.y - p.y);
				if (d < nearest_d) {
					nearest_d = d;
					nearest = (Enemy)flier;
				}
			}
		}
		return nearest;
	}
	
	public void vibrate() {
		vibrator.vibrate(200);
	}
	
    /* Callback invoked when the surface dimensions change. */
    public void setSurfaceSize(int width, int height) {
        synchronized (mSurfaceHolder) {
            mCanvasWidth = width;
            mCanvasHeight = height;

            // resize the background image
            mBackgroundImage = Bitmap.createScaledBitmap(
                    mBackgroundImage, width, height, true);
        }
    }

	public void launchSat(PointF position, PointF velocity) {
		if (reserveSatellites > 0) {
			addFlier(new LaserSentinel(this, position, velocity));
			reserveSatellites--;
		}
	}
}
