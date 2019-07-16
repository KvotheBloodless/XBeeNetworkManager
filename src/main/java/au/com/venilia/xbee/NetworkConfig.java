package au.com.venilia.xbee;

import org.apache.commons.cli.CommandLine;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import au.com.venilia.xbee.service.NetworkCommunicationsService;
import au.com.venilia.xbee.service.RoleNegotiationService;
import au.com.venilia.xbee.service.impl.RoleNegotiationServiceImpl;
import au.com.venilia.xbee.service.impl.XBeeNetworkCommunicationsService;
import au.com.venilia.xbee.service.impl.XBeeNetworkDiscoveryService;

@Configuration
public class NetworkConfig implements ApplicationContextAware {

    private CommandLine cli;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {

        cli = ((CliAnnotationConfigApplicationContext) applicationContext).getCommandLine();
    }

    @Bean
    public XBeeNetworkDiscoveryService xBeeNetworkDiscoveryService(
            final ThreadPoolTaskScheduler scheduler,
            final ApplicationEventPublisher eventPublisher) {

        return new XBeeNetworkDiscoveryService(
                scheduler,
                eventPublisher,
                cli.getOptionValue(App.XBEE),
                Integer.parseInt(cli.getOptionValue(App.XBEE_RATE)));
    }

    @Bean
    public NetworkCommunicationsService moduleCommunicationsService(final ApplicationEventPublisher eventPublisher,
            final XBeeNetworkDiscoveryService xBeeNetworkDiscoveryService) {

        return new XBeeNetworkCommunicationsService(eventPublisher, xBeeNetworkDiscoveryService);
    }

    @Bean
    public RoleNegotiationService roleNegotiationService(final ApplicationEventPublisher eventPublisher,
            final XBeeNetworkDiscoveryService xBeeNetworkDiscoveryService) {

        return new RoleNegotiationServiceImpl(eventPublisher, xBeeNetworkDiscoveryService);
    }
}
