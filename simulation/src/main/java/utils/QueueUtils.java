package utils;

import models.AbePacket;

import java.util.Queue;


public class QueueUtils {
    public static int getQueueLength(Queue<AbePacket> packetQueue) {
        int length = 0; // in bytes

        for(AbePacket packet : packetQueue) {
            length += packet.getLength();
        }
        return length;
    }
}
