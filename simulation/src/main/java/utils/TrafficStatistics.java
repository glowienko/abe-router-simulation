package utils;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import models.AbePacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Represents single set of traffic parameters in router
 * Should get simple params from algorithm.BetterThanBestEffort abject and gives us delays, loss ratio etc.
 */
@Getter
@Setter
@NoArgsConstructor
public class TrafficStatistics {


    public List<Double> timeXAxis = new ArrayList<Double>();
    public List<Double> delaysYAxis = new ArrayList<Double>();

    public List<Double> timeXAxisGreen = new ArrayList<Double>();
    public List<Double> delaysYAxisGreen = new ArrayList<Double>();

    public List<Double> timeXAxisBlue = new ArrayList<Double>();
    public List<Double> delaysYAxisBlue = new ArrayList<Double>();


    private long totalPacketsNumber;
    private long bluePacketsNumber;
    private long greenPacketsNumber;

    private long totalPacketsLostNumber;
    private long greenPacketsLostNumber;
    private long bluePacketsLostNumber;

    private double totalPacketsDelay;
    private double totalBluePacketsDelay;
    private double totalGreenPacketsDelay;


    private int avgPacketSize;
    private double avgPacketDelay;
    private double packetLossRate;

    private double virtualQueueOccupancy = 0;
    private int maxVirtualQueueSize;


    /**
     * @return average packets loss in %
     */
    public double countAvgPacketLoss() {
        return totalPacketsLostNumber * 100 / totalPacketsNumber;
    }

    public double countAvgBluePacketLoss() {
        return (bluePacketsLostNumber * 100 / bluePacketsNumber);
    }

    public double countAvgGreenPacketLoss() {
        return (greenPacketsLostNumber * 100 / greenPacketsNumber);
    }


    public double countAvgPacketDelay() {
        return totalPacketsDelay / totalPacketsNumber;
    }

    public double countAvgGreenPacketDelay() {
        return totalGreenPacketsDelay / greenPacketsNumber;
    }

    public double countAvgBluePacketDelay() {
        return totalBluePacketsDelay / bluePacketsNumber;
    }

    public void updateVirtualQueueOccupancy(Queue<AbePacket> virtualQueue) {
        virtualQueueOccupancy += (double) QueueUtils.getQueueLength(virtualQueue) / (double) maxVirtualQueueSize;
    }

    public double countVirtualQueueOccupancy() {
        return  (virtualQueueOccupancy * 100.0) / (double) totalPacketsNumber;
    }

    public void addPacketLoss(AbePacket packet) {
        if (packet.getColor().equals(Enums.COLOR.blue))
            bluePacketsLostNumber++;
        else if (packet.getColor().equals(Enums.COLOR.green))
            greenPacketsLostNumber++;
        totalPacketsLostNumber++;
    }

    public void addPacket(AbePacket packet) {
        if (packet.getColor().equals(Enums.COLOR.blue))
            bluePacketsNumber++;
        else if (packet.getColor().equals(Enums.COLOR.green))
            greenPacketsNumber++;
        totalPacketsNumber++;
    }

    public void addPacketDelay(AbePacket packet, double currentTime) {
        double delay = packet.getLeavingTime() - packet.getComingTime();

        totalPacketsDelay += delay;
        timeXAxis.add(currentTime);//dodanie kazdego pakietu do wykresu opoznienia
        delaysYAxis.add(delay);

        if (packet.getColor().equals(Enums.COLOR.blue)) {
            totalBluePacketsDelay += delay;

            timeXAxisBlue.add(currentTime);//dodanie punktu do wykresu niebieskich pakietow
            delaysYAxisBlue.add(delay);

        } else if (packet.getColor().equals(Enums.COLOR.green)) {
            totalGreenPacketsDelay += delay;

            timeXAxisGreen.add(currentTime);//dodanie punktu do wykresu zielonych pakietow
            delaysYAxisGreen.add(delay);
        }
    }
}
