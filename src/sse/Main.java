/*******************************************************************************
 * Copyright (c) 2011-2013 Jochen Rill.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Jochen Rill - initial API and implementation
 ******************************************************************************/
package sse;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.ini4j.Ini;

import sse.Backend.AmazonBackend;
import sse.Backend.Backend;
import sse.Backend.FileSystemBackend;
import sse.Graph.DAWG;
import sse.IOHandler.EncryptionEngine;
import sse.IOHandler.SearchEngine;

/**
 * This is the main class of the program. It manages the different input options
 * and reads the config file.
 * 
 * @author Jochen Rill
 * 
 */
public class Main {

	public static void main(String[] args) {

		CommandLineParser parser = new GnuParser();

		try {
			CommandLine cmd = parser.parse(buildOptions(), args);
			Ini config = new Ini(new File("sse.config"));

			String chosenBackend = cmd.getOptionValue("backend", "file-system");

			if (cmd.hasOption("v")) {

				Constants.DEBUG = true;
			}
			if (cmd.hasOption("search")) {

				Backend b;

				if (chosenBackend.equals("amazon")) {

					if (config.containsKey("amazon")) {

						System.out.println("[INFO:] Using Amazon backend.");
						b = new AmazonBackend(config.get("amazon", "key"),
								config.get("amazon", "skey"),
								cmd.getOptionValue("i", "block"), config.get(
										"amazon", "bucket"), cmd
										.getOptionValue("password")
										.toCharArray());

					} else {
						throw new ParseException(
								"No Amazon Credentials found. Search can't be performed.");
					}

				} else {

					System.out.println("[INFO:] Using Filesystem backend.");

					b = new FileSystemBackend(cmd.getOptionValue("i", "block"),
							cmd.getOptionValue("password").toCharArray(),
							cmd.getOptionValue("iv", "iv"));

				}
				double time = System.currentTimeMillis();
				SearchEngine sEn = new SearchEngine(b);
				System.out.println(sEn.find(cmd.getOptionValue("search")));
				if (Constants.DEBUG) {
					System.out.println("Search time: "
							+ ((System.currentTimeMillis() - time) / 1000));
				}

			} else if (cmd.hasOption("create")) {

				String input = FileUtils.readFileToString(new File(cmd
						.getOptionValue("i")));

				double generalTime = System.currentTimeMillis();
				double time = System.currentTimeMillis();
				DAWG t = new DAWG(input);

				if (Constants.DEBUG) {
					System.out
							.println("Excecution time for generating the graph: "
									+ ((System.currentTimeMillis() - time) / 1000));
				}
				time = System.currentTimeMillis();

				// Runtime r = Runtime.getRuntime();

				// t.printToFile("graph");
				// System.gc();
				// System.gc();
				// System.out
				// .println(((r.totalMemory() - r.freeMemory()) / (1024 *
				// 1024)));
				// System.exit(0);

				Backend b;
				if (chosenBackend.equals("amazon")) {
					if (config.containsKey("amazon")) {
						System.out.println("[INFO:] Using Amazon backend.");

						b = new AmazonBackend(config.get("amazon", "key"),
								config.get("amazon", "skey"),
								cmd.getOptionValue("o", "block"), config.get(
										"amazon", "bucket"), cmd
										.getOptionValue("password")
										.toCharArray());
					} else {
						throw new ParseException(
								"Amazon credentials can't be found. Exiting.");
					}

				} else {

					System.out.println("[INFO:] Using filesystem backend.");

					b = new FileSystemBackend(cmd.getOptionValue("o", "block"),
							cmd.getOptionValue("password").toCharArray(),
							cmd.getOptionValue("iv", "iv"));

				}

				EncryptionEngine w = new EncryptionEngine(b);
				w.writeBlocks(t);

				if (Constants.DEBUG) {
					System.out.println("Excecution time for output: "
							+ ((System.currentTimeMillis() - time) / 1000));
				}
				if (Constants.DEBUG) {
					System.out
							.println("Total excecution time: "
									+ ((System.currentTimeMillis() - generalTime) / 1000));
				}

			} else if (cmd.hasOption("h") || cmd.hasOption("help")) {
				HelpFormatter h = new HelpFormatter();
				h.printHelp("Usage: main [options]", buildOptions());

			} else {
				throw new ParseException(
						"Either create or search must be specified");
			}

		} catch (ParseException e) {
			System.out.println("Can't parse options: " + e.getMessage());

		} catch (IOException e) {
			System.out.println("File not found.");
		}

	}

	@SuppressWarnings("static-access")
	private static Options buildOptions() {
		Options options = new Options();

		Option input = OptionBuilder
				.hasArg()
				.withArgName("file")
				.isRequired()
				.withDescription(
						"The basename of the input files. Can be either a block for search mode or a text for create mode.")
				.create("i");
		OptionGroup mode = new OptionGroup();

		Option output = OptionBuilder.hasArg().withArgName("file")
				.withDescription("The basename of the output files.")
				.create("o");

		Option iv = OptionBuilder.hasArg().withArgName("file")
				.withDescription("The file wich contains the iv and salt.")
				.create("iv");
		Option help = new Option("help", "Prints the help message.");
		Option verbose = new Option("v", "Enables verbose mode.");
		Option backend = OptionBuilder
				.hasArg()
				.withArgName("backend")
				.withDescription(
						"Sets the backend which is to be used. Either amazon or file-system.")
				.create("backend");

		Option create = new Option("create", "Encrypts the given text");

		Option search = OptionBuilder.hasArg().withArgName("text")
				.withDescription("Searches for the given text.")
				.create("search");

		Option password = OptionBuilder.hasArg().withArgName("password")
				.withDescription("Sets the password used for encryption.")
				.isRequired().create("password");

		mode.addOption(create);
		mode.addOption(search);
		options.addOption(output);
		options.addOptionGroup(mode);

		options.addOption(input);
		options.addOption(iv);
		options.addOption(help);
		options.addOption(verbose);
		options.addOption(backend);
		options.addOption(password);

		return options;

	}
}
