package rc.devices;

import rc.communication.CommunicationLink;
import rc.joystick.Joystick;

/**
 * Created by Aleksandar Beserminji on 3/22/17.
 */

public abstract class Device {
    private CommunicationLink communicationLink;
    private Joystick joystick = null;

    // Communication link implementation should call this function
    // to get data prepared for transmitting
    public abstract Byte[] getData();

    public void setCommunicationLink(CommunicationLink communicationLink) { this.communicationLink = communicationLink; }
    public CommunicationLink getCommunicationLink() { return communicationLink; }

    public void setJoystick(Joystick joystick) { this.joystick = joystick; }
    public Joystick getJoystick() { return joystick; }
}
