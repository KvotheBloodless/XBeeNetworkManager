package au.com.venilia.network.service.xbee;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetryTemplate;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.models.XBeeMessage;

import au.com.venilia.network.event.DataEvent;
import au.com.venilia.network.service.NetworkCommunicationsService;

public class XBeeNetworkCommunicationsService implements NetworkCommunicationsService, IDataReceiveListener {

    private static final Logger LOG = LoggerFactory.getLogger(XBeeNetworkCommunicationsService.class);

    private final ApplicationEventPublisher eventPublisher;

    private final XBeeNetworkDiscoveryService xBeeNetworkDiscoveryService;

    private final RetryTemplate retryTemplate;

    private BlockingQueue<Communication> outgoingQueue;

    public XBeeNetworkCommunicationsService(final ApplicationEventPublisher eventPublisher,
            final XBeeNetworkDiscoveryService xBeeNetworkDiscoveryService,
            final RetryTemplate retryTemplate) {

        LOG.info("Creating network communications service");

        this.eventPublisher = eventPublisher;
        this.xBeeNetworkDiscoveryService = xBeeNetworkDiscoveryService;
        this.retryTemplate = retryTemplate;

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

                        LOG.debug("Sending {} to {}", new String(communication.getData()), communication.getPeer());

                        retryTemplate.execute(retryContext -> {

                            retryContext.setAttribute(RetryContext.NAME,
                                    String.format("Retry context to send %s to %s", new String(communication.getData()), communication.getPeer()));

                            try {
                            	
								xBeeNetworkDiscoveryService.getLocalInstance().sendData(communication.getPeer(),
										communication.getData());

								return null;
                            } catch (final XBeeException e) {

                                // If this fails permanently, the logging will be handled by the RetryListener
                                throw new RuntimeException(e);
                            }
                        });
                    } catch (final InterruptedException e) {

                        LOG.error("A {} was thrown - {}", e.getClass().getSimpleName(), e.getMessage(), e);
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

        eventPublisher.publishEvent(new DataEvent(this,
                PeerGroup.fromInstanceIdentifier(xbeeMessage.getDevice().getNodeID()), xbeeMessage.getData()));
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
