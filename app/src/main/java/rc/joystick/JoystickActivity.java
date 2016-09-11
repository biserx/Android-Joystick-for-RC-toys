package rc.joystick;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import rc.activities.MainActivity;
import rc.devices.R;
import rc.devices.RCBoatClient;
import rc.utils.Proportions;

public class JoystickActivity extends Activity {

	LinearLayout layout = null;
	TextView txtLeftJoystickStatus = null;
	TextView txtRightJoystickStatus = null;

	int LEFT_JOYSTICK_ID = -1;
	float LEFT_JOYSTICK_DEFAULT_X;
	float LEFT_JOYSTICK_DEFAULT_Y;
	boolean LEFT_JOYSTICK_ENABLE_X = false;
	boolean LEFT_JOYSTICK_ENABLE_Y = true;
	boolean LEFT_JOYSTICK_RESET_TO_DEFAULT = false;
	float JOYSTICK_LEFT_X;
	float JOYSTICK_LEFT_Y;

	int RIGHT_JOYSTICK_ID = -1;
	float RIGHT_JOYSTICK_DEFAULT_X;
	float RIGHT_JOYSTICK_DEFAULT_Y;
	boolean RIGHT_JOYSTICK_ENABLE_X = true;
	boolean RIGHT_JOYSTICK_ENABLE_Y = false;
	boolean RIGHT_JOYSTICK_RESET_TO_DEFAULT = false;
	float JOYSTICK_RIGHT_X;
	float JOYSTICK_RIGHT_Y;

	float JOYSTICK_SEPARATOR_LINE_POSITION;

	int CONTROL_MIN = 0;
	int CONTROL_MAX = 255;

	final int CONTROL_LEFT_X_MIN = CONTROL_MIN;
	final int CONTROL_LEFT_X_MAX = CONTROL_MAX;
	final int CONTROL_LEFT_Y_MIN = CONTROL_MIN;
	final int CONTROL_LEFT_Y_MAX = CONTROL_MAX;
	final int CONTROL_RIGHT_X_MIN = CONTROL_MIN;
	final int CONTROL_RIGHT_X_MAX = CONTROL_MAX;
	final int CONTROL_RIGHT_Y_MIN = CONTROL_MIN;
	final int CONTROL_RIGHT_Y_MAX = CONTROL_MAX;

	int CONTROL_LEFT_X;
	int CONTROL_LEFT_Y;
	int CONTROL_RIGHT_X;
	int CONTROL_RIGHT_Y;

	public enum StickPosition {
		UpLeft(0),
		UpCenter(1),
		UpRight(2),
		CenterLeft(3),
		CenterCenter(4),
		CenterRight(5),
		DownLeft(6),
		DownCenter(7),
		DownRight(8);
		final int value;
		StickPosition(int value) {
			this.value = value;
		}
		int getPositionX() {
			return value % 3;
		}
		int getPositionY() {
			return value / 3;
		}
	}

	public int getCONTROL_MIN() {
		return CONTROL_MIN;
	}

	public int getCONTROL_MAX() {
		return CONTROL_MAX;
	}

	public int getCONTROL_LEFT_X() {
		return CONTROL_LEFT_X;
	}

	public int getCONTROL_LEFT_Y() {
		return CONTROL_LEFT_Y;
	}

	public int getCONTROL_RIGHT_X() {
		return CONTROL_RIGHT_X;
	}

	public int getCONTROL_RIGHT_Y() {
		return CONTROL_RIGHT_Y;
	}

	StickPosition leftStick = StickPosition.CenterCenter;
	StickPosition rightStick = StickPosition.CenterCenter;
	public void configureSticks(StickPosition leftStick, StickPosition rightStick) {
		this.leftStick = leftStick;
		this.rightStick = rightStick;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_joystick);

		txtLeftJoystickStatus = (TextView) findViewById(R.id.txtLeftJoystickStatus);
		txtRightJoystickStatus = (TextView) findViewById(R.id.txtRightJoystickStatus);

		layout = (LinearLayout) findViewById(R.id.ll);

		layout.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
		layout.setOnTouchListener(controlsListener);

