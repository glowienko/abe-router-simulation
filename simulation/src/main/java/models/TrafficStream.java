package models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import utils.Enums;
import utils.ProbabilityDistribution;

import java.util.Random;

@AllArgsConstructor
@Builder
public class TrafficStream {
    private static final int MAX_PACKET_SIZE = 50000;
    private static final int MIN_PACKET_SIZE = 100;

    private static final int MAX_PACKET_TIME = 100;
    private static final double MIN_PACKET_TIME = 0.001;

    private Enums.COLOR color;

    private ProbabilityDistribution packetLengthDistribution;

    private ProbabilityDistribution timeDistribution;
    private String name;
    private Random random;


    public TrafficStream(Enums.COLOR color, String name) {
        this.color = color;
        this.name = name;
        this.random = new Random();

//        packetLengthDistribution = new ProbabilityDistribution(Enums.TYPE.poisson, 10000);

        if (color.equals(Enums.COLOR.blue)) {
            timeDistribution = new ProbabilityDistribution(Enums.TYPE.poisson, 1);
            packetLengthDistribution = new ProbabilityDistribution(Enums.TYPE.poisson, 1000);
        } else {
            timeDistribution = new ProbabilityDistribution(Enums.TYPE.poisson, 0.125);
            packetLengthDistribution = new ProbabilityDistribution(Enums.TYPE.poisson, 300);
        }

    }

    public AbePacket generatePacket() {
        AbePacket newPacket = new AbePacket();
        newPacket.setLength((int) packetLengthDistribution.getPoissonValue(random.nextDouble()));
        newPacket.setColor(color);
        newPacket.setLength(getRandomPacketSize());
        return newPacket;
    }

    public double getPacketArrivalTime(double currentTime) {

        if (currentTime == 0) {
            currentTime = timeDistribution.getPoissonValue(random.nextDouble());

            while (currentTime < MIN_PACKET_TIME || currentTime > MAX_PACKET_TIME) {
                currentTime = timeDistribution.getPoissonValue(random.nextDouble());
            }
        }

        double time = timeDistribution.getPoissonValue(random.nextDouble());

        while (time < MIN_PACKET_TIME || time > MAX_PACKET_TIME) {
            time = timeDistribution.getPoissonValue(random.nextDouble());
        }

        return currentTime + time;
    }

    private int getRandomPacketSize() {
        int packetSize = (int) packetLengthDistribution.getPoissonValue(random.nextDouble());

        while (packetSize < MIN_PACKET_SIZE || packetSize > MAX_PACKET_SIZE) {
            packetSize = (int) packetLengthDistribution.getPoissonValue(random.nextDouble());
        }

        return packetSize;
    }

}
