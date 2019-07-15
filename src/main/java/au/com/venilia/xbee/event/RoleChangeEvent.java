package au.com.venilia.xbee.event;

import static au.com.venilia.xbee.service.RoleNegotiationService.Role.MASTER;
import static au.com.venilia.xbee.service.RoleNegotiationService.Role.SLAVE;

import org.springframework.context.ApplicationEvent;

import au.com.venilia.xbee.service.RoleNegotiationService.Role;

public class RoleChangeEvent extends ApplicationEvent {

    public RoleChangeEvent(final Role role) {

        super(role);
    }

    public Role getRole() {

        return (Role) source;
    }

    public static RoleChangeEvent master() {

        return new RoleChangeEvent(MASTER);
    }

    public static RoleChangeEvent slave() {

        return new RoleChangeEvent(SLAVE);
    }
}
