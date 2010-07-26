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
import android.graphics.drawable.Drawable;
import android.os.Handler;
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
    private long lastBombardment;

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
	public int earthRadius;
	
	private List<Flier> fliers = new LinkedList<Flier>();
	private List<Flier> newFliers = new LinkedList<Flier>();
	private List<Flier> deadFliers = new LinkedList<Flier>();
	
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
    }
	
	@Override
	public void run() {
        mLastTime = lastBombardment = System.currentTimeMillis();
		mMode = STATE_RUNNING; // XXX
		
		addFlier(new LaserSentinel(this, BattleSats.MASS_SATELLITE, new PointF(100.0f, 0.0f), new PointF(0.0f, -30f)));
		addFlier(new LaserSentinel(this, BattleSats.MASS_SATELLITE, new PointF(-100.0f, 0.0f), new PointF(0.0f, 30f)));
		addFlier(new LaserSentinel(this, BattleSats.MASS_SATELLITE, new PointF(0.0f, 100.0f), new PointF(30.0f, 0.0f)));
		addFlier(new LaserSentinel(this, BattleSats.MASS_SATELLITE, new PointF(0.0f, -100.0f), new PointF(-30.0f, 0.0f)));
		
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

    public void setRunning(boolean b) {
        mRun = b;
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

	private void doDraw(Canvas canvas) {
		// Draw the background image. Operations on the Canvas accumulate
		// so this is like clearing the screen.
		canvas.drawBitmap(mBackgroundImage, 0, 0, null);
		
		canvas.translate(mCanvasWidth / 2, mCanvasHeight / 2);
		canvas.scale(mVisualScale, mVisualScale);

		mEarth.setBounds(-earthRadius, -earthRadius, earthRadius, earthRadius);
		mEarth.draw(canvas);
		
		synchronized (fliers) {
			for (Flier flier : fliers) {
				flier.draw(canvas);
			}
		}
	}

	private void updatePhysics() {
		long now = System.currentTimeMillis();
		long elapsed = now - mLastTime;
		
		for (Flier flier : fliers) {
			flier.updatePosition(elapsed);
		}
		
        mLastTime = now;
        
        // Bombardment.
        // TODO: bomber unit
        if (now - lastBombardment > 2000) {
        	lastBombardment = now;
        	int c1 = (now % 2 == 0) ? 1 : -1;
        	int c2 = (now / 2 % 2 == 0) ? 1 : -1;
    		addFlier(new EnemyBomb(this, BattleSats.MASS_SATELLITE,
    				new PointF(c1 * mCanvasWidth / 2, c2 * mCanvasHeight / 2),
    				new PointF(-c1 * 0.7f, -c2 * 0.8f)));
       }

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
        // synchronized to make sure these all change atomically
        synchronized (mSurfaceHolder) {
            mCanvasWidth = width;
            mCanvasHeight = height;

    		earthRadius = Math.min(mCanvasWidth, mCanvasHeight) / BattleSats.EARTH_SIZE_QUOTIENT / 2;
   
            // don't forget to resize the background image
            mBackgroundImage = Bitmap.createScaledBitmap(
                    mBackgroundImage, width, height, true);
        }
    }
}
