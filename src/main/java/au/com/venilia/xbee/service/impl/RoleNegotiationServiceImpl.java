package au.com.venilia.xbee.service.impl;

import static au.com.venilia.xbee.service.ModuleDiscoveryService.ModuleGroup.CONTROLLERS;
import static au.com.venilia.xbee.service.RoleNegotiationService.Role.MASTER;
import static au.com.venilia.xbee.service.RoleNegotiationService.Role.SLAVE;

import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.digi.xbee.api.AbstractXBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;
import com.google.common.collect.Lists;

import au.com.venilia.xbee.event.ControllerModuleDetectionEvent;
import au.com.venilia.xbee.event.RoleChangeEvent;
import au.com.venilia.xbee.service.ModuleDiscoveryService;
import au.com.venilia.xbee.service.RoleNegotiationService;

@Service
public class RoleNegotiationServiceImpl implements RoleNegotiationService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private ModuleDiscoveryService moduleDiscoveryService;

    private Role role = MASTER; // default

    @Override
    public Role getRole() {

        return role;
    }

    @EventListener
    public void negotiateRoles(final ControllerModuleDetectionEvent event) throws XBeeException {

        // We quite simply look at the known controller modules (including this one) and take the one with lowest address as the master
        final List<AbstractXBeeDevice> allDevices = Lists.newArrayList(moduleDiscoveryService.getRemoteModules(CONTROLLERS));
        allDevices.add(moduleDiscoveryService.getLocalModule());

        allDevices.sort(new Comparator<AbstractXBeeDevice>() {

            @Override
            public int compare(final AbstractXBeeDevice bee1, final AbstractXBeeDevice bee2) {

                return ByteBuffer.wrap(bee1.get16BitAddress().getValue()).getInt()
                        - ByteBuffer.wrap(bee2.get16BitAddress().getValue()).getInt();
            }
        });

        // The first device in allDevices list is the master
        if (allDevices.indexOf(moduleDiscoveryService.getLocalModule()) == 0) {

            if (role != MASTER) {

                role = MASTER;
                eventPublisher.publishEvent(RoleChangeEvent.master());
            }
        } else {

            if (role != SLAVE) {

                role = SLAVE;
                eventPublisher.publishEvent(RoleChangeEvent.slave());
            }
        }
    }
}
