package au.com.venilia.xbee;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public class App {

    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    private static volatile boolean run = true;

    private final CliAnnotationConfigApplicationContext ctx;

    public App(final CommandLine commandLine) throws IOException {

        ctx = new CliAnnotationConfigApplicationContext(commandLine);
        ctx.register(Config.class);
        ctx.refresh();

        final Thread runThread = new Thread() {

            @Override
            public void run() {

                try {

                    while (run)
                        Thread.sleep(2000);

                    ctx.stop();
                } catch (final Exception ex) {

                    System.err.println("Failed to stop App");
                }
            }
        };

        runThread.start();
        try {

            runThread.join();
        } catch (InterruptedException e) {

            System.out.println("Run thread died");
        }
    }

    public void stop() {

        run = false;
    }

    public ApplicationContext getApplicationContext() {

        return ctx;
    }

    public static void main(String... args) throws ParseException {

        System.out.println("=======================");
        System.out.println("= XBeeNetworkManager !=");
        System.out.println("=======================");

        try {

            LOG.info("Launching XBee Network Manager");
            new App(parseCliArguments(args));
        } catch (final ParseException | IOException parseException) {

            System.exit(-1);
        }
    }

    private static CommandLine parseCliArguments(String... args) throws ParseException {

        final Options options = new Options();

        // final Option a = new Option("k", SIGNALK, true, "ex: --" + SIGNALK + " host:port");
        // a.setRequired(true);
        // options.addOption(a);

        final CommandLineParser parser = new DefaultParser();
        final HelpFormatter formatter = new HelpFormatter();

        try {

            final CommandLine commandLine = parser.parse(options, args);
            return commandLine;
        } catch (final ParseException pe) {

            System.out.println(String.format("Invalid parameters - %s", pe.getMessage()));
            formatter.printHelp("java -jar XBeeNetworkManager-[version].jar <options>", options);
            throw pe;
        }
    }
}
