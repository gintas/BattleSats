package lt.miliauskas.battlesats;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;

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
	
	/* Touch handling */
	
	private static final int POINTER_STATE_IDLE = 0;
	private static final int POINTER_STATE_LAUNCHING = 1;
	private static final int POINTER_STATE_RESIZING = 2;
	private static final int POINTER_STATE_AFTER_RESIZE = 3;

	private int pointerState = POINTER_STATE_IDLE;
	private PointF motionStartPos = new PointF();
	private float multitouchInitialLength;
	private float multitouchInitialScale;
	
	private float multitouchLength(MotionEvent event) {
		return PointF.length(
				event.getX(1) - event.getX(0),
				event.getY(1) - event.getY(0));
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		if (action == MotionEvent.ACTION_DOWN) {
			// Start of a gesture.
			pointerState = POINTER_STATE_LAUNCHING;
			motionStartPos.set(event.getX(), event.getY());
		} else if (action == MotionEvent.ACTION_MOVE) {
			// Pointer move. Only interesting if we're resizing.
			if (pointerState == POINTER_STATE_RESIZING) {
				thread.mVisualScale = multitouchInitialScale * multitouchLength(event) / multitouchInitialLength;
			}
		} else if (action == MotionEvent.ACTION_POINTER_2_DOWN) {
			// 2nd pointer down, starting scaling
			multitouchInitialLength = multitouchLength(event);
			multitouchInitialScale = thread.mVisualScale;
			if (multitouchInitialLength > 20.0f) {
				// Avoid spurious multitouch.
				pointerState = POINTER_STATE_RESIZING;
			}
		} else if ((action == MotionEvent.ACTION_POINTER_2_UP) || (action == MotionEvent.ACTION_POINTER_1_UP)) {
			// Multitouch ended, finishing scaling.
			if (pointerState == POINTER_STATE_RESIZING) {
				pointerState = POINTER_STATE_AFTER_RESIZE;
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (pointerState == POINTER_STATE_LAUNCHING) {
				// Launch a new satellite.
				PointF motionEndPos = new PointF(event.getX(), event.getY());
				PointF v = new PointF(
						(motionEndPos.x - motionStartPos.x) / BattleSats.DRAG_VELOCITY_RATIO,
						(motionEndPos.y - motionStartPos.y) / BattleSats.DRAG_VELOCITY_RATIO);
				Flier f = new LaserSentinel(thread, thread.toInternalCoords(motionStartPos), v);
				thread.addFlier(f);
			}
		}
		return true;
	}
	
}
