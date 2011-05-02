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

import sse.Graph.CDWAG;
import sse.IOHandler.BinaryParser;
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

		options.addOption("i", true,
				"Defines the input file. Define either an input file or a string.");
		options.addOption("o", true,
				"Defines the output file. \"default.vc\" if no file is given.");
		options.addOption("s", true,
				"Defines the String used for input. Define either a string or an input file.");
		options.addOption("evaluate", false,
				"Turns on evaluation mode. Only works with unencrypted files.");
		options.addOption("m", false, "Generates the SuffixVectors in memory");
		options.addOption("h", false, "Prints help");
		options.addOption("help", false, "Prints this help message");
		options.addOption("v", false, "Verbose");
		options.addOption("b", false,
				"Turns off block usage. Used in evaluation and create mode. Default on.");
		options.addOption("enc", false,
				"Turns off block encryption. Default on.");
		options.addOption("blocksize", true, "Sets the blocksize multiplier");
		options.addOption("search", false, "Turns on search mode");
		options.addOption("key", true, "Path to the key file for encryption");
		options.addOption("create", false, "Turns on creation mode");
		options.addOption("text", true,
				"If search mode is used, this is the text that will be searched");
		options.addOption("indcpa",false,"Turns on IND-CPA-Mode");
		CommandLineParser parser = new GnuParser();

		try {
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption("v")) {

				Constants.DEBUG = true;
			}
			if (cmd.hasOption("search")) {
				if (cmd.hasOption("i")) {
					if (cmd.hasOption("text")) {

						if (cmd.hasOption("key")) {
							SearchEngine sEn = new SearchEngine(
									cmd.getOptionValue("text"),
									cmd.getOptionValue("key"));
							System.out.println(sEn.find(
									cmd.getOptionValue("i"), true));
							System.out.println(sEn.files);
						} else {
							SearchEngine sEn = new SearchEngine(
									cmd.getOptionValue("text"));
							System.out.println(sEn.find(
									cmd.getOptionValue("i"), false));
						}
					} else {
						throw new ParseException("Text argument missing");
					}
				} else {
					throw new ParseException("Input argument is needed");
				}
			} else if (cmd.hasOption("evaluate")) {

				if (cmd.hasOption("i")) {
					BinaryParser p = new BinaryParser(cmd.getOptionValue("i"));
					if (cmd.hasOption("b")) {
						System.out.println(p.getText());
					} else {
						System.out.println(p.getTextWithBlocks());
					}
				} else {
					throw new ParseException("Input file must be given.");
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

						input += scanner.nextLine();
					}
					scanner.close();
					r.close();

				} else if (cmd.hasOption("s")) {
					input = cmd.getOptionValue("s");
				} else {
					throw new MissingOptionException(
							"You must define an input, either a string or a file");
				}
				if (cmd.hasOption("o")) {
					outputFile = cmd.getOptionValue("o");
				} else {
					outputFile = "default.vc";
				}
				if (cmd.hasOption("blocksize")) {
					Constants.VECTOR_SIZE_MULTI = Short.valueOf(cmd
							.getOptionValue("blocksize"));
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
					InMemoryVG generator = new InMemoryVG(t);
					list = generator.getListOfVectors();
					
					for (SuffixVector v : list) {
						for (EdgePosition e : v.getMap().values()) {
							ep.add(e);
						}
					}
					
					generator = null;
					t = null;
					System.gc();
					
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
				BinaryWriter out = new BinaryWriter(outputFile, input);
				if (cmd.hasOption("b")) {
					out.writeAll(list, ep);
				} else {
					out.writeBlocks(list, ep, !cmd.hasOption("enc"),textLength,cmd.hasOption("indcpa"));
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
