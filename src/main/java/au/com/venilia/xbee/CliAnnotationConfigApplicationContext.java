package au.com.venilia.xbee;

import org.apache.commons.cli.CommandLine;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class CliAnnotationConfigApplicationContext extends AnnotationConfigApplicationContext {

    private final CommandLine commandLine;

    public CliAnnotationConfigApplicationContext(final CommandLine commandLine) {

        this.commandLine = commandLine;
    }

    public CommandLine getCommandLine() {

        return commandLine;
    }
}
