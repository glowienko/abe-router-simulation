package utils;

import models.PacketEvent;

import java.util.Comparator;

public class PacketEventComparator implements Comparator<PacketEvent> {


    public int compare(PacketEvent o1, PacketEvent o2) {
        if(o1.getTime() > o2.getTime()) {
            return 1;
        }

        if(o1.getTime() < o2.getTime()) {
            return -1;
        }
        return 0;
    }
}
