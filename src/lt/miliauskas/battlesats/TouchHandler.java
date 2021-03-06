package lt.miliauskas.battlesats;

import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class TouchHandler implements OnTouchListener {
	
	private static final int POINTER_STATE_IDLE = 0;
	private static final int POINTER_STATE_LAUNCHING = 1;
	private static final int POINTER_STATE_RESIZING = 2;
	private static final int POINTER_STATE_AFTER_RESIZE = 3;

	private BattleThread thread;
	private int pointerState = POINTER_STATE_IDLE;
	private PointF motionStartPos = new PointF();
	private PointF motionEndPos = new PointF();
	private PointF motionVelocity = new PointF();
	private float multitouchInitialLength;
	private float multitouchInitialScale;
	
	public TouchHandler(BattleThread thread ) {
		this.thread = thread;
	}

	public boolean onTouch(View view, MotionEvent event) {
		int action = event.getAction();
		if (action == MotionEvent.ACTION_DOWN) {
			// Start of a gesture.
			pointerState = POINTER_STATE_LAUNCHING;
			motionStartPos.set(event.getX(), event.getY());
		} else if (action == MotionEvent.ACTION_MOVE) {
			// Pointer move.
			if (pointerState == POINTER_STATE_RESIZING) {
				thread.setVisualScale(multitouchInitialScale * multitouchLength(event) / multitouchInitialLength);
			} else if (pointerState == POINTER_STATE_LAUNCHING) {
				motionEndPos.set(event.getX(), event.getY());
				motionVelocity.set(
						(motionEndPos.x - motionStartPos.x) / BattleSats.DRAG_VELOCITY_RATIO,
						(motionEndPos.y - motionStartPos.y) / BattleSats.DRAG_VELOCITY_RATIO);
				thread.showTrace(thread.toInternalCoords(motionStartPos), motionVelocity);
			}
		} else if (action == MotionEvent.ACTION_POINTER_2_DOWN) {
			// 2nd pointer down, starting scaling
			multitouchInitialLength = multitouchLength(event);
			multitouchInitialScale = thread.mVisualScale;
			if (multitouchInitialLength > 20.0f) {
				// Avoid spurious multitouch.
				pointerState = POINTER_STATE_RESIZING;
				thread.hideTrace();
			}
		} else if ((action == MotionEvent.ACTION_POINTER_2_UP) || (action == MotionEvent.ACTION_POINTER_1_UP)) {
			// Multitouch ended, finishing scaling.
			if (pointerState == POINTER_STATE_RESIZING) {
				pointerState = POINTER_STATE_AFTER_RESIZE;
			}
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (thread.getMode() == BattleThread.STATE_PAUSE) {
				thread.unpause();
			} else if (thread.getMode() == BattleThread.STATE_LOSE) {
				thread.doStart();
			} else if (pointerState == POINTER_STATE_LAUNCHING) {
				// Launch a new satellite.
				motionEndPos.set(event.getX(), event.getY());
				motionVelocity.set(
						(motionEndPos.x - motionStartPos.x) / BattleSats.DRAG_VELOCITY_RATIO,
						(motionEndPos.y - motionStartPos.y) / BattleSats.DRAG_VELOCITY_RATIO);
				thread.launchSat(thread.toInternalCoords(motionStartPos), motionVelocity);
			}
			pointerState = POINTER_STATE_IDLE;
			thread.hideTrace();
		}
		return true;
	}
	
	private float multitouchLength(MotionEvent event) {
		return PointF.length(
				event.getX(1) - event.getX(0),
				event.getY(1) - event.getY(0));
	}
}
