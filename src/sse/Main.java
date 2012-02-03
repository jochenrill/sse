package sse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.LinkedList;
import java.util.Scanner;

import org.apache.commons.cli.*;
import org.ini4j.Ini;

import sse.Backend.AmazonBackend;
import sse.Backend.FileSystemBackend;
import sse.Backend.GoogleBackend;
import sse.Graph.DAWG;
import sse.Graph.Node;
import sse.IOHandler.BinaryWriter;
import sse.IOHandler.SearchEngine;

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
				Constants.NUMOCCURS_BYTE = Short.parseShort(config.get(
						"general", "numoccurs_byte"));
				Constants.ORIGINAL_EDGE_POSITION_BYTES = Short
						.parseShort(config.get("general",
								"original_edge_position_bytes"));
				Constants.ORIGINAL_VECTOR_POSITION_BYTES = Short
						.parseShort(config.get("general",
								"original_vector_position_bytes"));
				Constants.PADDING_BYTE = (char) Byte.parseByte(config.get(
						"general", "padding_byte"));
				Constants.VECTOR_DEPTH_BYTES = Short.parseShort(config.get(
						"general", "vector_depth_bytes"));
				Constants.VECTOR_MARKER = config
						.get("general", "vector_marker").charAt(0);
				Constants.VECTOR_SIZE_MULTI = Short.parseShort(config.get(
						"general", "vector_size_multi"));
				Constants.EXACT_MATCHING = Boolean.parseBoolean(config.get(
						"general", "exact_matching"));
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
												"amazon", "bucket")),
										cmd.getOptionValue("i"), password);
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
												"google", "bucket")),
										cmd.getOptionValue("i"), password);
								System.out.println(sEn.find(cmd
										.getOptionValue("text")));
								System.out.println("Files opened:"
										+ sEn.getTransferedFilesCount());
							} else {
								System.out
										.println("No Google Credentials found. Search can't be performed.");
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
									cmd.getOptionValue("i"), password);
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

				String input = "";
				String outputFile = null;
				if (cmd.hasOption("i")) {
					File inputFile = new File(cmd.getOptionValue("i"));

					BufferedReader r = new BufferedReader(new FileReader(
							inputFile));
					Scanner scanner = new Scanner(r);

					while (scanner.hasNextLine()) {

						input += "\n" + scanner.nextLine();
					}
					scanner.close();
					r.close();

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
				long textLength = t.text.length();
				if (Constants.DEBUG) {
					System.out
							.println("Excecution time for generating the graph: "
									+ ((System.currentTimeMillis() - time) / 1000));
				}
				time = System.currentTimeMillis();
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

						BinaryWriter out = new BinaryWriter(outputFile,
								new AmazonBackend(config.get("amazon", "key"),
										config.get("amazon", "skey"),
										outputFile, config.get("amazon",
												"bucket")));
						out.writeBlocks(nodeList, textLength, password);
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

						BinaryWriter out = new BinaryWriter(outputFile,
								new GoogleBackend(config.get("google", "key"),
										config.get("google", "skey"),
										outputFile, config.get("google",
												"bucket")));
						out.writeBlocks(nodeList, textLength, password);
					} else {
						System.out
								.println("Google credentials can't be found. Exiting.");
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

					BinaryWriter out = new BinaryWriter(outputFile,
							new FileSystemBackend(outputFile));
					out.writeBlocks(nodeList, textLength, password);

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
