package au.com.venilia.xbee.service.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;

import au.com.venilia.xbee.service.ModuleCommunicationsService;
import au.com.venilia.xbee.service.ModuleDiscoveryService;
import au.com.venilia.xbee.service.ModuleDiscoveryService.ModuleGroup;

@Service
public class ModuleCommunicationsServiceImpl implements ModuleCommunicationsService {

    private static final Logger LOG = LoggerFactory.getLogger(ModuleCommunicationsServiceImpl.class);

    @Autowired
    private ModuleDiscoveryService moduleDiscoveryService;

    private BlockingQueue<Communication> communicationsQueue;

    @PostConstruct
    public void init() {

        communicationsQueue = new LinkedBlockingDeque<>();
        new Thread(new Runnable() {

            @Override
            public void run() {

                while (true)
                    try {

                        final Communication communication = communicationsQueue.take();

                        try {

                            LOG.debug("Sending {} to {}", communication.getData(), communication.getTarget());
                            moduleDiscoveryService.getLocalModule().sendData(communication.getTarget(), communication.getData());
                        } catch (final TimeoutException e) {

                            // TODO: log and notify discovery service
                            e.printStackTrace();
                        } catch (final XBeeException e) {

                            // TODO: log and notify discovery service
                            e.printStackTrace();
                        }
                    } catch (final InterruptedException e) {

                        e.printStackTrace();
                    }
            }
        }).start();
    }

    @Override
    public void pseudoBroadcast(final ModuleGroup moduleGroup, final byte[] data) {

        moduleDiscoveryService.getRemoteModules(moduleGroup).forEach(target -> communicate(target, data));
    }

    @Override
    public void communicate(final RemoteXBeeDevice target, final byte[] data) {

        communicationsQueue.add(Communication.as(target, data));
    }

    private static class Communication {

        private final RemoteXBeeDevice target;

        private final byte[] data;

        protected Communication(final RemoteXBeeDevice target, final byte[] data) {

            this.target = target;
            this.data = data;
        }

        public static Communication as(final RemoteXBeeDevice target, final byte[] data) {

            return new Communication(target, data);
        }

        public RemoteXBeeDevice getTarget() {

            return target;
        }

        public byte[] getData() {

            return data;
        }
    }
}
