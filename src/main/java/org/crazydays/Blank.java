package org.crazydays;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;

public class Blank {
    private StlHeader header;
    private List<Facet> facets;

    public Blank() {
        this.facets = new LinkedList<>();
    }

    public void setHeader(StlHeader header) {
        this.header = header;
    }

    public void addFacet(Facet facet) {
        this.facets.add(facet);
    }

    public byte[] getBytes() {
        ByteBuffer buffer =  ByteBuffer.allocate(80 + 4 + (50 * facets.size())).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(header.getBytes());
        buffer.putInt(facets.size());
        for (Facet facet : facets) {
            buffer.put(facet.getBytes());
        }
        return buffer.array();
    }
}
