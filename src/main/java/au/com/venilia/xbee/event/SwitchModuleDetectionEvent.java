package au.com.venilia.xbee.event;

import com.digi.xbee.api.RemoteXBeeDevice;

public class SwitchModuleDetectionEvent extends ModuleDetectionEvent {

    public SwitchModuleDetectionEvent(final RemoteXBeeDevice device) {

        super(device);
    }
}
