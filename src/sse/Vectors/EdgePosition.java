package sse.Vectors;

import java.io.Serializable;

public class EdgePosition implements Comparable<EdgePosition>, Serializable {
    
    private static final long serialVersionUID = 1460328906527139022L;
    private long position;
    private long movedPosition;

    public EdgePosition(int pos) {
        this.position = pos;
        this.movedPosition = pos;
    }

    public void setPosition(int position) {
        this.position = position;
        this.movedPosition = position;
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
