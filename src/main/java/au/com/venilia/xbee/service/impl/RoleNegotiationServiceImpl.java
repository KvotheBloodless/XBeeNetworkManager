package au.com.venilia.xbee.service.impl;

import static au.com.venilia.xbee.service.RoleNegotiationService.Role.MASTER;
import static au.com.venilia.xbee.service.RoleNegotiationService.Role.SLAVE;

import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

import com.digi.xbee.api.AbstractXBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;
import com.google.common.collect.Lists;

import au.com.venilia.xbee.event.LocalRoleChangeEvent;
import au.com.venilia.xbee.event.PeerDetectionEvent;
import au.com.venilia.xbee.service.RoleNegotiationService;

public class RoleNegotiationServiceImpl implements RoleNegotiationService {

    private static final Logger LOG = LoggerFactory.getLogger(RoleNegotiationServiceImpl.class);

    private final ApplicationEventPublisher eventPublisher;

    private final XBeeNetworkDiscoveryService xBeeNetworkDiscoveryService;

    private Role role = MASTER; // default

    public RoleNegotiationServiceImpl(final ApplicationEventPublisher eventPublisher,
            final XBeeNetworkDiscoveryService xBeeNetworkDiscoveryService) {

        LOG.info("Creating role negotiation service");

        this.eventPublisher = eventPublisher;
        this.xBeeNetworkDiscoveryService = xBeeNetworkDiscoveryService;
    }

    @Override
    public Role currentRole() {

        return role;
    }

    @EventListener(
            condition = "#event.moduleGroup == T(au.com.venilia.xbee.service.ModuleDiscoveryService.ModuleGroup).CONTROLLERS")
    public void negotiateRoles(final PeerDetectionEvent event) throws XBeeException {

        // We quite simply look at the known modules (including this one) and take the one with lowest address as the master
        final List<AbstractXBeeDevice> allDevices = Lists.newArrayList(xBeeNetworkDiscoveryService.getPeers(event.getModuleGroup()));
        allDevices.add(xBeeNetworkDiscoveryService.getLocalInstance());

        allDevices.sort(new Comparator<AbstractXBeeDevice>() {

            @Override
            public int compare(final AbstractXBeeDevice bee1, final AbstractXBeeDevice bee2) {

                return ByteBuffer.wrap(bee1.get16BitAddress().getValue()).getInt()
                        - ByteBuffer.wrap(bee2.get16BitAddress().getValue()).getInt();
            }
        });

        // The first device in allDevices list is the master
        if (allDevices.indexOf(xBeeNetworkDiscoveryService.getLocalInstance()) == 0) {

            if (role != MASTER) {

                role = MASTER;
                eventPublisher.publishEvent(LocalRoleChangeEvent.master(this));
            }
        } else {

            if (role != SLAVE) {

                role = SLAVE;
                eventPublisher.publishEvent(LocalRoleChangeEvent.slave(this));
            }
        }
    }
}
