package rc.devices;

import rc.communication.WiFi_UDP;
import rc.joystick.Joystick;
import rc.joystick.Thumb;
import rc.utils.Proportions;

public class RCBoat extends Device {

    private final String SERVER_IP = "192.168.4.1";
    private final int SERVER_PORT = 80;

    private final long transmitInterval = 20;

    private final int THROTTLE_NEUTRAL = 0;
    private final int THROTTLE_MAX = 255;

    private final int STEER_NEUTRAL = 128;
    private final int STEER_LEFT_MAX = 158;
    private final int STEER_RIGHT_MAX = 92;

    private float joystick_control_range, joystick_control_center;
    private float min_forward_throttle, min_backward_throttle;
    private float min_steer_left, max_steer_left, min_steer_right, max_steer_right;

    private byte setBitsAsUnsignedByte(int x) {
        byte b = (byte) x;
        return (byte) (b & 0xFF);
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

    @Override
    public Byte[] getData() {
        // If joystick is not set, return null
        if (getJoystick() == null) {
            return null;
        }

        // Get values from joystick
        float jThrottle = getJoystick().getStickLeft().getYPosition();
        float jSteer = getJoystick().getStickRight().getXPosition();

        int rudder; // PWM
        int throttle; // PWM
        int courseA; // Course (forward, backward)
        int courseB; // Inverted courseA

        // Throttle calculations
        // If throttle stick is pushed over 1/4 of the center
        // set maximum throttle
        if (jThrottle > min_forward_throttle) {
            throttle = THROTTLE_MAX;
            courseA = 1;
            courseB = 0;
        } else if (jThrottle < min_backward_throttle) {
            throttle = THROTTLE_MAX;
            courseA = 0;
            courseB = 1;
        } else {
            throttle = THROTTLE_NEUTRAL;
            courseA = 0;
            courseB = 0;
        }

        // Steer calculation
        if (jSteer > min_steer_right) {
            rudder = Math.round(Proportions.linearProportion(jSteer, min_steer_right, max_steer_right, STEER_NEUTRAL, STEER_RIGHT_MAX));
        } else if (jSteer < min_steer_left) {
            rudder = Math.round(Proportions.linearProportion(jSteer, min_steer_left, max_steer_left, STEER_NEUTRAL, STEER_LEFT_MAX));
        } else {
            rudder = STEER_NEUTRAL;
        }

        Byte[] sendData = new Byte[5];

        sendData[0] = setBitsAsUnsignedByte(rudder);
        sendData[1] = setBitsAsUnsignedByte(throttle);
        sendData[2] = setBitsAsUnsignedByte(courseA);
        sendData[3] = setBitsAsUnsignedByte(courseB);
        sendData[4] = setBitsAsUnsignedByte((rudder + throttle + courseA + courseB) % 255);


        return sendData;
    }
}