		String classname = getIntent().getExtras().getString(MainActivity.communicationModule);
		try {
			Class<?> clazz = Class.forName(classname);
			Constructor<?> constructor = clazz.getConstructor(JoystickActivity.class);
			Object instance = constructor.newInstance(this);
			Runnable run = (Runnable) instance;
			new Thread(run).start();
		} catch (ClassNotFoundException e) {
			return;
		} catch (InstantiationException e) {
			return;
		} catch (IllegalAccessException e) {
			return;
		} catch (NoSuchMethodException e) {
			return;
		} catch (InvocationTargetException e) {
			return;
		}
	}


	OnGlobalLayoutListener layoutListener = new OnGlobalLayoutListener() {
		@Override
		public void onGlobalLayout() {

			JOYSTICK_SEPARATOR_LINE_POSITION = layout.getWidth() / 2;

			LEFT_JOYSTICK_DEFAULT_X = leftStick.getPositionX() * layout.getWidth() / 4;
			LEFT_JOYSTICK_DEFAULT_Y = leftStick.getPositionY() * layout.getHeight() / 2;
			RIGHT_JOYSTICK_DEFAULT_X = layout.getWidth() / 2 + rightStick.getPositionX() * layout.getWidth() / 4;
			RIGHT_JOYSTICK_DEFAULT_Y = rightStick.getPositionY() * layout.getHeight() / 2;

			JOYSTICK_LEFT_X = LEFT_JOYSTICK_DEFAULT_X;
			JOYSTICK_LEFT_Y = LEFT_JOYSTICK_DEFAULT_Y;

			JOYSTICK_RIGHT_X = RIGHT_JOYSTICK_DEFAULT_X;
			JOYSTICK_RIGHT_Y = RIGHT_JOYSTICK_DEFAULT_Y;

			calcControls();
			drawBackground();

			layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
		}
	};

	OnTouchListener controlsListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			int pointerCount = arg1.getPointerCount();
			int actionIndex = arg1.getActionIndex();
			int pointerID = arg1.getPointerId(actionIndex);
			int pID = arg1.findPointerIndex(actionIndex);

			try {
				switch (arg1.getActionMasked()) {
					case MotionEvent.ACTION_DOWN:
					case MotionEvent.ACTION_POINTER_DOWN:
						if (arg1.getX(pointerID) < layout.getWidth() / 2) {
							if (LEFT_JOYSTICK_ID == -1) {
								LEFT_JOYSTICK_ID = pID;
								if (LEFT_JOYSTICK_ENABLE_X)
									JOYSTICK_LEFT_X = arg1.getX(pointerID);
								if (LEFT_JOYSTICK_ENABLE_Y)
									JOYSTICK_LEFT_Y = arg1.getY(pointerID);
							}
						}
						if (arg1.getX(pointerID) > layout.getWidth() / 2) {
							if (RIGHT_JOYSTICK_ID == -1) {
								RIGHT_JOYSTICK_ID = pID;
								if (RIGHT_JOYSTICK_ENABLE_X)
									JOYSTICK_RIGHT_X = arg1.getX(pointerID);
								if (RIGHT_JOYSTICK_ENABLE_Y)
									JOYSTICK_RIGHT_Y = arg1.getY(pointerID);
							}
						}
						break;
					case MotionEvent.ACTION_MOVE:
						for (int i = 0; i < pointerCount; i++) {
							pID = arg1.getPointerId(i);
							if (LEFT_JOYSTICK_ID == pID) {
								if (LEFT_JOYSTICK_ENABLE_X) {
									JOYSTICK_LEFT_X = arg1.getX(i);
									if (JOYSTICK_LEFT_X > JOYSTICK_SEPARATOR_LINE_POSITION)
										JOYSTICK_LEFT_X = JOYSTICK_SEPARATOR_LINE_POSITION;
								}
								if (LEFT_JOYSTICK_ENABLE_Y)
									JOYSTICK_LEFT_Y = arg1.getY(i);
							} else if (RIGHT_JOYSTICK_ID == pID) {
								if (RIGHT_JOYSTICK_ENABLE_X) {
									JOYSTICK_RIGHT_X = arg1.getX(i);
									if (JOYSTICK_RIGHT_X < JOYSTICK_SEPARATOR_LINE_POSITION)
										JOYSTICK_RIGHT_X = JOYSTICK_SEPARATOR_LINE_POSITION;
								}
								if (RIGHT_JOYSTICK_ENABLE_Y)
									JOYSTICK_RIGHT_Y = arg1.getY(i);
							}
						}
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_POINTER_UP:
					case MotionEvent.ACTION_CANCEL:
						if (LEFT_JOYSTICK_ID == pID) {
							LEFT_JOYSTICK_ID = -1;
							if (LEFT_JOYSTICK_RESET_TO_DEFAULT) {
								JOYSTICK_LEFT_X = LEFT_JOYSTICK_DEFAULT_X;
								JOYSTICK_LEFT_Y = LEFT_JOYSTICK_DEFAULT_Y;
							}
						} else if (RIGHT_JOYSTICK_ID == pID) {
							RIGHT_JOYSTICK_ID = -1;
							if (RIGHT_JOYSTICK_RESET_TO_DEFAULT) {
								JOYSTICK_RIGHT_X = RIGHT_JOYSTICK_DEFAULT_X;
								JOYSTICK_RIGHT_Y = RIGHT_JOYSTICK_DEFAULT_Y;
							}
						}
						break;
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
			drawBackground();
			calcControls();
			return true;
		}
	};

	private void drawBackground() {
		Bitmap b = Bitmap.createBitmap(layout.getWidth(), layout.getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);

		int w = layout.getWidth();
		int h = layout.getHeight();

		int joystick_size = h / 4;
		Bitmap joystick_resource = BitmapFactory.decodeResource(getResources(), R.drawable.joystick_top);
		Bitmap joystick = Bitmap.createScaledBitmap(joystick_resource, joystick_size, joystick_size, false);

		paint.setColor(Color.argb(255, 200, 200, 200));
		c.drawRect(w / 2, 0, w, h, paint);
		paint.setColor(Color.argb(255, 150, 150, 150));
		c.drawRect(0, 0, w / 2, h, paint);

		c.drawBitmap(joystick, JOYSTICK_LEFT_X - joystick_size/2, JOYSTICK_LEFT_Y - joystick_size/2, null);
		c.drawBitmap(joystick, JOYSTICK_RIGHT_X - joystick_size/2, JOYSTICK_RIGHT_Y - joystick_size/2, null);

		layout.setBackground(new BitmapDrawable(getResources(), b));
	}

	private void calcControls() {

		float cX1 = JOYSTICK_LEFT_X / (layout.getWidth() / 2);
		float cY1 = 1 - JOYSTICK_LEFT_Y / layout.getHeight(); // (layout.getHeight() - JOYSTICK_LEFT_Y) / layout.getHeight();
		float cX2 = JOYSTICK_RIGHT_X / (layout.getWidth() / 2) - 1; // (JOYSTICK_RIGHT_X - (layout.getWidth() / 2)) / (layout.getWidth() / 2);
		float cY2 = 1 - JOYSTICK_RIGHT_Y / layout.getHeight(); // (layout.getHeight() - JOYSTICK_RIGHT_Y) / layout.getHeight();

		float _min = 0f;
		float _max = 1f;

		if (cX1 < _min) cX1 = _min;
		if (cY1 < _min) cY1 = _min;
		if (cX2 < _min) cX2 = _min;
		if (cY2 < _min) cY2 = _min;

		if (cX1 > _max) cX1 = _max;
		if (cY1 > _max) cY1 = _max;
		if (cX2 > _max) cX2 = _max;
		if (cY2 > _max) cY2 = _max;

		cX1 = Proportions.linearProportion(cX1, _min, _max, CONTROL_LEFT_X_MIN, CONTROL_LEFT_X_MAX);
		cY1 = Proportions.linearProportion(cY1, _min, _max, CONTROL_LEFT_Y_MIN, CONTROL_LEFT_Y_MAX);
		cX2 = Proportions.linearProportion(cX2, _min, _max, CONTROL_RIGHT_X_MIN, CONTROL_RIGHT_X_MAX);
		cY2 = Proportions.linearProportion(cY2, _min, _max, CONTROL_RIGHT_Y_MIN, CONTROL_RIGHT_Y_MAX);

		CONTROL_LEFT_X = Math.round(cX1);
		CONTROL_LEFT_Y = Math.round(cY1);
		CONTROL_RIGHT_X = Math.round(cX2);
		CONTROL_RIGHT_Y = Math.round(cY2);

		txtLeftJoystickStatus.setText(CONTROL_LEFT_X + " x " + CONTROL_LEFT_Y);
		txtRightJoystickStatus.setText(CONTROL_RIGHT_X + " x " + CONTROL_RIGHT_Y);
	}

}

