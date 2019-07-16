package au.com.venilia.xbee.event;

import static au.com.venilia.xbee.service.RoleNegotiationService.Role.MASTER;
import static au.com.venilia.xbee.service.RoleNegotiationService.Role.SLAVE;

import org.springframework.context.ApplicationEvent;

import au.com.venilia.xbee.service.RoleNegotiationService.Role;

public class LocalRoleChangeEvent extends ApplicationEvent {

    private final Role role;

    public LocalRoleChangeEvent(final Object source, final Role role) {

        super(source);

        this.role = role;
    }

    public Role getRole() {

        return role;
    }

    public static LocalRoleChangeEvent master(final Object source) {

        return new LocalRoleChangeEvent(source, MASTER);
    }

    public static LocalRoleChangeEvent slave(final Object source) {

        return new LocalRoleChangeEvent(source, SLAVE);
    }
}
