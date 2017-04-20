package rc.devices;

import rc.joystick.Joystick;
import rc.joystick.Thumb;
import rc.communication.WiFi_UDP;
import rc.utils.Proportions;

public class RCAirplane extends Device {

    // Configuration values
    private final String SERVER_IP = "192.168.0.152"; // Hardcoded server IP
    private final int SERVER_PORT = 8080; // Hardcoded port

    private final long transmitInterval = 20;

    private boolean motorsArmed = false;
    private boolean motorsRunning = false;
    private int motorsWaitingElapsed = 0;
    private final long motorsWaitingPeriod = 2000/transmitInterval;

    private boolean motorsArming = false;
    private boolean motorsDisarming = false;
    private final int throttleIncrement = 30;

    private final int THROTTLE_OFF = 0;
    private final int THROTTLE_ARM = 900;
    private final int THROTTLE_MIN = 1100;
    private final int THROTTLE_MAX = 2000;

    private final int AILERONS_MIN = 20;
    private final int AILERONS_MAX = 160;

    private final int ELEVATOR_MIN = 20;
    private final int ELEVATOR_MAX = 160;

    private final int RUDDER_MIN = 20;
    private final int RUDDER_MAX = 160;

    // Function that returns byte values for given type
    private byte[] getShortBytesBE(short x) {
        byte[] ret = new byte[2];
        ret[0] = (byte)(x & 0xff);
        ret[1] = (byte)((x >> 8) & 0xff);
        return  ret;
    }

    public RCAirplane(Joystick joystick) {
        setJoystick(joystick);

        WiFi_UDP link = new WiFi_UDP(this, SERVER_IP, SERVER_PORT);
        link.setTransmitIntervalMs(transmitInterval);
        link.disableRedundantData();
        setCommunicationLink(link);

        joystick.getStickLeft().setDefaultPosition(Thumb.ThumbPosition.BottomCenter);
        joystick.getStickRight().setDefaultPosition(Thumb.ThumbPosition.CenterCenter);
        joystick.getStickLeft().setResetToDefaultPosition(Thumb.ResetToDefaultPosition.X);
        joystick.getStickRight().setResetToDefaultPosition(Thumb.ResetToDefaultPosition.Both);
    }

    short[] data = new short[5];
    short throttle, rudder, elevator, ailerons;

    @Override
    public Byte[] getData() {
        // If joystick is not set, return null
        if (getJoystick() == null) {
            return null;
        }

        // Get values from joystick
        float jMin = getJoystick().getStickLeft().getMinValue();
        float jMax = getJoystick().getStickLeft().getMaxValue();
        float jThrottle = getJoystick().getStickLeft().getYPosition();
        float jRudder = getJoystick().getStickLeft().getXPosition();
        float jElevator = getJoystick().getStickRight().getYPosition();
        float jAilerons = getJoystick().getStickRight().getXPosition();

        float minThreshold = jMax * 0.05f;
        float maxThreshold = jMax * 0.95f;

        // Procedure for arming the engine
        if (!motorsArmed && !motorsArming) {
            if (jThrottle < minThreshold && jRudder < minThreshold ) {
                throttle = THROTTLE_OFF;
                motorsArming = true;
                motorsDisarming = false;
            }
        } else if (motorsArmed && !motorsDisarming){
            if (jThrottle < minThreshold && jRudder > maxThreshold) {
                throttle = THROTTLE_ARM;
                motorsDisarming = true;
                motorsArming = false;
            }
        }

        if (motorsArming) {
            throttle += throttleIncrement;
            if (throttle >= THROTTLE_ARM) {
                motorsArmed = true;
                motorsArming = false;
                motorsRunning = false;
                throttle = THROTTLE_ARM;
            }
        } else if (motorsDisarming) {
            throttle -= throttleIncrement;
            if (throttle <= THROTTLE_OFF) {
                motorsArmed = false;
                motorsDisarming = false;
                motorsRunning = false;
                throttle = THROTTLE_OFF;
            }
        } else if (motorsArmed) {
            if (motorsRunning) {
                throttle = (short) Math.round(Proportions.linearProportion(jThrottle, jMin, jMax, THROTTLE_MIN, THROTTLE_MAX));
            } else {
                if (motorsWaitingElapsed < motorsWaitingPeriod) {
                    motorsWaitingElapsed++;
                } else {
                    throttle += throttleIncrement;
                    if (throttle >= THROTTLE_MIN) {
                        motorsRunning = true;
                        throttle = THROTTLE_MIN;
                        motorsWaitingElapsed = 0;
                    }
                }
            }
        } else {
            throttle = THROTTLE_OFF;
        }

        // Scaling values to our desired values
        rudder = (short) Math.round(Proportions.linearProportion(jRudder, jMin, jMax, RUDDER_MIN, RUDDER_MAX));
        elevator = (short) Math.round(Proportions.linearProportion(jElevator, jMin, jMax, ELEVATOR_MIN, ELEVATOR_MAX));
        ailerons = (short) Math.round(Proportions.linearProportion(jAilerons, jMin, jMax, AILERONS_MIN, AILERONS_MAX));

        data[0] = throttle;
        data[1] = rudder;
        data[2] = elevator;
        data[3] = ailerons;
        data[4] = (short) ((throttle + rudder + elevator + ailerons) % 747);

        // Log.d("Sending data:", String.valueOf(throttle) + "  /  " + String.valueOf(rudder) + "  /  " + String.valueOf(elevator) + "  /  " + String.valueOf(ailerons));

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

