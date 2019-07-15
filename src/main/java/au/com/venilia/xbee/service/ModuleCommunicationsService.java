package au.com.venilia.xbee.service;

import com.digi.xbee.api.RemoteXBeeDevice;

import au.com.venilia.xbee.service.ModuleDiscoveryService.ModuleGroup;

public interface ModuleCommunicationsService {

    public void pseudoBroadcast(final ModuleGroup moduleGroup, final byte[] data);

    public void communicate(final RemoteXBeeDevice target, final byte[] data);
}
