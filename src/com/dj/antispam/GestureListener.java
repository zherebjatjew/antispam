package com.dj.antispam;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Toast;

/**
 * Created with IntelliJ IDEA.
 * User: dj
 * Date: 14.09.13
 * Time: 21:23
 * To change this template use File | Settings | File Templates.
 */
public abstract class GestureListener extends GestureDetector.SimpleOnGestureListener {
	private static final int SWIPE_MIN_DISTANCE = 150;
	private static final int SWIPE_MAX_OFF_PATH = 100;
	private static final int SWIPE_THRESHOLD_VELOCITY = 100;

	private MotionEvent mLastOnDownEvent = null;

	@Override
	public boolean onDown(MotionEvent e)
	{
		mLastOnDownEvent = e;
		return super.onDown(e);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
	{
		if(e1 == null) {
			e1 = mLastOnDownEvent;
		}
		if (e1 == null || e2 == null) {
			return false;
		}

		float dX = e2.getX() - e1.getX();
		float dY = e1.getY() - e2.getY();

		if (Math.abs(dY) < SWIPE_MAX_OFF_PATH && Math.abs(velocityX) >= SWIPE_THRESHOLD_VELOCITY && Math.abs(dX) >= SWIPE_MIN_DISTANCE ) {
			if (dX > 0) {
				onSwipeRight(e2.getX(), e2.getY());
			} else {
				onSwipeLeft(e2.getX(), e2.getY());
			}
			return true;
		}
		return false;
	}

	protected abstract void onSwipeRight(float x, float y);

	protected abstract void onSwipeLeft(float x, float y);
}
