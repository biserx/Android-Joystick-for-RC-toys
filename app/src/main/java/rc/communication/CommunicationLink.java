package rc.communication;

import rc.devices.Device;

/**
 * Created by Aleksandar Beserminji on 3/22/17.
 */

public abstract class CommunicationLink implements Runnable {

    private Device device = null;

    public CommunicationLink(Device device) {
        this.device = device;
    }

    long transmitIntervalMs = 100;
    public void setTransmitIntervalMs(long mills) {
        transmitIntervalMs = mills;
    }


    boolean transmitRedundantData = false;

    public void enableRedundantData() {
        transmitRedundantData = true;
    }
    public void disableRedundantData() {
        transmitRedundantData = false;
    }

    // Overwrite this function in device implementation
    // And implement logic for transmitting data
    protected abstract boolean transmitData(Byte[] data) throws Exception;

    private Byte[] oldData = null;
    @Override
    public final void run() {
        while (!Thread.interrupted()) {

            boolean proceedWithTransmission = true;

            Byte[] data = device.getData();

            if (data == null) {
                proceedWithTransmission = false;
            } else if (!transmitRedundantData) {
                if (oldData == null) {
                    oldData = new Byte[data.length];
                    proceedWithTransmission = true;
                } else {
                    proceedWithTransmission = false;
                    for (int i = 0; i < data.length; i++) {
                        if (data[i] != oldData[i]) {
                            proceedWithTransmission = true;
                            oldData[i] = data[i];
                        }
                    }
                }
            }

            if (proceedWithTransmission) {
                try {
                    transmitData(data);
                } catch (Exception e) {
                    break;
                }
            }

            try {
                Thread.sleep(transmitIntervalMs);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
