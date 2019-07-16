package au.com.venilia.xbee.event;

import org.springframework.context.ApplicationEvent;

import com.digi.xbee.api.RemoteXBeeDevice;

import au.com.venilia.xbee.service.NetworkCommunicationsService.PeerGroup;

public class PeerDetectionEvent extends ApplicationEvent {

    private final RemoteXBeeDevice peer;

    private final PeerGroup moduleGroup;

    public PeerDetectionEvent(final Object source, final RemoteXBeeDevice peer, final PeerGroup moduleGroup) {

        super(source);

        this.peer = peer;
        this.moduleGroup = moduleGroup;
    }

    public RemoteXBeeDevice getPeer() {

        return peer;
    }

    public PeerGroup getModuleGroup() {

        return moduleGroup;
    }
}
