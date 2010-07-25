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

        Log.i("BattleView", "constructor");
        
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
	
	private PointF motionStartPos;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			motionStartPos = new PointF(event.getX(), event.getY());
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			PointF motionEndPos = new PointF(event.getX(), event.getY());
			PointF v = new PointF(
					(motionEndPos.x - motionStartPos.x) / BattleSats.DRAG_VELOCITY_RATIO,
					-(motionEndPos.y - motionStartPos.y) / BattleSats.DRAG_VELOCITY_RATIO);
			Flier f = new EnemyBomb(thread, BattleSats.MASS_SATELLITE, thread.toInternalCoords(motionStartPos), v);
			thread.addFlier(f);
			
			motionStartPos = null;
		}
		Log.i("BattleView", "touch " + event.getAction());
		
		return true;
	}
	
}
