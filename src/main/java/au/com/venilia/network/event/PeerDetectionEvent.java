package au.com.venilia.network.event;

import org.springframework.context.ApplicationEvent;

import com.digi.xbee.api.RemoteXBeeDevice;

import au.com.venilia.network.service.NetworkCommunicationsService.PeerGroup;

public class PeerDetectionEvent extends ApplicationEvent {

    private final RemoteXBeeDevice peer;

    private final PeerGroup peerGroup;

    public PeerDetectionEvent(final Object source, final RemoteXBeeDevice peer, final PeerGroup peerGroup) {

        super(source);

        this.peer = peer;
        this.peerGroup = peerGroup;
    }

    public RemoteXBeeDevice getPeer() {

        return peer;
    }

    public PeerGroup getPeerGroup() {

        return peerGroup;
    }
}
