package au.com.venilia.xbee.event;

import org.springframework.context.ApplicationEvent;

import com.digi.xbee.api.RemoteXBeeDevice;

public abstract class ModuleDetectionEvent extends ApplicationEvent {

    public ModuleDetectionEvent(final RemoteXBeeDevice device) {

        super(device);
    }

    public RemoteXBeeDevice getDevice() {

        return (RemoteXBeeDevice) getSource();
    }
}
