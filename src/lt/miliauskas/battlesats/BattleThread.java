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
import android.util.Log;
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
	
	private int mCanvasHeight = 1;
	private int mCanvasWidth = 1;
	
	/** The drawable to use as the background of the animation canvas */
	private Bitmap mBackgroundImage;
	
	private Drawable mEarth;
	public int earthRadius;
	
	private List<Flier> fliers = new LinkedList<Flier>();
	private List<Flier> newFliers = new LinkedList<Flier>();
	private List<Flier> deadFliers = new LinkedList<Flier>();

    public BattleThread(SurfaceHolder surfaceHolder, Context context) {
        mSurfaceHolder = surfaceHolder;

        mContext = context;
        
        Resources res = context.getResources();
        // load background image as a Bitmap instead of a Drawable b/c
        // we don't need to transform it and it's faster to draw this way
        mBackgroundImage = BitmapFactory.decodeResource(res,
                R.drawable.stars);
        mEarth = res.getDrawable(R.drawable.bluemarble);
    }
	
	@Override
	public void run() {
		Log.i("BattleThread", "run()");
        mLastTime = System.currentTimeMillis();
		mMode = STATE_RUNNING; // XXX
		
		addFlier(new LaserSentinel(this, BattleSats.MASS_SATELLITE, new PointF(100.0f, 0.0f), new PointF(0.0f, -2.2f)));
		addFlier(new LaserSentinel(this, BattleSats.MASS_SATELLITE, new PointF(-100.0f, 0.0f), new PointF(0.0f, -2.4f)));
		addFlier(new LaserSentinel(this, BattleSats.MASS_SATELLITE, new PointF(80.0f, -20.0f), new PointF(0.0f, -2.6f)));
//		addFlier(new LaserSentinel(this, BattleSats.MASS_SATELLITE, new PointF(-120.0f, 40.0f), new PointF(0.0f, -2.0f)));
//		addFlier(new EnemyBomb(this, BattleSats.MASS_SATELLITE, new PointF(0.0f, -120.0f), new PointF(2.3f, 0.0f)));
		
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
    	synchronized (newFliers) {
    		newFliers.add(f);
    	}
    }
    
    public void removeFlier(Flier f) {
		synchronized (deadFliers) {
			deadFliers.add(f);
		}
    }

	private void doDraw(Canvas canvas) {
		// Draw the background image. Operations on the Canvas accumulate
		// so this is like clearing the screen.
		canvas.drawBitmap(mBackgroundImage, 0, 0, null);

		mEarth.setBounds(
				mCanvasWidth / 2 - earthRadius, mCanvasHeight / 2 - earthRadius,
				mCanvasWidth / 2 + earthRadius, mCanvasHeight / 2 + earthRadius);
		mEarth.draw(canvas);
		
		synchronized (fliers) {
			for (Flier flier : fliers) {
				flier.draw(canvas);
			}
		}
	}

	private void updatePhysics() {
		// TODO elapsed
		long now = System.currentTimeMillis();
		double elapsed = (now - mLastTime) / 1000.0;
		
		for (Flier flier : fliers) {
			flier.updatePosition(elapsed);
		}
		
        mLastTime = now;
		
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
	
	public PointF toDisplayCoords(PointF p) {
		return new PointF(mCanvasWidth / 2 + p.x, mCanvasHeight / 2 - p.y);
	}
	
	public PointF toInternalCoords(PointF p) {
		return new PointF(p.x - mCanvasWidth / 2, mCanvasHeight / 2 - p.y);		
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
