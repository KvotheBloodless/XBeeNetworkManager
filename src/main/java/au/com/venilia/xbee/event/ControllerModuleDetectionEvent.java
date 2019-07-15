package au.com.venilia.xbee.event;

import com.digi.xbee.api.RemoteXBeeDevice;

public class ControllerModuleDetectionEvent extends ModuleDetectionEvent {

    public ControllerModuleDetectionEvent(final RemoteXBeeDevice device) {

        super(device);
    }
}
