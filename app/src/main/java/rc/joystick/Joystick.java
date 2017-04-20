package rc.joystick;

/**
 * Created by Aleksandar Beserminji on 4/15/17.
 */

public class Joystick {

    Thumb stickLeft = null, stickRight = null;

    public Joystick() {
        stickLeft = new Thumb();
        stickRight = new Thumb();
    }

    public Thumb getStickLeft() {
        return stickLeft;
    }

    public Thumb getStickRight() {
        return stickRight;
    }

}
