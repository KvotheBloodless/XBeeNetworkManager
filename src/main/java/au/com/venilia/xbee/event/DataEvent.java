package au.com.venilia.xbee.event;

import org.springframework.context.ApplicationEvent;

import au.com.venilia.xbee.service.NetworkCommunicationsService.PeerGroup;

public class DataEvent extends ApplicationEvent {

    private final PeerGroup peerGroup;

    private final byte[] data;

    public DataEvent(final Object source, final PeerGroup peerGroup, final byte[] data) {

        super(source);

        this.peerGroup = peerGroup;
        this.data = data;
    }

    public PeerGroup getPeerGroup() {

        return peerGroup;
    }

    public byte[] getData() {

        return data;
    }
}
