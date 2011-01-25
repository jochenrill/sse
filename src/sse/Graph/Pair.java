package sse.Graph;
public class Pair {
    public boolean b;
    public int k;
    public Node s;

    public Pair(boolean b, Node s) {
        this.b = b;
        this.s = s;
    }

    public Pair(Node s, int k) {
        this.k = k;
        this.s = s;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Pair) {
            Pair p = (Pair) o;
            return (this.k == p.k && this.s == p.s);
        } else {
            return false;
        }
    }
}
