package sse.Graph;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
/**
 * Implementation of Ukkonen's algorithm for generating a suffix tree in O(n) time - for testing purposes only
 * and not needed in the implementation of sse.
 * @author Jochen Rill
 *
 */
 
public class SuffixTree {
    private long nodeCount;
    private String text;
    private Node root;
   

    public SuffixTree(String text) {
        this.text = text;
        this.root = new Node(nodeCount++);
        Node bottom = new Node(nodeCount++);
        for (int i = 0; i < text.length(); i++) {
            Edge newEdge = new Edge(i,i, bottom, root);
            bottom.addEdge(text.charAt(i), newEdge);
        }
        root.setSuffixLink(bottom);
        Node s = root;
        int k = 0;
        
        for(int i = 0; i < text.length();i++){
           
            Pair p1 = update(s, k, i);
            s = p1.s;
            k = p1.k;
            Pair p2 = canonize(s, k, i);
            s = p2.s;
            k = p2.k;
        }
    }

    private Pair update(Node s, int k, int i) {
        Node oldr = root;
        Pair pair = testAndSplit(s, k, i - 1, text.charAt(i));
        boolean endPoint = pair.b;
        Node r = pair.s;
        while (!endPoint) {
            Node r1 = new Node(nodeCount++);
            Edge newTransition = new Edge(i, text.length(), r, r1);
            r.addEdge(text.charAt(i), newTransition);
            if (oldr != root) {
                oldr.setSuffixLink(r);
            }
            oldr = r;
            Pair sk = canonize(s.getSuffixLink(), k, i - 1);
            s = sk.s;
            k = sk.k;
            Pair ep = testAndSplit(s, k, i - 1, text.charAt(i));
            endPoint = ep.b;
            r = ep.s;
        }
        if (oldr != root) {
            oldr.setSuffixLink(s);
        }
        return new Pair(s, k);
    }
    
    public void printToFile(int i) {
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter("graph" + i
                    + ".dot"));
            w.write("/* this is a generated dot file: www.graphviz.org */\n"
                    + "digraph suffixtree {\n"
                    + "\trankdir=LR\nnode[shape=box]");
            printDot(root, w, new LinkedList<Node>());
            w.write("}");
            w.close();
        } catch (IOException e) {
            System.out.println("File not found");
        }
    }

    // Recursive method to traverse the tree
    private void printDot(Node root, BufferedWriter writer,
            LinkedList<Node> visited) throws IOException {
        if (root.getEdges() != null) {
            for (Edge e : root.getEdges()) {
                String string = root.getId() + "->" + e.getEnd().getId() + "\n";
                writer.write(string);
             
                if (e.getEdgeLabelEnd() == e.getEdgeLabelStart()) {
                    writer
                            .write("[" + "label=\""
                                    + text.charAt(e.getEdgeLabelStart())
                                    + "\"];\n");
                } else {
                    writer.write("["
                            + "label=\""
                            + text.substring(e.getEdgeLabelStart(), e
                                    .getEdgeLabelEnd()) + "\"];\n");
                }
                if (!visited.contains(e.getEnd())) {
                    printDot(e.getEnd(), writer, visited);
                }
                visited.add(e.getEnd());
            }
        }
    }

    private Pair testAndSplit(Node s, int k, int p, char t) {
        if (k <= p) {
            Edge transition = s.getEdge(text.charAt(k));
            int k1 = transition.getEdgeLabelStart();
            int p1 = transition.getEdgeLabelEnd();
            if (t == text.charAt(k1 + p - k + 1)) {
                return new Pair(true, s);
            } else {
                Node r = new Node(nodeCount++);
                Edge newTransition1 = new Edge(k1, k1 + p - k, s, r);
                Edge newTransition2 = new Edge(k1 + p - k + 1, p1, r,
                        transition.getEnd());
                s.addEdge(text.charAt(k1), newTransition1);
                r.addEdge(text.charAt(k1 + p - k + 1), newTransition2);
                s.removeEdge(transition);
                return new Pair(false, r);
            }
        } else {
            boolean isTrans = (s.getEdge(t) != null);
            return new Pair(isTrans,s);
        }
    }

    private Pair canonize(Node s, int k, int p) {
        if (p < k) {
            return new Pair(s, k);
        } else {
            Edge transition = s.getEdge(text.charAt(k));
            int p1 = transition.getEdgeLabelEnd();
            int k1 = transition.getEdgeLabelStart();
            Node s1 = transition.getEnd();
            while (p1 - k1 <= p - k) {
                k = k + p1 - k1 + 1;
                s = s1;
                if (k <= p) {
                    transition = s.getEdge(text.charAt(k));
                    p1 = transition.getEdgeLabelEnd();
                    k1 = transition.getEdgeLabelStart();
                }
            }
            return new Pair(s, k);
        }
    }

    
}
