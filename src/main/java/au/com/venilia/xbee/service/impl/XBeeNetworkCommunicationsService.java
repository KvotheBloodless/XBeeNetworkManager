package au.com.venilia.xbee.service.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.models.XBeeMessage;

import au.com.venilia.xbee.event.DataEvent;
import au.com.venilia.xbee.service.NetworkCommunicationsService;

public class XBeeNetworkCommunicationsService implements NetworkCommunicationsService, IDataReceiveListener {

    private static final Logger LOG = LoggerFactory.getLogger(XBeeNetworkCommunicationsService.class);

    private final ApplicationEventPublisher eventPublisher;

    private final XBeeNetworkDiscoveryService xBeeNetworkDiscoveryService;

    private BlockingQueue<Communication> outgoingQueue;

    public XBeeNetworkCommunicationsService(final ApplicationEventPublisher eventPublisher,
            final XBeeNetworkDiscoveryService xBeeNetworkDiscoveryService) {

        LOG.info("Creating module communications service");

        this.eventPublisher = eventPublisher;
        this.xBeeNetworkDiscoveryService = xBeeNetworkDiscoveryService;
        
        init();
    }

    private void init() {

        xBeeNetworkDiscoveryService.getLocalInstance().addDataListener(this);

        outgoingQueue = new LinkedBlockingDeque<>();
        new Thread(new Runnable() {

            @Override
            public void run() {

                while (true)
                    try {

                        final Communication communication = outgoingQueue.take();

                        try {

                            LOG.debug("Sending {} to {}", communication.getData(), communication.getPeer());
                            xBeeNetworkDiscoveryService.getLocalInstance().sendData(communication.getPeer(), communication.getData());
                        } catch (final TimeoutException e) {

                            // TODO: log and notify discovery service
                            e.printStackTrace();
                        } catch (final XBeeException e) {

                            // TODO: log and notify discovery service
                            e.printStackTrace();
                        }
                    } catch (final InterruptedException e) {

                        e.printStackTrace();
                    }
            }
        }).start();
    }

    @Override
    public void send(final byte[] data) {

        xBeeNetworkDiscoveryService.getPeers().forEach(target -> send(target, data));
    }

    @Override
    public void send(final PeerGroup peerGroup, final byte[] data) {

        xBeeNetworkDiscoveryService.getPeers(peerGroup).forEach(target -> send(target, data));
    }

    private void send(final RemoteXBeeDevice target, final byte[] data) {

        outgoingQueue.add(Communication.as(target, data));
    }

    @Override
    public void dataReceived(final XBeeMessage xbeeMessage) {

        eventPublisher.publishEvent(new DataEvent(this, PeerGroup.fromInstanceIdentifier(xbeeMessage.getDevice().getNodeID()), xbeeMessage.getData()));
    }

    private static class Communication {

        private final RemoteXBeeDevice peer;

        private final byte[] data;

        protected Communication(final RemoteXBeeDevice target, final byte[] data) {

            this.peer = target;
            this.data = data;
        }

        public static Communication as(final RemoteXBeeDevice peer, final byte[] data) {

            return new Communication(peer, data);
        }

        public RemoteXBeeDevice getPeer() {

            return peer;
        }

        public byte[] getData() {

            return data;
        }
    }
}
