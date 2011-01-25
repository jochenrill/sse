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
import org.apache.commons.cli.*;

import sse.Graph.CDWAG;
import sse.IOHandler.BinaryParser;
import sse.IOHandler.BinaryWriter;
import sse.Vectors.Constants;
import sse.Vectors.EdgePosition;
import sse.Vectors.InMemoryVG;
import sse.Vectors.OutOfMemoryVG;
import sse.Vectors.SuffixVector;

public class Main {
	public static String text;

	public static void main(String[] args) {
		Options options = new Options();
		options.addOption("i", true,
				"Defines the input file. Define either an input file or a string.");
		options.addOption("o", true,
				"Defines the output file. \"default.vc\" if no file is given.");
		options.addOption("s", true,
				"Defines the String used for input. Define either a string or an input file.");
		options.addOption("e", false,
				"Runs an evaluation of the generated file");
		options.addOption("m", false, "Generates the SuffixVectors in memory");
		options.addOption("h", false, "Prints help");
		options.addOption("help", false, "Prints this help message");
		options.addOption("v", false, "Verbose");
		options.addOption("b", false, "Split in blocks. Default turned off");
		CommandLineParser parser = new GnuParser();
		// Define a series of variables to check command line options
		String outputFile = null;
		boolean evaluate = false;
		boolean blocks = false;
		String input = null;
		boolean inMemory = false;
		boolean verbose = false;
		try {
			CommandLine cmd = parser.parse(options, args);
			if (cmd.hasOption("i")) {
				File inputFile = new File(cmd.getOptionValue("i"));
				BufferedReader r = new BufferedReader(new FileReader(inputFile));
				input = r.readLine();
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
			if (cmd.hasOption("b")) {
				blocks = true;
			}
			if (cmd.hasOption("e")) {
				evaluate = true;
			}
			if (cmd.hasOption("m")) {
				inMemory = true;
			}
			if (cmd.hasOption("v")) {
				verbose = true;
			}
			if (cmd.hasOption("h") || cmd.hasOption("help")) {
				HelpFormatter h = new HelpFormatter();
				h.printHelp("Usage: main [options]", options);
				System.exit(0);
			}
		} catch (ParseException e) {
			System.out.println("Can't parse options: " + e.getMessage());
			System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// s = "ananasan$";
		// s = "aatttatttatta$";
		double generalTime = System.currentTimeMillis();
		double time = System.currentTimeMillis();
		CDWAG t = new CDWAG(input);
		if (verbose) {
			System.out.println("Excecution time for generating the graph: "
					+ ((System.currentTimeMillis() - time) / 1000));
		}
		time = System.currentTimeMillis();
		ArrayList<SuffixVector> list = new ArrayList<SuffixVector>();
		ArrayList<EdgePosition> ep = new ArrayList<EdgePosition>();
		if (inMemory) {
			InMemoryVG generator = new InMemoryVG(t);
			list = generator.getListOfVectors();
			ep = generator.getListOfEdges();
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
		if (verbose) {
			System.out.println("Excecution time for generating the vectors: "
					+ ((System.currentTimeMillis() - time) / 1000));
		}
		time = System.currentTimeMillis();
		Collections.sort(list);
		Collections.sort(ep);
		if (verbose) {
			System.out.println("Excecution time for sorting the vectors: "
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
		if (verbose) {
			System.out.println("Excecution time for updating the positions: "
					+ ((System.currentTimeMillis() - time) / 1000));
		}
		time = System.currentTimeMillis();
		BinaryWriter out = new BinaryWriter(outputFile, input);
		if (!blocks) {
			out.writeAll(list, ep);
		} else {
			out.writeBlocks(list, ep);
		}
		if (verbose) {
			System.out.println("Excecution time for output: "
					+ ((System.currentTimeMillis() - time) / 1000));
		}
		if (verbose) {
			System.out.println("Total excecution time: "
					+ ((System.currentTimeMillis() - generalTime) / 1000));
		}
		if (evaluate) {
			BinaryParser p = new BinaryParser(outputFile);
			if (!blocks) {
				System.out.println(p.getText());
			} else {
				System.out.println(p.getTextWithBlocks());
			}
		}
	}
}
