package au.com.venilia.xbee;

import org.apache.commons.cli.CommandLine;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("au.com.venilia.xbee")
public class Config implements ApplicationContextAware {

    private CommandLine cli;

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {

        cli = ((CliAnnotationConfigApplicationContext) applicationContext).getCommandLine();
    }

    @Bean
    public CommandLine cli() {

        return cli;
    }
}
