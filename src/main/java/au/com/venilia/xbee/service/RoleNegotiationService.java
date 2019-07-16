package au.com.venilia.xbee.service;

// TODO: This interface and implementation probably belong in a different package
public interface RoleNegotiationService {

    /**
     * Retrieves the role this controller plays within the network
     * 
     * @return the role
     */
    public Role currentRole();

    public static enum Role {

        MASTER,
        SLAVE;
    }
}
