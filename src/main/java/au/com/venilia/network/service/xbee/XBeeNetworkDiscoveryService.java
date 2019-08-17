package au.com.venilia.network.service.xbee;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;

import com.digi.xbee.api.AbstractXBeeDevice;
import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IDiscoveryListener;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import au.com.venilia.network.event.PeerDetectionEvent;
import au.com.venilia.network.service.NetworkCommunicationsService.PeerGroup;
import au.com.venilia.network.service.NetworkDiscoveryService;

public class XBeeNetworkDiscoveryService implements NetworkDiscoveryService {

    private static final Logger LOG = LoggerFactory.getLogger(XBeeNetworkDiscoveryService.class);

    private final TaskScheduler scheduler;

    private final ApplicationEventPublisher eventPublisher;

    private final long discoveryRunDelaySeconds;

    private XBeeDevice localInstance;
    private final Multimap<PeerGroup, RemoteXBeeDevice> peers;
    private XBeeNetwork network;

    public XBeeNetworkDiscoveryService(final TaskScheduler scheduler, final ApplicationEventPublisher eventPublisher,
            final String serialDescriptor, final int baudRate, final long discoveryRunDelaySeconds) {

        LOG.info("Creating network discovery service for serial port {} and baud rate {}", serialDescriptor, baudRate);

        this.scheduler = scheduler;
        this.eventPublisher = eventPublisher;
        this.discoveryRunDelaySeconds = discoveryRunDelaySeconds;

        localInstance = new XBeeDevice(serialDescriptor, baudRate);
        peers = Multimaps.newSetMultimap(Maps.newHashMap(), () -> Sets.newHashSet());

        init();
    }

    private void init() {

        try {

            localInstance.open();

            network = localInstance.getNetwork();

            scheduler.scheduleWithFixedDelay(new Runnable() {

                @Override
                public void run() {

                    try {

                        performDiscovery();
                    } catch (final XBeeException e) {

                        LOG.error("A {} was thrown during discovery - {}", e.getClass().getSimpleName(), e.getMessage(),
                                e);
                    }
                }
            }, Duration.ofSeconds(discoveryRunDelaySeconds));
        } catch (final XBeeException e) {

            LOG.error("An {} was thrown opening connection to local module - {}", e.getClass().getSimpleName(),
                    e.getMessage(), e);
            // throw new RuntimeException(e);
        }
    }

    private IDiscoveryListener discoveryListener;

    private void performDiscovery() throws XBeeException {

        if (network.isDiscoveryRunning())
            throw new IllegalStateException("XBee module discovery is already in progress");

        LOG.info("Performing XBee module disovery on network {}", network);

        final Map<RemoteXBeeDevice, Boolean> deviceSeenDuringThisDiscovery = network.getDevices()
                .stream()
                .collect(Collectors.toMap(d -> d, d -> false));

        if (discoveryListener != null)
            network.removeDiscoveryListener(discoveryListener);

        discoveryListener = new IDiscoveryListener() {

            @Override
            public void deviceDiscovered(final RemoteXBeeDevice discoveredDevice) {

                if (!deviceSeenDuringThisDiscovery.containsKey(discoveredDevice)) {

                    LOG.debug("New module {} found during discovery process; adding to list.", discoveredDevice);

                    final PeerGroup peerGroup = PeerGroup.fromInstanceIdentifier(discoveredDevice.getNodeID());
                    peers.put(peerGroup, discoveredDevice);

                    eventPublisher.publishEvent(new PeerDetectionEvent(this, discoveredDevice, peerGroup));
                } else {

                    LOG.debug("Known module {} seen during discovery process", discoveredDevice);

                    deviceSeenDuringThisDiscovery.put(discoveredDevice, true);
                }
            }

            @Override
            public void discoveryFinished(final String error) {

                if (error != null)
                    throw new RuntimeException(new XBeeException(
                            String.format("An error occurred during remote XBee module discovery - %s", error)));

                deviceSeenDuringThisDiscovery.entrySet()
                        .stream()
                        .filter(e -> !e.getValue().booleanValue())
                        .map(e -> e.getKey())
                        .forEach(d -> {

                            LOG.debug("Module {} was not seen during discovery process; removing from list.", d);

                            peers.remove(PeerGroup.fromInstanceIdentifier(d.getNodeID()), d);
                            network.removeRemoteDevice(d);
                        });

                LOG.debug("Discovery completed. Remote XBee modules: {}", network.getDevices());
            }

            @Override
            public void discoveryError(final String error) {

                throw new RuntimeException(new XBeeException(
                        String.format("An error occurred during remote XBee module discovery - %s", error)));
            }
        };

        network.addDiscoveryListener(discoveryListener);

        // Start the discovery process
        network.startDiscoveryProcess();
    }

    public XBeeDevice getLocalInstance() {

        return localInstance;
    }

    public Set<RemoteXBeeDevice> getPeers(final PeerGroup peerGroup) {

        return (Set<RemoteXBeeDevice>) peers.get(peerGroup);
    }

    public Set<RemoteXBeeDevice> getPeers() {

        return (Set<RemoteXBeeDevice>) peers.values();
    }

    @PreDestroy
    private void shutdown() {

        if (localInstance.isOpen())
            localInstance.close();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns the low significant bits of the XBee module 16 bit address
     * </p>
     */
    @Override
    public int getLocalInstanceId() {

        return addressToId(localInstance);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns the low significant bits of the XBee module 16 bit address
     * </p>
     */
    @Override
    public Set<Integer> getPeerIds(final PeerGroup peerGroup) {

        return peers.get(peerGroup).stream().map(p -> addressToId(p)).collect(Collectors.toSet());
    }

    private static int addressToId(final AbstractXBeeDevice device) {

        return device.get16BitAddress().getLsb();
    }
}
