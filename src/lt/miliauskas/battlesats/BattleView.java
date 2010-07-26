package lt.miliauskas.battlesats;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnTouchListener;

public class BattleView extends SurfaceView implements Callback {

    /** The thread that actually draws the animation */
    private BattleThread thread;
	
	public BattleView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        thread = new BattleThread(holder, context);
    }

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
        thread.setSurfaceSize(width, height);		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        thread.setRunning(true);
        thread.start();
	}

    /*
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     */
	@Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }
	
	
	private static final int POINTER_STATE_IDLE = 0;
	private static final int POINTER_STATE_LAUNCHING = 1;
	private static final int POINTER_STATE_RESIZING = 2;
	private static final int POINTER_STATE_AFTER_RESIZE = 3;

	private int pointerState = POINTER_STATE_IDLE;
	private PointF motionStartPos;
	private float multitouchInitialLength;
	private PointF multitouchInitialCenter;
	private float multitouchInitialScale;
	
	private float multitouchLength(MotionEvent event) {
		return PointF.length(
				event.getX(1) - event.getX(0),
				event.getY(1) - event.getY(0));
	}
	
	private PointF multitouchCenter(MotionEvent event) {
		return new PointF(
				(event.getX(1) + event.getX(0)) / 2,
				(event.getY(1) + event.getY(0)) / 2);	
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		Log.i("touch event", "action:" + action);
		if (action == MotionEvent.ACTION_DOWN) {
			// Start of a gesture.
			pointerState = POINTER_STATE_LAUNCHING;
			motionStartPos = new PointF(event.getX(), event.getY());
		} else if (action == MotionEvent.ACTION_MOVE) {
			// Pointer move. Only interesting if we're resizing.
			if (pointerState == POINTER_STATE_RESIZING) {
				float currentLength = multitouchLength(event);
				thread.mVisualScale = multitouchInitialScale * currentLength / multitouchInitialLength;
			}
		} else if (action == MotionEvent.ACTION_POINTER_2_DOWN) {
			// 2nd pointer down, starting scaling
			pointerState = POINTER_STATE_RESIZING;
			multitouchInitialLength = multitouchLength(event);
			multitouchInitialCenter = multitouchCenter(event);
			multitouchInitialScale = thread.mVisualScale;
		} else if ((action == MotionEvent.ACTION_POINTER_2_UP) || (action == MotionEvent.ACTION_POINTER_1_UP)) {
			// Multitouch ended, finishing scaling.
			pointerState = POINTER_STATE_AFTER_RESIZE;
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (pointerState == POINTER_STATE_LAUNCHING) {
				// Launch a new satellite.
				PointF motionEndPos = new PointF(event.getX(), event.getY());
				PointF v = new PointF(
						(motionEndPos.x - motionStartPos.x) / BattleSats.DRAG_VELOCITY_RATIO,
						(motionEndPos.y - motionStartPos.y) / BattleSats.DRAG_VELOCITY_RATIO);
				Flier f = new LaserSentinel(thread, BattleSats.MASS_SATELLITE, thread.toInternalCoords(motionStartPos), v);
				thread.addFlier(f);
			}			
			motionStartPos = null;
		}
		return true;
	}
	
}
