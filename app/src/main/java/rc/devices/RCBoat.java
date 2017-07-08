package rc.devices;

import rc.communication.WiFi_UDP;
import rc.joystick.Joystick;
import rc.joystick.Thumb;
import rc.utils.Proportions;

public class RCBoat extends Device {

    private final String SERVER_IP = "192.168.43.63";
    private final int SERVER_PORT = 8080;

    private final long transmitInterval = 20;

    private final int THROTTLE_NEUTRAL = 0;
    private final int THROTTLE_MAX = 2000;

    private final int STEER_NEUTRAL = 110;
    private final int STEER_LEFT_MAX = 125;
    private final int STEER_RIGHT_MAX = 65;

    private float joystick_control_range, joystick_control_center;
    private float min_forward_throttle, min_backward_throttle;
    private float min_steer_left, max_steer_left, min_steer_right, max_steer_right;

    // Function that returns byte values for given type
    private byte[] getShortBytesBE(short x) {
        byte[] ret = new byte[2];
        ret[0] = (byte)(x & 0xff);
        ret[1] = (byte)((x >> 8) & 0xff);
        return  ret;
    }

    public RCBoat(Joystick joystick) {
        setJoystick(joystick);

        WiFi_UDP link = new WiFi_UDP(this, SERVER_IP, SERVER_PORT);
        link.setTransmitIntervalMs(transmitInterval);
        link.disableRedundantData();
        setCommunicationLink(link);

        joystick.getStickLeft().setDefaultPosition(Thumb.ThumbPosition.CenterCenter);
        joystick.getStickLeft().setResetToDefaultPosition(Thumb.ResetToDefaultPosition.None);
        joystick.getStickLeft().setEnableXAxis(false);
        joystick.getStickRight().setDefaultPosition(Thumb.ThumbPosition.CenterCenter);
        joystick.getStickRight().setResetToDefaultPosition(Thumb.ResetToDefaultPosition.None);
        joystick.getStickRight().setEnableYAxis(false);

        joystick_control_range =  joystick.getStickLeft().getMaxValue() - joystick.getStickLeft().getMinValue();
        joystick_control_center = joystick.getStickLeft().getMinValue() + joystick_control_range / 2;

        min_forward_throttle = joystick_control_center + joystick_control_range / 4;
        min_backward_throttle = joystick_control_center - joystick_control_range / 4;

        min_steer_left = joystick_control_center - joystick_control_range / 4;
        max_steer_left = joystick_control_center - joystick_control_range / 2;
        min_steer_right = joystick_control_center + joystick_control_range / 4;
        max_steer_right = joystick_control_center + joystick_control_range / 2;
    }

    short[] data = new short[5];
    short throttle, rudder, engineP1, engineP2;

    @Override
    public Byte[] getData() {

        // If joystick is not set, return null
        if (getJoystick() == null) {
            return null;
        }

        // Get values from joystick
        float jThrottle = getJoystick().getStickLeft().getYPosition();
        float jSteer = getJoystick().getStickRight().getXPosition();


        // Throttle calculations
        // If throttle stick is pushed over 1/4 of the center
        // set maximum throttle
        if (jThrottle > min_forward_throttle) {
            throttle = THROTTLE_MAX;
            engineP1 = 1;
            engineP2 = 0;
        } else if (jThrottle < min_backward_throttle) {
            throttle = THROTTLE_MAX;
            engineP1 = 0;
            engineP2 = 1;
        } else {
            throttle = THROTTLE_NEUTRAL;
            engineP1 = 0;
            engineP2 = 0;
        }

        // Steer calculation
        if (jSteer > min_steer_right) {
            rudder = (short) Math.round(Proportions.linearProportion(jSteer, min_steer_right, max_steer_right, STEER_NEUTRAL, STEER_RIGHT_MAX));
        } else if (jSteer < min_steer_left) {
            rudder = (short) Math.round(Proportions.linearProportion(jSteer, min_steer_left, max_steer_left, STEER_NEUTRAL, STEER_LEFT_MAX));
        } else {
            rudder = STEER_NEUTRAL;
        }

        data[0] = throttle;
        data[1] = rudder;
        data[2] = engineP1;
        data[3] = engineP2;
        data[4] = 0;


        // Repack data for return
        Byte[] sendData = new Byte[data.length * Short.SIZE / Byte.SIZE];

        // Convert short array to byte array
        sendData[0] = getShortBytesBE(data[0])[0];
        sendData[1] = getShortBytesBE(data[0])[1];
        sendData[2] = getShortBytesBE(data[1])[0];
        sendData[3] = getShortBytesBE(data[1])[1];
        sendData[4] = getShortBytesBE(data[2])[0];
        sendData[5] = getShortBytesBE(data[2])[1];
        sendData[6] = getShortBytesBE(data[3])[0];
        sendData[7] = getShortBytesBE(data[3])[1];
        sendData[8] = getShortBytesBE(data[4])[0];
        sendData[9] = getShortBytesBE(data[4])[1];

        return sendData;
    }
}
