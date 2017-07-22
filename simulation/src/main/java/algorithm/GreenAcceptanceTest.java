package algorithm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import models.AbePacket;
import utils.QueueUtils;

import java.util.Queue;


@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GreenAcceptanceTest {

    private AbePacket greenPacket;
    private int throughput;
    private Queue<AbePacket> greenQueue;
    private Queue<AbePacket> blueQueue;


    public boolean isPacketAccepted() {
        int transmissionDelay = greenPacket.getLength() / throughput;
        int updatedGreenQueueLength = QueueUtils.getQueueLength(greenQueue) + greenPacket.getLength();

        return updatedGreenQueueLength + getFirstPartBlueQueueLength() <= throughput * greenPacket.getDeadline() + transmissionDelay;
    }


    private int getFirstPartBlueQueueLength() {
        int length = 0;
        double transmissionDelay = greenPacket.getLength() / throughput;
        double greenDeadline = greenPacket.getDeadline();

        for (AbePacket bluePacket : blueQueue) {
            if (bluePacket.getDeadline() > greenDeadline + transmissionDelay)
                break;
            length += bluePacket.getLength();
        }
        return length;
    }
}
