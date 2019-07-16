package au.com.venilia.xbee;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@ComponentScan("au.com.venilia.xbee")
public class Config {

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {

        final ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();

        threadPoolTaskScheduler.setPoolSize(5);

        return threadPoolTaskScheduler;
    }
}
