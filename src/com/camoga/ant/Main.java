package com.camoga.ant;

import java.io.IOException;
import java.util.logging.Logger;
import java.awt.GraphicsEnvironment;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.camoga.ant.gui.Window;
import com.camoga.ant.net.Client;

public class Main {

    public static Client client;
	public static final Logger LOG = Logger.getLogger("Client");
	public static final Version VERSION = new Version(1,1,2);
    public static Window window;

    public static void main(String[] args) throws IOException {
		Options options = new Options();
		
		options.addOption(Option.builder("h").longOpt("help").hasArg(false).desc("print help").build());
		options.addOption(Option.builder("ng").longOpt("nogui").hasArg(false).desc("run without GUI").build());                      
		options.addOption(Option.builder("nl").longOpt("nolog").hasArg(false).desc("run without log").build());                      
		options.addOption(Option.builder().longOpt("host").hasArg(true).desc("server address").build());                             
		options.addOption(Option.builder("u").longOpt("user").hasArg(true).desc("login username").build());                          
		options.addOption(Option.builder("w").hasArg(true).desc("number of 2d square grid ants").type(Number.class).build());        
		options.addOption(Option.builder("wh").hasArg(true).desc("number of 2d hexagonal grid ants").type(Number.class).build());    
		options.addOption(Option.builder("w3").hasArg(true).desc("number of 3d ants").type(Number.class).build());                   
		options.addOption(Option.builder("w4").hasArg(true).desc("number of 4d ants").type(Number.class).build());                   
		options.addOption(Option.builder("c").hasArg(false).desc("Run ant").build());
        options.addOption(Option.builder("ws").longOpt("soup").hasArg(true).numberOfArgs(4).desc("Run random soups").argName("threads rule number_soups iterations_per_soup").build());
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null;
		
		try {
			cmd = parser.parse(options, args);

			// if(cmd.hasOption("c")) {
			// 	runRule();
			// 	return;
			// }

			if(cmd.hasOption("h")) {
				formatter.printHelp("java -jar langton.jar <opt>", options);
				System.exit(0);
			}
			
			int workers2 = cmd.hasOption("w") ? ((Number) cmd.getParsedOptionValue("w")).intValue():0;
			int workershex = cmd.hasOption("wh") ? ((Number) cmd.getParsedOptionValue("wh")).intValue():0;
			int workers3 = cmd.hasOption("w3") ? ((Number) cmd.getParsedOptionValue("w3")).intValue():0;
			int workers4 = cmd.hasOption("w4") ? ((Number) cmd.getParsedOptionValue("w4")).intValue():0;
			boolean nolog = cmd.hasOption("nl");
			if(cmd.hasOption("u")) {
				String username = cmd.getOptionValue("u");
				System.out.print("Enter secret token: ");
				String secrettoken = new String(System.console().readPassword());
				if(username != null && secrettoken != null) {
					Client.username = username;
					Client.secrettoken = secrettoken;
				}
			}
			boolean gui = !GraphicsEnvironment.isHeadless() && !cmd.hasOption("ng");
			String host = cmd.hasOption("host") ? cmd.getOptionValue("host"):"app.langtonsant.es";
			if(workers2 == 0 && workershex == 0 && workers3 == 0 && workers4 == 0) workers2 = 1;
			initLogger(!nolog);
            client = new Client(host,3,workershex,workers3,workers4);
			if(gui)
				window = new Window(null);
			
		} catch(ParseException e) {
			System.out.println(e.getMessage());
			formatter.printHelp("java -jar langton.jar <opt>", options);
			System.exit(1);
		}
	}

    public static void initLogger(boolean log) {
        if(log) {
            LOG.setLevel(java.util.logging.Level.INFO);
            System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s%n");			
        } else {
            LOG.setLevel(java.util.logging.Level.WARNING);
        }
        LOG.info("Langton's Ant Client v"+VERSION);
    }
}
