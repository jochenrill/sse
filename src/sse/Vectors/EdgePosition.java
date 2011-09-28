package sse.Vectors;

import java.io.Serializable;

public class EdgePosition implements Comparable<EdgePosition>, Serializable {
    
    private static final long serialVersionUID = 1460328906527139022L;
    private long position;
    private long movedPosition;
    private boolean leadsToSink;

    public EdgePosition(int pos, boolean sink) {
        this.position = pos;
        this.movedPosition = pos;
        this.leadsToSink = sink;
    }

    public void setPosition(int position) {
        this.position = position;
        this.movedPosition = position;
    }

    public boolean leadsToSink(){
    	return leadsToSink;
    }
    public long getPosition() {
        return position;
    }

    @Override
    public int compareTo(EdgePosition o) {
        if (o == null) {
            throw new NullPointerException();
        }
        if (this.getPosition() == o.getPosition()) {
            return 0;
        } else if (this.getPosition() < o.getPosition()) {
            return -1;
        } else {
            return 1;
        }
    }

    public void setMovedPosition(long movedPosition) {
        this.movedPosition = movedPosition;
    }

    public long getMovedPosition() {
        return movedPosition;
    }

   
}
