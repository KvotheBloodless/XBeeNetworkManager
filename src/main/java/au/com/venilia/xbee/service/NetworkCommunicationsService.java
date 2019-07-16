package au.com.venilia.xbee.service;

public interface NetworkCommunicationsService {

    /**
     * Send message to all peers
     * 
     * @param data the message
     */
    public void send(final byte[] data);

    /**
     * Send message to all peers in a given peer group
     * 
     * @param peerGroup the peer group
     * @param data the message
     */
    public void send(final PeerGroup peerGroup, final byte[] data);

    public static enum PeerGroup {

        SWITCHES("SW"),
        CONTROLLERS("CT");

        public final static String SEPARATOR_CHAR = "$";

        private final String prefix;

        private PeerGroup(final String prefix) {

            this.prefix = prefix;
        }

        public static PeerGroup fromInstanceIdentifier(final String id) {

            for (final PeerGroup value : values()) {

                if (id.substring(0, id.indexOf(SEPARATOR_CHAR)).equals(value.prefix))
                    return value;
            }

            throw new IllegalArgumentException(String.format("Could not determine peer group from peer ID %s", id));
        }
    }
}
