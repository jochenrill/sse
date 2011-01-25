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
import sse.Vectors.EdgePosition;
import sse.Vectors.InMemoryVG;
import sse.Vectors.OutOfMemoryVG;
import sse.Vectors.SuffixVector;


public class Main {
	public static String text;

	public static void main(String[] args) {

		Options options = new Options();
		options.addOption("i", true, "Defines the input file");
		options.addOption("o", true, "Defines the output file");
		options.addOption("s", true, "Defines the String used for input");
		options.addOption("e", false,
				"Runs an evaluation of the generated file");
		options.addOption("m", false, "Generates the SuffixVectors in memory");
		options.addOption("h",false,"Prints help");
		options.addOption("help", false ,"Prints help");
		CommandLineParser parser = new GnuParser();
		
		// File inputFile = null;;
		String outputFile = null;
		boolean evaluate = false;
		String input = null;
		boolean inMemory = false;

		try {
			CommandLine cmd = parser.parse(options, args);
		
			if (cmd.hasOption("i")) {
				File inputFile = new File(cmd.getOptionValue("i"));
				BufferedReader r = new BufferedReader(new FileReader(inputFile));
				input = r.readLine();
			} else if (cmd.hasOption("s")) {
				input = cmd.getOptionValue("s");
			}
			if (cmd.hasOption("o")) {
				outputFile = cmd.getOptionValue("o");
			} else {
				outputFile = "default.vc";
			}

			if (cmd.hasOption("e")) {
				evaluate = true;
			}
			if (cmd.hasOption("m")) {
				inMemory = true;
			}
			if(cmd.hasOption("h") || cmd.hasOption("help")){
				HelpFormatter h = new HelpFormatter();
				h.printHelp("test", options);
				System.exit(0);
			}
		} catch (ParseException e) {
			System.out.println("Can't parse options " + e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// s = "ananasan$";
		// s = "aatttatttatta$";
		double time = System.currentTimeMillis();
		CDWAG t = new CDWAG(input);
		System.out.println("Excecution time: "
				+ ((System.currentTimeMillis() - time) / 1000));
		time = System.currentTimeMillis();
		// ArrayList<SuffixVector> list = t.getListOfVectors();
		// SuffixVector rootVector = t.getRootVector();
		// ArrayList<EdgePosition> ep = t.listOfEdges;

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
				System.out.println(o.available());
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
		Collections.sort(list);
		Collections.sort(ep);

		// Update position of edges
		Iterator<SuffixVector> i = list.iterator();
		SuffixVector tmp = i.next();
		int offset = 0;
		for (EdgePosition p : ep) {
			while (tmp != null && tmp.getLocation() <= p.getPosition()) {
				offset += tmp.getSize();
				if (i.hasNext()) {
					tmp = i.next();
				} else {
					tmp = null;
				}
			}
			p.setMovedPosition(p.getMovedPosition() + offset);
		}

		BinaryWriter out = new BinaryWriter(outputFile, input);
		out.write(list, ep);

		if (evaluate) {
			BinaryParser p = new BinaryParser("test.vc");
			p.getText();
		}
	}
}
