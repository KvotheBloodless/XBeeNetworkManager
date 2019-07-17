package au.com.venilia.network.service;

public interface ControllerRoleNegotiationService {

    /**
     * Retrieves the role this controller plays within the network
     * 
     * @return the role
     */
    public ControllerRole currentRole();

    public static enum ControllerRole {

        MASTER,
        SLAVE;
    }
}
