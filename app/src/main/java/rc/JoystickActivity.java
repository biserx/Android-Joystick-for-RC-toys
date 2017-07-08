package rc;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Constructor;

import rc.devices.Device;
import rc.devices.R;
import rc.joystick.Joystick;
import rc.joystick.Thumb;
import rc.utils.Proportions;

public class JoystickActivity extends Activity {

	private LinearLayout layout = null;
	private TextView txtLeftJoystickStatus = null;
	private TextView txtRightJoystickStatus = null;

	private Joystick joystick = null;

	private int leftPadId = -1;
	private int rightPadId = -1;

	private Device controlledDevice = null;
	private Thread communicationThread = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_joystick);

		layoutInitialized = false;

		txtLeftJoystickStatus = (TextView) findViewById(R.id.txtLeftJoystickStatus);
		txtRightJoystickStatus = (TextView) findViewById(R.id.txtRightJoystickStatus);

        joystick = new Joystick();

		try {
			Class<?> clazz = (Class<?>) getIntent().getExtras().get(MainActivity.DEVICE);
			Constructor<?> constructor = clazz.getConstructor(Joystick.class);
			Object instance = constructor.newInstance(joystick);
            controlledDevice = (Device) instance;
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}

		layout = (LinearLayout) findViewById(R.id.ll);

		layout.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
		layout.setOnTouchListener(controlsListener);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (controlledDevice != null) {
			if (communicationThread == null || communicationThread.isInterrupted() || !communicationThread.isAlive()) {
				communicationThread = new Thread(controlledDevice.getCommunicationLink());
				communicationThread.start();
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		communicationThread.interrupt();
	}

	private float padLeftX;
	private float padLeftY;
	private float padRightX;
	private float padRightY;

	private float padLeftDefaultX;
	private float padLeftDefaultY;
	private float padRightDefaultX;
	private float padRightDefaultY;

    int padSize;

	boolean layoutInitialized;

	OnGlobalLayoutListener layoutListener = new OnGlobalLayoutListener() {
		@Override
		public void onGlobalLayout() {

            padSize = layout.getHeight() / 6;

			padLeftDefaultX = joystick.getStickLeft().getDefaultPosition().getPositionX() * ((layout.getWidth() - 2 * padSize) / 4) + padSize / 2;
			padLeftDefaultY = joystick.getStickLeft().getDefaultPosition().getPositionY() * (layout.getHeight() - padSize) / 2 + padSize / 2;
			padRightDefaultX = layout.getWidth() / 2 + padSize / 2 + joystick.getStickRight().getDefaultPosition().getPositionX() * (layout.getWidth() - 2 * padSize) / 4;
			padRightDefaultY = joystick.getStickRight().getDefaultPosition().getPositionY() * (layout.getHeight() - padSize) / 2 + padSize / 2;

			padLeftX = padLeftDefaultX;
			padLeftY = padLeftDefaultY;

			padRightX = padRightDefaultX;
			padRightY = padRightDefaultY;

			updateJoystickValues();
			drawJoystickOnScreen();

			layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

			layoutInitialized = true;
		}
	};

    OnTouchListener controlsListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			if (!layoutInitialized) return false;

			int pointerCount = arg1.getPointerCount();
			int actionIndex = arg1.getActionIndex();
			int pointerID = arg1.getPointerId(actionIndex);
			int pID = arg1.findPointerIndex(actionIndex);

			try {
				switch (arg1.getActionMasked()) {
					case MotionEvent.ACTION_DOWN:
					case MotionEvent.ACTION_POINTER_DOWN:
						if (arg1.getX(pointerID) < layout.getWidth() / 2) {
							if (leftPadId == -1) {
								leftPadId = pID;
								if (joystick.getStickLeft().isEnableXAxis()) {
                                    padLeftX = arg1.getX(pointerID);
                                    if (padLeftX > layout.getWidth() / 2 - padSize / 2) padLeftX = layout.getWidth() / 2 - padSize / 2;
                                    if (padLeftX < padSize / 2) padLeftX = padSize / 2;
                                } if (joystick.getStickLeft().isEnableYAxis()) {
                                    padLeftY = arg1.getY(pointerID);
                                    if (padLeftY > layout.getHeight() - padSize / 2) padLeftY = layout.getHeight() - padSize / 2;
                                    if (padLeftY < padSize / 2) padLeftY = padSize / 2;
                                }
							}
						}
						if (arg1.getX(pointerID) > layout.getWidth() / 2) {
							if (rightPadId == -1) {
								rightPadId = pID;
								if (joystick.getStickRight().isEnableXAxis()) {
                                    padRightX = arg1.getX(pointerID);
                                    if (padRightX > layout.getWidth() - padSize/2) padRightX = layout.getWidth() - padSize/2;
                                    if (padRightX < layout.getWidth() / 2 + padSize/2) padRightX = layout.getWidth() / 2 + padSize/2;
                                }
								if (joystick.getStickRight().isEnableYAxis()) {
                                    padRightY = arg1.getY(pointerID);
                                    if (padRightY > layout.getHeight() - padSize / 2) padRightY = layout.getHeight() - padSize / 2;
                                    if (padRightY < padSize / 2) padRightY = padSize / 2;
                                }
							}
						}
						break;
					case MotionEvent.ACTION_MOVE:
						for (int i = 0; i < pointerCount; i++) {
							pID = arg1.getPointerId(i);
							if (leftPadId == pID) {
								if (joystick.getStickLeft().isEnableXAxis()) {
									padLeftX = arg1.getX(i);
                                    if (padLeftX > layout.getWidth() / 2 - padSize / 2) padLeftX = layout.getWidth() / 2 - padSize / 2;
                                    if (padLeftX < padSize / 2) padLeftX = padSize / 2;
								}
								if (joystick.getStickLeft().isEnableYAxis()) {
                                    padLeftY = arg1.getY(i);
                                    if (padLeftY > layout.getHeight() - padSize / 2) padLeftY = layout.getHeight() - padSize / 2;
                                    if (padLeftY < padSize / 2) padLeftY = padSize / 2;
                                }
							} else if (rightPadId == pID) {
								if (joystick.getStickRight().isEnableXAxis()) {
									padRightX = arg1.getX(i);
                                    if (padRightX > layout.getWidth() - padSize/2) padRightX = layout.getWidth() - padSize/2;
                                    if (padRightX < layout.getWidth() / 2 + padSize/2) padRightX = layout.getWidth() / 2 + padSize/2;
								}
								if (joystick.getStickRight().isEnableYAxis()) {
                                    padRightY = arg1.getY(i);
                                    if (padRightY > layout.getHeight() - padSize / 2) padRightY = layout.getHeight() - padSize / 2;
                                    if (padRightY < padSize / 2) padRightY = padSize / 2;
                                }
							}
						}
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_POINTER_UP:
					case MotionEvent.ACTION_CANCEL:
						if (leftPadId == pID) {
							leftPadId = -1;
							if (joystick.getStickLeft().getResetToDefaultPosition() == Thumb.ResetToDefaultPosition.Both) {
								padLeftX = padLeftDefaultX;
								padLeftY = padLeftDefaultY;
							} else if (joystick.getStickLeft().getResetToDefaultPosition() == Thumb.ResetToDefaultPosition.X) {
								padLeftX = padLeftDefaultX;
							} else if (joystick.getStickLeft().getResetToDefaultPosition() == Thumb.ResetToDefaultPosition.Y) {
								padLeftY = padLeftDefaultY;
							}
						} else if (rightPadId == pID) {
							rightPadId = -1;
							if (joystick.getStickRight().getResetToDefaultPosition() == Thumb.ResetToDefaultPosition.Both) {
								padRightX = padRightDefaultX;
								padRightY = padRightDefaultY;
							} else if (joystick.getStickRight().getResetToDefaultPosition() == Thumb.ResetToDefaultPosition.X) {
								padRightX = padRightDefaultX;
							} else if (joystick.getStickRight().getResetToDefaultPosition() == Thumb.ResetToDefaultPosition.Y) {
								padRightY = padRightDefaultY;
							}
						}
						break;
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
			drawJoystickOnScreen();
			updateJoystickValues();
			return true;
		}
	};

    Bitmap joystick_bmp = null;
    Bitmap background = null;

    private void drawJoystickOnScreen() {

		if (background == null) {
            background = Bitmap.createBitmap(layout.getWidth(), layout.getHeight(), Bitmap.Config.ARGB_8888);
        }
        if (joystick_bmp == null) {
            Bitmap joystick_resource = BitmapFactory.decodeResource(getResources(), R.drawable.joystick_top);
            joystick_bmp = Bitmap.createScaledBitmap(joystick_resource, padSize, padSize, false);
        }

		Canvas c = new Canvas(background);
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);

		int w = layout.getWidth();
		int h = layout.getHeight();

        paint.setColor(Color.argb(255, 220, 220, 220));
        c.drawRect(0, 0, w, h, paint);

        //paint.setColor(Color.argb(255, 220, 200, 200));
		//c.drawRect(w / 2, 0, w, h, paint);
        paint.setColor(Color.argb(255, 200, 200, 200));
        c.drawRect(w/2+ padSize/2, 0+ padSize/2, w- padSize/2, h- padSize/2, paint);

		//paint.setColor(Color.argb(255, 150, 170, 170));
		//c.drawRect(0, 0, w / 2, h, paint);
        paint.setColor(Color.argb(255, 150, 150, 150));
        c.drawRect(0 + padSize/2, 0+ padSize/2, w/2- padSize/2, h- padSize/2, paint);


		c.drawBitmap(joystick_bmp, padLeftX - padSize/2, padLeftY - padSize/2, null);
		c.drawBitmap(joystick_bmp, padRightX - padSize/2, padRightY - padSize/2, null);

		layout.setBackground(new BitmapDrawable(getResources(), background));
	}

	private final int PAD_MIN_DISPLAY_VAL = 0;
	private final int PAD_MAX_DISPLAY_VAL = 255;

	private void updateJoystickValues() {

		float cX1 = (padLeftX - padSize/2) / (layout.getWidth() / 2 - padSize);
		float cY1 = 1 - (padLeftY - padSize/2) / (layout.getHeight() - padSize); // (layout.getHeight() - padLeftY) / layout.getHeight();
		float cX2 = (padRightX - 3*padSize/2) / (layout.getWidth() / 2 - padSize) - 1; // (padRightX - (layout.getWidth() / 2)) / (layout.getWidth() / 2);
		float cY2 = 1 - (padRightY - padSize/2) / (layout.getHeight() - padSize); // (layout.getHeight() - padRightY) / layout.getHeight();

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

		joystick.getStickLeft().setXPosition(cX1);
		joystick.getStickLeft().setyPosition(cY1);
		joystick.getStickRight().setXPosition(cX2);
		joystick.getStickRight().setyPosition(cY2);

		cX1 = Proportions.linearProportion(cX1, _min, _max, PAD_MIN_DISPLAY_VAL, PAD_MAX_DISPLAY_VAL);
		cY1 = Proportions.linearProportion(cY1, _min, _max, PAD_MIN_DISPLAY_VAL, PAD_MAX_DISPLAY_VAL);
		cX2 = Proportions.linearProportion(cX2, _min, _max, PAD_MIN_DISPLAY_VAL, PAD_MAX_DISPLAY_VAL);
		cY2 = Proportions.linearProportion(cY2, _min, _max, PAD_MIN_DISPLAY_VAL, PAD_MAX_DISPLAY_VAL);

		txtLeftJoystickStatus.setText(Math.round(cX1) + " x " + Math.round(cY1));
		txtRightJoystickStatus.setText(Math.round(cX2) + " x " + Math.round(cY2));
	}
}

