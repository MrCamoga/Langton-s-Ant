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
import com.camoga.ant.results.Result;
import com.camoga.ant.results.ResultRules;
import com.camoga.ant.results.ResultSoup;
import com.camoga.ant.results.ResultSoupRestore;

public class Main {

	public static Client client;
	public static final Logger LOG = Logger.getLogger("Client");
	public static final Version VERSION = new Version(1,2,1);
	public static Window window;

	public static void main(String[] args) throws IOException {
		Options options = new Options();
		
		options.addOption(Option.builder("h").longOpt("help").hasArg(false).desc("print help").build());
		options.addOption(Option.builder("ng").longOpt("nogui").hasArg(false).desc("run without GUI").build());                      
		options.addOption(Option.builder("nl").longOpt("nolog").hasArg(false).desc("run without log").build());                      
		options.addOption(Option.builder().longOpt("host").hasArg(true).argName("hostname").desc("server address").build());                             
		options.addOption(Option.builder("u").longOpt("user").hasArg(true).argName("username").desc("login username").build());                          
		options.addOption(Option.builder("w").hasArg(true).argName("num_threads").desc("number of 2d square grid ants").type(Number.class).build());        
		options.addOption(Option.builder("wh").hasArg(true).argName("num_threads").desc("number of 2d hexagonal grid ants").type(Number.class).build());    
		options.addOption(Option.builder("w3").hasArg(true).argName("num_threads").desc("number of 3d ants").type(Number.class).build());                   
		options.addOption(Option.builder("w4").hasArg(true).argName("num_threads").desc("number of 4d ants").type(Number.class).build());                   
		options.addOption(Option.builder("c").hasArg(false).desc("Run ant").build());
		options.addOption(Option.builder("ws").longOpt("soup").hasArgs().argName("threads rule number_soups iterations_per_soup | threads savefile").desc("Run random soups").build());
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(120);
		CommandLine cmd = null;
		
		try {
			cmd = parser.parse(options, args);

			// if(cmd.hasOption("c")) { // TODO restore code to run ant from file
			// 	runRule();
			// 	return;
			// }

			if(cmd.hasOption("h")) {
				formatter.printHelp("java -jar langton.jar <opt>", options);
				System.exit(0);
			}
			
			if(cmd.hasOption("w")) {
				int w = ((Number) cmd.getParsedOptionValue("w")).intValue();
				Result result = new ResultRules(0);
				result.addWorkers(w);
				WorkerManager.add(result);
			} 
			if(cmd.hasOption("wh")) {
				int w = ((Number) cmd.getParsedOptionValue("wh")).intValue();
				Result result = new ResultRules(1);
				result.addWorkers(w);
				WorkerManager.add(result);
			} 
			if(cmd.hasOption("w3")) {
				int w = ((Number) cmd.getParsedOptionValue("w3")).intValue();
				Result result = new ResultRules(2);
				result.addWorkers(w);
				WorkerManager.add(result);
			} 
			if(cmd.hasOption("w4")) {
				int w = ((Number) cmd.getParsedOptionValue("w4")).intValue();
				Result result = new ResultRules(3);
				result.addWorkers(w);
				WorkerManager.add(result);
			}
			if(cmd.hasOption("ws")) {
				String[] wsValues = cmd.getOptionValues("ws");
				int wsThreads = Integer.parseInt(wsValues[0]);
				Result result;
				if(wsValues.length == 2) {
					result = new ResultSoupRestore(wsValues[1]);
				} else {
					int wsRule = Integer.parseInt(wsValues[1]);
					int wsSize = Integer.parseInt(wsValues[2]);
					int wsIterations = Integer.parseInt(wsValues[3]);
					result = new ResultSoup(0, wsRule, null, 5, wsIterations, wsSize);
				}
				result.addWorkers(wsThreads);
				WorkerManager.add(result);
			}
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
			if(WorkerManager.getNumWorkers() == 0) WorkerManager.add(new ResultRules(0));
			initLogger(!nolog);
			client = new Client(host);
			if(gui)
				window = new Window();
			
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
