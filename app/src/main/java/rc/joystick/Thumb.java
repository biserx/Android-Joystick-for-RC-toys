package rc.joystick;

/**
 * Created by Aleksandar Beserminji on 4/15/17.
 */

public class Thumb {

    private final float minValue = 0f;
    private final float maxValue = 1f;

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public enum ThumbPosition {
        UpLeft(0),
        TopCenter(1),
        TopRight(2),
        CenterLeft(3),
        CenterCenter(4),
        CenterRight(5),
        BottomLeft(6),
        BottomCenter(7),
        BottomRight(8);
        final int value;
        ThumbPosition(int value) {
            this.value = value;
        }
        public int getPositionX() {
            return value % 3;
        }
        public int getPositionY() {
            return value / 3;
        }
    }

    public enum ResetToDefaultPosition {
        None,
        X,
        Y,
        Both
    }

    private ThumbPosition defaultPosition = ThumbPosition.CenterCenter;
    ResetToDefaultPosition resetToDefaultPosition = ResetToDefaultPosition.None;
    private boolean enableXAxis = true;
    private boolean enableYAxis = true;
    private float xPosition = 0f;
    private float yPosition = 0f;

    public ThumbPosition getDefaultPosition() {
        return defaultPosition;
    }

    public void setDefaultPosition(ThumbPosition defaultPosition) {
        this.defaultPosition = defaultPosition;
        xPosition = defaultPosition.getPositionX() * 0.5f;
        yPosition = (2 - defaultPosition.getPositionY()) * 0.5f;
    }

    public ResetToDefaultPosition getResetToDefaultPosition() {
        return resetToDefaultPosition;
    }

    public void setResetToDefaultPosition(ResetToDefaultPosition resetToDefaultPosition) {
        this.resetToDefaultPosition = resetToDefaultPosition;
    }

    public boolean isEnableXAxis() {
        return enableXAxis;
    }

    public void setEnableXAxis(boolean enableXAxis) {
        this.enableXAxis = enableXAxis;
    }

    public boolean isEnableYAxis() {
        return enableYAxis;
    }

    public void setEnableYAxis(boolean enableYAxis) {
        this.enableYAxis = enableYAxis;
    }

    public float getXPosition() {
        return xPosition;
    }

    public void setXPosition(float xPosition) {
        this.xPosition = xPosition;
    }

    public float getYPosition() {
        return yPosition;
    }

    public void setyPosition(float yPosition) {
        this.yPosition = yPosition;
    }

}
