package au.com.venilia.xbee.service.impl;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.TaskScheduler;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IDiscoveryListener;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;

import au.com.venilia.xbee.event.ControllerModuleDetectionEvent;
import au.com.venilia.xbee.event.SwitchModuleDetectionEvent;
import au.com.venilia.xbee.service.ModuleDiscoveryService;

public class ModuleDiscoveryServiceImpl implements ModuleDiscoveryService {

    private static final Logger LOG = LoggerFactory.getLogger(ModuleDiscoveryServiceImpl.class);

    @Autowired
    private TaskScheduler scheduler;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private XBeeDevice localModule;

    private final Multimap<ModuleGroup, RemoteXBeeDevice> remoteModules;

    private XBeeNetwork network;

    public ModuleDiscoveryServiceImpl(final String serialDescriptor, final int baudRate) {

        localModule = new XBeeDevice(serialDescriptor, baudRate);
        remoteModules = Multimaps.newSetMultimap(Maps.newHashMap(), () -> Sets.newHashSet());
    }

    @PostConstruct
    private void init() throws XBeeException {

        try {

            localModule.open();

            network = localModule.getNetwork();

            scheduler.scheduleWithFixedDelay(new Runnable() {

                @Override
                public void run() {

                    try {

                        performDiscovery();
                    } catch (final XBeeException e) {

                        e.printStackTrace();
                    }
                }
            }, Duration.ofMinutes(1)); // Run discovery each minute
        } catch (final XBeeException e) {

            LOG.error("An {} was thrown opening connection to local module - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }
    }

    private IDiscoveryListener discoveryListener;

    private void performDiscovery() throws XBeeException {

        if (network.isDiscoveryRunning())
            throw new IllegalStateException("XBee module discovery is already in progress");

        LOG.info("Performing XBee module disovery on network {}", network);

        final Map<RemoteXBeeDevice, Boolean> deviceSeenDuringThisDiscovery =
                network.getDevices().stream().collect(Collectors.toMap(d -> d, d -> false));

        if (discoveryListener != null)
            network.removeDiscoveryListener(discoveryListener);

        discoveryListener = new IDiscoveryListener() {

            @Override
            public void deviceDiscovered(final RemoteXBeeDevice discoveredDevice) {

                if (!deviceSeenDuringThisDiscovery.containsKey(discoveredDevice)) {

                    LOG.debug("New module {} found during discovery process; adding to list.", discoveredDevice);

                    final ModuleGroup moduleGroup = ModuleGroup.fromNodeId(discoveredDevice.getNodeID());
                    remoteModules.put(moduleGroup, discoveredDevice);

                    switch (moduleGroup) {
                        case CONTROLLERS:
                            eventPublisher.publishEvent(new ControllerModuleDetectionEvent(discoveredDevice));
                            break;
                        case SWITCHES:
                            eventPublisher.publishEvent(new SwitchModuleDetectionEvent(discoveredDevice));
                            break;
                    }

                } else
                    deviceSeenDuringThisDiscovery.put(discoveredDevice, true);
            }

            @Override
            public void discoveryFinished(final String error) {

                if (error != null)
                    throw new RuntimeException(
                            new XBeeException(String.format("An error occurred during remote XBee module discovery - %s", error)));

                deviceSeenDuringThisDiscovery.entrySet()
                        .stream()
                        .filter(e -> !e.getValue().booleanValue())
                        .map(e -> e.getKey())
                        .forEach(d -> {

                            LOG.debug("Module {} was not seen during discovery process; removing from list.", d);

                            remoteModules.remove(ModuleGroup.fromNodeId(d.getNodeID()), d);
                            network.removeRemoteDevice(d);
                        });

                LOG.debug("Discovery completed. Remote XBee modules: {}", network.getDevices());
            }

            @Override
            public void discoveryError(final String error) {

                throw new RuntimeException(
                        new XBeeException(String.format("An error occurred during remote XBee module discovery - %s", error)));
            }
        };

        network.addDiscoveryListener(discoveryListener);

        // Start the discovery process
        network.startDiscoveryProcess();
    }

    @Override
    public XBeeDevice getLocalModule() {

        return localModule;
    }

    @Override
    public Set<RemoteXBeeDevice> getRemoteModules(final ModuleGroup moduleGroup) {

        return (Set<RemoteXBeeDevice>) remoteModules.get(moduleGroup);
    }

    @PreDestroy
    private void shutdown() {

        if (localModule.isOpen())
            localModule.close();
    }
}
