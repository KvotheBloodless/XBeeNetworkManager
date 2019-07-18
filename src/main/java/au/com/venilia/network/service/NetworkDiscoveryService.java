package au.com.venilia.network.service;

import java.util.Set;

import au.com.venilia.network.service.NetworkCommunicationsService.PeerGroup;

public interface NetworkDiscoveryService {

    /**
     * Returns a unique numeric identifier of the local peer
     * 
     * @return the unique numeric identifier
     */
    public int getLocalInstanceId();

    /**
     * Returns unique numeric identifiers of the peers in a {@link PeerGroup}
     * 
     * @param peerGroup the peer group
     * @return the unique numeric identifiers
     */
    public Set<Integer> getPeerIds(final PeerGroup peerGroup);
}
