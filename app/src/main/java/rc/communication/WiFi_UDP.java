package rc.communication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import rc.devices.Device;

/**
 * Created by Aleksandar Beserminji on 3/22/17.
 */

public class WiFi_UDP extends CommunicationLink {

    String serverIP;
    int serverPort;

    public WiFi_UDP(final Device device, String serverIP, int serverPort) {
        super(device);
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    DatagramSocket clientSocket = null;
    InetAddress IPAddress = null;

    @Override
    protected boolean transmitData(Byte[] data) throws Exception {
        if (clientSocket == null || clientSocket.isClosed()) {
            try {
                clientSocket = new DatagramSocket();
                IPAddress = InetAddress.getByName(serverIP);
            } catch (Exception e) {
                return false;
            }
        }

        int i = 0;
        byte[] _data = new byte[data.length];
        for (Byte b : data) {
            _data[i++] = b;
        }

        DatagramPacket sendPacket = new DatagramPacket(_data, _data.length, IPAddress, serverPort);
        try {
            clientSocket.send(sendPacket);
        } catch (IOException e) {
            return false;
        }

        return true;
    }
}
