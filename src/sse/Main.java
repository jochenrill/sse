package sse;

import java.io.File;
import java.io.IOException;

import java.util.LinkedList;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.ini4j.Ini;

import sse.Backend.AmazonBackend;
import sse.Backend.FileSystemBackend;
import sse.Backend.GoogleBackend;
import sse.Backend.SmartdriveBackend;
import sse.Graph.DAWG;
import sse.Graph.Node;
import sse.IOHandler.BinaryWriter;
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

		Options options = new Options();

		options.addOption("i", true, "Defines the input file.");
		options.addOption("o", true,
				"Defines the output file. \"default.vc\" if no file is given.");
		options.addOption("h", false, "Prints help");
		options.addOption("help", false, "Prints this help message");
		options.addOption("v", false, "Verbose");
		options.addOption("amazon", false,
				"Uploads to Amazon S3. Credentials must be specified in sse.config.");
		options.addOption("smartdrive", false,
				"Uploads to 1&1 Smartdrive. Credentials must be specified in sse.config.");
		options.addOption(
				"google",
				false,
				"Uploads to Google Cloud Services. Credentials must be specified in sse.config.");
		options.addOption("search", false, "Turns on search mode");
		options.addOption(
				"password",
				true,
				"Provides the password for search and encryption. If the password is not provided, the user will be prompted to enter it.");
		options.addOption("create", false, "Turns on creation mode");
		options.addOption("text", true,
				"If search mode is used, this is the text that will be searched");

		CommandLineParser parser = new GnuParser();

		try {
			CommandLine cmd = parser.parse(options, args);
			Ini config = new Ini(new File("sse.config"));

			// parse configuration
			if (config.containsKey("general")) {
				Constants.ALPHABET_SIZE = Short.parseShort(config.get(
						"general", "alphabet_size"));
				Constants.DEBUG = Boolean.parseBoolean(config.get("general",
						"debug"));
				Constants.EDGE_REFERENCE_BYTES = Short.parseShort(config.get(
						"general", "edge_reference_bytes"));
				Constants.BLOCK_REFERENCE_BYTES = Short.parseShort(config.get(
						"general", "block_reference_bytes"));
				Constants.NUMOCCURS_BYTES = Short.parseShort(config.get(
						"general", "numoccurs_byte"));

				Constants.PADDING_BYTE = (char) Byte.parseByte(config.get(
						"general", "padding_byte"));

				Constants.VECTOR_MARKER = config
						.get("general", "vector_marker").charAt(0);
				Constants.VECTOR_SIZE_MULTI = Short.parseShort(config.get(
						"general", "vector_size_multi"));

			}

			if (cmd.hasOption("v")) {

				Constants.DEBUG = true;
			}
			if (cmd.hasOption("search")) {
				// We need a key file, a search text and an input text for
				// searching

				if (cmd.hasOption("i")) {
					if (cmd.hasOption("text")) {

						if (cmd.hasOption("amazon")) {
							if (config.containsKey("amazon")) {
								char[] password;
								if (cmd.hasOption("password")) {
									password = cmd.getOptionValue("password")
											.toCharArray();
								} else {
									password = System.console().readPassword(
											"[%s]:", "Password");
								}
								System.out
										.println("[INFO:] Using Amazon backend.");
								SearchEngine sEn = new SearchEngine(

								new AmazonBackend(config.get("amazon", "key"),
										config.get("amazon", "skey"),
										cmd.getOptionValue("i"), config.get(
												"amazon", "bucket")), password);
								System.out.println(sEn.find(cmd
										.getOptionValue("text")));
								System.out.println("Files opened:"
										+ sEn.getTransferedFilesCount());
							} else {
								System.out
										.println("No Amazon Credentials found. Search can't be performed.");
							}
						} else if (cmd.hasOption("google")) {
							if (config.containsKey("google")) {
								char[] password;
								if (cmd.hasOption("password")) {
									password = cmd.getOptionValue("password")
											.toCharArray();
								} else {
									password = System.console().readPassword(
											"[%s]:", "Password");
								}
								System.out
										.println("[INFO:] Using Google backend.");

								SearchEngine sEn = new SearchEngine(

								new GoogleBackend(config.get("google", "key"),
										config.get("google", "skey"),
										cmd.getOptionValue("i"), config.get(
												"google", "bucket")), password);
								System.out.println(sEn.find(cmd
										.getOptionValue("text")));
								System.out.println("Files opened:"
										+ sEn.getTransferedFilesCount());
							} else {
								System.out
										.println("No Google Credentials found. Search can't be performed.");
							}
						} else if (cmd.hasOption("smartdrive")) {
							if (config.containsKey("smartdrive")) {
								char[] password;
								if (cmd.hasOption("password")) {
									password = cmd.getOptionValue("password")
											.toCharArray();
								} else {
									password = System.console().readPassword(
											"[%s]:", "Password");
								}
								System.out
										.println("[INFO:] Using Smartdrive backend.");

								SearchEngine sEn = new SearchEngine(

								new SmartdriveBackend(config.get("smartdrive",
										"url"),
										config.get("smartdrive", "user"),
										config.get("smartdrive", "password"),
										cmd.getOptionValue("i")), password);
								System.out.println(sEn.find(cmd
										.getOptionValue("text")));
								System.out.println("Files opened:"
										+ sEn.getTransferedFilesCount());
							} else {
								System.out
										.println("No Smartdrive Credentials found. Search can't be performed.");
							}
						} else {
							char[] password;
							if (cmd.hasOption("password")) {
								password = cmd.getOptionValue("password")
										.toCharArray();
							} else {
								password = System.console().readPassword(
										"[%s]:", "Password");
							}
							System.out
									.println("[INFO:] Using Filesystem backend.");

							SearchEngine sEn = new SearchEngine(

							new FileSystemBackend(cmd.getOptionValue("i")),
									password);
							System.out.println(sEn.find(cmd
									.getOptionValue("text")));
							System.out.println("Files opened: "
									+ sEn.getTransferedFilesCount());
						}

					} else {
						throw new ParseException("Text argument missing");
					}
				} else {
					throw new ParseException("Input argument is needed");
				}

			} else if (cmd.hasOption("create")) {

				String input = null;
				String outputFile = null;
				if (cmd.hasOption("i")) {
					File inputFile = new File(cmd.getOptionValue("i"));

					input = FileUtils.readFileToString(inputFile);

				} else {
					throw new MissingOptionException(
							"You must define an input, either a string or a file");
				}
				if (cmd.hasOption("o")) {
					outputFile = cmd.getOptionValue("o");
				} else {
					outputFile = "default.vc";
				}

				double generalTime = System.currentTimeMillis();
				double time = System.currentTimeMillis();
				DAWG t = new DAWG(input);
				/*
				 * Runtime r = Runtime.getRuntime();
				 * 
				 * System.gc(); System.out .println(((r.totalMemory() -
				 * r.freeMemory()) / (1024 * 1024))); System.exit(0);
				 */
				long textLength = t.text.length();
				if (Constants.DEBUG) {
					System.out
							.println("Excecution time for generating the graph: "
									+ ((System.currentTimeMillis() - time) / 1000));
				}
				time = System.currentTimeMillis();
				// System.exit(0);

				// this method calculates the number of unique paths from each
				// node to the sink. This is also the number of occurrences of
				// each suffix

				t.storeUniquePaths(false);

				if (Constants.DEBUG) {
					System.out
							.println("Excecution time for counting the unique paths: "
									+ ((System.currentTimeMillis() - time) / 1000));
				}
				time = System.currentTimeMillis();
				LinkedList<Node> nodeList = t.toList();
				if (cmd.hasOption("amazon")) {
					if (config.containsKey("amazon")) {
						char[] password;
						if (cmd.hasOption("password")) {
							password = cmd.getOptionValue("password")
									.toCharArray();
						} else {
							password = System.console().readPassword("[%s]:",
									"Password");
						}
						System.out.println("[INFO:] Using Amazon backend.");

						BinaryWriter out = new BinaryWriter(new AmazonBackend(
								config.get("amazon", "key"), config.get(
										"amazon", "skey"), outputFile,
								config.get("amazon", "bucket")), password);
						out.writeBlocks(nodeList, textLength);
					} else {
						System.out
								.println("Amazon credentials can't be found. Exiting.");
					}
				} else if (cmd.hasOption("google")) {
					if (config.containsKey("google")) {
						char[] password;
						if (cmd.hasOption("password")) {
							password = cmd.getOptionValue("password")
									.toCharArray();
						} else {
							password = System.console().readPassword("[%s]:",
									"Password");
						}
						System.out.println("[INFO:] Using Google backend.");

						BinaryWriter out = new BinaryWriter(new GoogleBackend(
								config.get("google", "key"), config.get(
										"google", "skey"), outputFile,
								config.get("google", "bucket")), password);
						out.writeBlocks(nodeList, textLength);
					} else {
						System.out
								.println("Google credentials can't be found. Exiting.");
					}
				} else if (cmd.hasOption("smartdrive")) {
					if (config.containsKey("smartdrive")) {
						char[] password;
						if (cmd.hasOption("smartdrive")) {
							password = cmd.getOptionValue("password")
									.toCharArray();
						} else {
							password = System.console().readPassword("[%s]:",
									"Password");
						}
						System.out.println("[INFO:] Using Smartdrive backend.");

						BinaryWriter out = new BinaryWriter(
								new SmartdriveBackend(config.get("smartdrive",
										"url"),
										config.get("smartdrive", "user"),
										config.get("smartdrive", "password"),
										outputFile), password);
						out.writeBlocks(nodeList, textLength);
					} else {
						System.out
								.println("Smartdrive credentials can't be found. Exiting.");
					}
				} else {
					char[] password;
					if (cmd.hasOption("password")) {
						password = cmd.getOptionValue("password").toCharArray();
					} else {
						password = System.console().readPassword("[%s]:",
								"Password");
					}
					System.out.println("[INFO:] Using filesystem backend.");

					BinaryWriter out = new BinaryWriter(new FileSystemBackend(
							outputFile), password);

					out.writeBlocks(nodeList, textLength);

				}

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
				h.printHelp("Usage: main [options]", options);

			} else {
				throw new ParseException(
						"Either create, evaluate or search must be specified");
			}

		} catch (ParseException e) {
			System.out.println("Can't parse options: " + e.getMessage());

		} catch (IOException e) {
			System.out.println("File not found.");
		}

	}
}
