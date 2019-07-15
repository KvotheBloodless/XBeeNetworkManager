package au.com.venilia.xbee.service;

public interface RoleNegotiationService {

    /**
     * Retrieves the role this controller plays within the network
     * 
     * @return the role
     */
    public Role getRole();

    public static enum Role {

        MASTER,
        SLAVE;
    }
}
