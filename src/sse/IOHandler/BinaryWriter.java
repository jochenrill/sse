package sse.IOHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import sse.Vectors.Constants;
import sse.Vectors.EdgePosition;
import sse.Vectors.SuffixVector;

public class BinaryWriter {
    private BinaryOut w;
    private String input;

    public BinaryWriter(String fileName, String input) {
        this.input = input;
        try {
            w = new BinaryOut(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(ArrayList<SuffixVector> list, ArrayList<EdgePosition> ep) {
        int pos = 0;
        for (SuffixVector v : list) {
            // write sequence before the vector
            if (v.getLocation() != 0) {
                for (; pos < v.getLocation(); pos++) {
                    w.write(input.charAt(pos));
                }
            }
            // write vector itself
            w.write(Constants.VECTOR_MARKER);
            switch (Constants.VECTOR_DEPTH_BYTES) {
                case 8:
                    w.write((long) v.getDepth());
                    break;
                case 4:
                    w.write((int) v.getDepth());
                    break;
                case 2:
                    w.write((short) v.getDepth());
                    break;
                case 1:
                    w.write((char) v.getDepth());
                    break;
            }
            for (Character c : v.getMap().keySet()) {
                // write first char of edge
                w.write(c);
                // write bytesequence for representing the edge
                switch (Constants.EDGE_REFERENCE_BYTES) {
                    case 8:
                        w.write((long) v.getMap().get(c).getMovedPosition());
                        break;
                    case 4:
                        w.write((int) v.getMap().get(c).getMovedPosition());
                        break;
                    case 2:
                        w.write((short) v.getMap().get(c).getMovedPosition());
                        break;
                    case 1:
                        w.write((char) v.getMap().get(c).getMovedPosition());
                        break;
                }
                
            }
            w.write(Constants.VECTOR_MARKER);
        }
        // write the rest
        for (; pos < input.length(); pos++) {
            w.write(input.charAt(pos));
        }
        w.close();
    }
}
