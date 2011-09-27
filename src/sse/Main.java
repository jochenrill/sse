package sse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Scanner;

import org.apache.commons.cli.*;
import org.ini4j.Ini;

import sse.Backend.AmazonBackend;
import sse.Backend.FileSystemBackend;
import sse.Backend.GoogleBackend;
import sse.Graph.CDWAG;
import sse.IOHandler.BinaryWriter;
import sse.IOHandler.SearchEngine;
import sse.Vectors.Constants;
import sse.Vectors.EdgePosition;
import sse.Vectors.InMemoryVG;
import sse.Vectors.OutOfMemoryVG;
import sse.Vectors.SuffixVector;

public class Main {

	public static void main(String[] args) {
		Options options = new Options();

		options.addOption("i", true, "Defines the input file.");
		options.addOption("o", true,
				"Defines the output file. \"default.vc\" if no file is given.");
		options.addOption("m", false, "Generates the SuffixVectors in memory");
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
		options.addOption("indcpa", false, "Turns on IND-CPA-Mode");
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
				CDWAG t = new CDWAG(input);
				long textLength = t.text.length();
				if (Constants.DEBUG) {
					System.out
							.println("Excecution time for generating the graph: "
									+ ((System.currentTimeMillis() - time) / 1000));
				}
				time = System.currentTimeMillis();
				ArrayList<SuffixVector> list = new ArrayList<SuffixVector>();

				ArrayList<EdgePosition> ep = new ArrayList<EdgePosition>();
				if (cmd.hasOption("m")) {
					// Generate Vectors in memory. Much faster than out of
					// memory
					InMemoryVG generator = new InMemoryVG(t);
					list = generator.getListOfVectors();

					for (SuffixVector v : list) {
						for (EdgePosition e : v.getMap().values()) {
							ep.add(e);
						}
					}

					generator = null;
					t = null;

				} else {
					OutOfMemoryVG generator = new OutOfMemoryVG(t);
					File vectorFile = new File("_vector.tmp");
					try {
						generator.printListOfVectors(vectorFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
					t = null;

					generator = null;
					System.gc();

					try {
						ObjectInputStream o = new ObjectInputStream(
								new FileInputStream("_vector.tmp"));
						Object obj = null;
						while (!((obj = o.readObject()) instanceof String)) {
							list.add((SuffixVector) obj);
						}
						o.close();
						vectorFile.delete();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					for (SuffixVector v : list) {
						for (EdgePosition e : v.getMap().values()) {
							ep.add(e);
						}
					}
				}
				if (Constants.DEBUG) {
					System.out
							.println("Excecution time for generating the vectors: "
									+ ((System.currentTimeMillis() - time) / 1000));
				}
				time = System.currentTimeMillis();

				// Sort position and vector objects for easier merging
				Collections.sort(list);
				Collections.sort(ep);
				if (Constants.DEBUG) {
					System.out
							.println("Excecution time for sorting the vectors: "
									+ ((System.currentTimeMillis() - time) / 1000));
				}
				time = System.currentTimeMillis();
				// Update position of edges
				Iterator<SuffixVector> iterator = list.iterator();
				SuffixVector tmp = iterator.next();
				int offset = 0;
				for (EdgePosition p : ep) {
					while (tmp != null && tmp.getLocation() <= p.getPosition()) {
						offset += tmp.getSize();
						if (iterator.hasNext()) {
							tmp = iterator.next();
						} else {
							tmp = null;
						}
					}
					p.setMovedPosition(p.getMovedPosition() + offset);
				}
				if (Constants.DEBUG) {
					System.out
							.println("Excecution time for updating the positions: "
									+ ((System.currentTimeMillis() - time) / 1000));
				}
				time = System.currentTimeMillis();

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
						BinaryWriter out = new BinaryWriter(outputFile, input,
								new AmazonBackend(config.get("amazon", "key"),
										config.get("amazon", "skey"),
										outputFile, config.get("amazon",
												"bucket")));
						out.writeBlocks(list, ep, textLength,
								cmd.hasOption("indcpa"), password);
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
						BinaryWriter out = new BinaryWriter(outputFile, input,
								new GoogleBackend(config.get("google", "key"),
										config.get("google", "skey"),
										outputFile, config.get("google",
												"bucket")));
						out.writeBlocks(list, ep, textLength,
								cmd.hasOption("indcpa"), password);
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
					BinaryWriter out = new BinaryWriter(outputFile, input,
							new FileSystemBackend(outputFile));
					out.writeBlocks(list, ep, textLength,
							cmd.hasOption("indcpa"), password);

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
