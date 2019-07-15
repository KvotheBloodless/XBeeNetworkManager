package au.com.venilia.xbee.service;

import java.util.Set;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeDevice;

/**
 * Describes a service capable of performing analysis of the XBee network
 */
public interface ModuleDiscoveryService {

    /**
     * @return the local XBee module
     */
    public XBeeDevice getLocalModule();

    /**
     * @return the node IDs of the remote XBee modules from a given group
     */
    public Set<RemoteXBeeDevice> getRemoteModules(final ModuleGroup moduleGroup);

    public static enum ModuleGroup {

        SWITCHES("SW"),
        CONTROLLERS("CT");

        public final static String SEPARATOR_CHAR = "$";

        private final String prefix;

        private ModuleGroup(final String prefix) {

            this.prefix = prefix;
        }

        public static ModuleGroup fromNodeId(final String nodeId) {

            for (final ModuleGroup value : values()) {

                if (nodeId.substring(0, nodeId.indexOf(SEPARATOR_CHAR)).equals(value.prefix))
                    return value;
            }

            throw new IllegalArgumentException(String.format("Could not determine module group from node ID %s", nodeId));
        }

        public String getPrefix() {

            return prefix;
        }
    }
}
