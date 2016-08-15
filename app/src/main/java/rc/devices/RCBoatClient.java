package rc.devices;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import rc.joystick.JoystickActivity;
import rc.utils.Proportions;

public class RCBoatClient implements Runnable {

    JoystickActivity joystick;

    int joystick_control_range;
    int joystick_control_center;

    int min_forward_throttle;
    int min_backward_throttle;

    int min_steer_left;
    int max_steer_left;
    int min_steer_right;
    int max_steer_right;



    public RCBoatClient(JoystickActivity joystick) {
        this.joystick = joystick;

        joystick_control_range =  joystick.getCONTROL_MAX() - joystick.getCONTROL_MIN();
        joystick_control_center = joystick.getCONTROL_MIN() + joystick_control_range / 2;

        min_forward_throttle = joystick_control_center + joystick_control_range / 4;
        min_backward_throttle = joystick_control_center - joystick_control_range / 4;

        min_steer_left = joystick_control_center - joystick_control_range / 4;
        max_steer_left = joystick_control_center - joystick_control_range / 2;
        min_steer_right = joystick_control_center + joystick_control_range / 4;
        max_steer_right = joystick_control_center + joystick_control_range / 2;
    }

    private final int THROTTLE_NEUTRAL = 0;
        private final int THROTTLE_MIN = 254; // 225;
        private final int THROTTLE_MAX = 254;

        private final int STEER_NEUTRAL = 128;
        private final int STEER_LEFT_MIN = STEER_NEUTRAL;
        private final int STEER_RIGHT_MIN = STEER_NEUTRAL;
        private final int STEER_LEFT_MAX = 158; // 80;
        private final int STEER_RIGHT_MAX = 92; // 186;

        private int PIN_ENB = 0; // PWM
        private int PIN_IN4 = 0; // Course
        private int PIN_IN5 = 0; // Inverted IN4
        private int PIN_SERVO = 0; // PWM

        private final String SERVER_IP = "192.168.4.1";
        private final int SERVER_PORT = 80;


        public byte setBitsAsUnsignedByte(int x) {
            if (x > 255)
                return (byte) 0;
            if (x > 127)
                x = x - 256;
            return (byte) x;
        }

        void calculateControls() {
            // Get values from joystick
            float throttle = joystick.getCONTROL_LEFT_Y();
            float steer = joystick.getCONTROL_RIGHT_X();

            // Throttle calculations
            // If throttle stick is pushed over 1/4 of the center
            // set maximum throttle
            if (throttle > min_forward_throttle) {
                throttle = THROTTLE_MAX;
                PIN_IN4 = 1;
                PIN_IN5 = 0;
            } else if (throttle < min_backward_throttle) {
                throttle = THROTTLE_MAX;
                PIN_IN4 = 0;
                PIN_IN5 = 1;
            } else {
                throttle = THROTTLE_NEUTRAL;
                PIN_IN4 = 0;
                PIN_IN5 = 0;
            }
            PIN_ENB = Math.round(throttle);

            // Steer calculation
            if (steer > min_steer_right) {
                steer = Proportions.linearProportion(steer, min_steer_right, max_steer_right,
                        STEER_RIGHT_MIN, STEER_RIGHT_MAX);
            } else if (steer < min_steer_left) {
                steer = Proportions.linearProportion(steer, min_steer_left, max_steer_left,
                        STEER_LEFT_MIN, STEER_LEFT_MAX);
            } else {
                steer = STEER_NEUTRAL;
            }

            PIN_SERVO = Math.round(steer);
        }

        @Override
        public void run() {
            while(true) {
                try {
                    DatagramSocket clientSocket = new DatagramSocket();
                    InetAddress IPAddress = InetAddress.getByName(SERVER_IP);
                    byte[] sendData = new byte[5];

                    int[] data = new int[5];
                    while (true) {
                        calculateControls();
                        if (data[0] != PIN_SERVO || data[1] != PIN_ENB || data[2] != PIN_IN4 || data[3] != PIN_IN5) {
                            data[0] = PIN_SERVO;
                            data[1] = PIN_ENB;
                            data[2] = PIN_IN4;
                            data[3] = PIN_IN5;
                            data[4] = (data[0] + data[1] + data[2] + data[3]) % 255;

                            sendData[0] = setBitsAsUnsignedByte(data[0]);
                            sendData[1] = setBitsAsUnsignedByte(data[1]);
                            sendData[2] = setBitsAsUnsignedByte(data[2]);
                            sendData[3] = setBitsAsUnsignedByte(data[3]);
                            sendData[4] = setBitsAsUnsignedByte(data[4]);

                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, SERVER_PORT);
                            clientSocket.send(sendPacket);
                        }
                        Thread.sleep(200);

                    }
                    // clientSocket.close();
                } catch (SocketException e) {
                    //e.printStackTrace();
                } catch (UnknownHostException e) {
                    //e.printStackTrace();
                } catch (IOException e) {
                    //e.printStackTrace();
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // e.printStackTrace();
                }
            }
        }
}
