package com.dj.antispam;

/**
 * Created with IntelliJ IDEA.
 * User: dj
 * Date: 15.09.13
 * Time: 13:07
 * To change this template use File | Settings | File Templates.
 */

import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class SwipeTouchListener implements OnTouchListener {

	private final GestureDetector gestureDetector = new GestureDetector(new GestureListener());

	public boolean onTouch(final View view, final MotionEvent motionEvent) {
		return gestureDetector.onTouchEvent(motionEvent);
	}

	private final class GestureListener extends SimpleOnGestureListener {

		private static final int SWIPE_THRESHOLD = 100;
		private static final int SWIPE_VELOCITY_THRESHOLD = 100;

		@Override
		public boolean onDown(MotionEvent e) {
			return true;
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			boolean result = false;
			try {
				float diffY = e2.getY() - e1.getY();
				float diffX = e2.getX() - e1.getX();
				if (Math.abs(diffX) > Math.abs(diffY)) {
					if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
						if (diffX > 0) {
							onSwipeRight(e2.getX(), e2.getY());
						} else {
							onSwipeLeft(e2.getX(), e2.getY());
						}
					}
				} else {
					if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
						if (diffY > 0) {
							onSwipeBottom(e2.getX(), e2.getY());
						} else {
							onSwipeTop(e2.getX(), e2.getY());
						}
					}
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			return result;
		}
	}

	protected void onSwipeRight(float x,  float y) {}

	protected void onSwipeLeft(float x,  float y) {}

	protected void onSwipeTop(float x,  float y) {}

	protected void onSwipeBottom(float x,  float y) {}
}
