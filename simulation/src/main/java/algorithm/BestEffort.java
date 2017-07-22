package algorithm;


import models.AbePacket;
import models.PacketEvent;
import models.TrafficStream;
import utils.Enums;
import utils.PacketEventComparator;
import utils.QueueUtils;
import utils.TrafficStatistics;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class BestEffort {

    private static final int TOTAL_SIMULATED_PACKETS = 10000;
    private static final int ROUTER_THROUGHPUT = 1000;
    private static final int QUEUE_SIZE = 10000;

    private PriorityQueue<PacketEvent> eventQueue;

    private Queue<AbePacket> packetsQueue; //AbePacket object instead of Packet only for using the same statistics methods
                                           // best effort do not use colors and deadlines

    private TrafficStatistics statistics;

    private TrafficStream greenTrafficStream;
    private TrafficStream blueTrafficStream;

    private double currentTime;
    private boolean isRouterBusy;

    public BestEffort() {
        greenTrafficStream = new TrafficStream(Enums.COLOR.green, "greenStream");
        blueTrafficStream = new TrafficStream(Enums.COLOR.blue, "blueStream");
        statistics = new TrafficStatistics();
        packetsQueue = new LinkedList<>();
        eventQueue = new PriorityQueue<>(10000, new PacketEventComparator());

        statistics.setMaxVirtualQueueSize(QUEUE_SIZE);

        isRouterBusy = false;
        currentTime = 0;
    }

    public TrafficStatistics startSimulation() {

        currentTime = 0;

        setupInitialArrivalEvents();

        while (statistics.getTotalPacketsNumber() <= TOTAL_SIMULATED_PACKETS) {

            statistics.updateVirtualQueueOccupancy(packetsQueue);

            PacketEvent currentEvent = getCurrentPacketEvent();
            currentTime = currentEvent.getTime();

            if (currentEvent.getType().equals(Enums.EVENT_TYPE.packetArrival)) {
                handleArrivalEvent(currentEvent);
            }

            if (currentEvent.getType().equals(Enums.EVENT_TYPE.packetLeaving)) {
                handleLeavingEvent(currentEvent);
            }
        }

        return statistics;
    }

    private void handleArrivalEvent(PacketEvent packetEvent) {
        AbePacket arrivingPacket = packetEvent.getPacket();
        statistics.addPacket(arrivingPacket);

        if (packetsQueue.isEmpty() && !isRouterBusy) { // queue is empty and router is not busy
            /*
            So we are starting to serving this packet immediately and we are creating new event of packet leaving
            New packetLeaving event is possible when router will finish serving this packet

            Summing up - packetLeaving event is event of end serving last packet and getting new one from queues according to DSD
             */
            arrivingPacket.setComingTime(currentTime);
            isRouterBusy = true;

            if (arrivingPacket.getColor().equals(Enums.COLOR.blue)) {
                addBluePacketArrivalEvent();
            } else {
                addGreenPacketArrivalEvent();
            }
            eventQueue.add(new PacketEvent(Enums.EVENT_TYPE.packetLeaving, arrivingPacket, currentTime + (double) arrivingPacket.getLength() / (double) ROUTER_THROUGHPUT));

        } else if (QueueUtils.getQueueLength(packetsQueue) + arrivingPacket.getLength() > QUEUE_SIZE) {
            statistics.addPacketLoss(arrivingPacket);
        } else {//queue not full and not empty or queue empty but router is busy serving some previous packet
            arrivingPacket.setComingTime(currentTime);
            packetsQueue.add(arrivingPacket);


            // add next packet arrival event from this stream - according to the poisson probability distribution for this stream
            if (arrivingPacket.getColor().equals(Enums.COLOR.blue)) {
                addBluePacketArrivalEvent();
            } else {
                addGreenPacketArrivalEvent();
            }
        }
    }

    private void handleLeavingEvent(PacketEvent packetEvent) {
        AbePacket leavingPacket = packetEvent.getPacket();

        leavingPacket.setLeavingTime(currentTime);
        statistics.addPacketDelay(packetEvent.getPacket(), currentTime);


        if (packetsQueue.isEmpty()) {
            // queue is empty - so we have situation that packet arrived to the empty queue and was started to be serving in router'
            // so we are now have event of leaving this packet  - we are marking router as not busy, and that's all
            // ! next event in event queue should be event of packet arrival to the queue
            isRouterBusy = false;
        } else {  //queue not empty  -- > so we are serving packet
            servePacket();
        }
    }

    private void servePacket() {
        AbePacket packet = packetsQueue.poll(); //get packet from the queue and create packet leaving event when it will exit system
        eventQueue.add(new PacketEvent(Enums.EVENT_TYPE.packetLeaving, packet, currentTime + ((double)packet.getLength() / (double) ROUTER_THROUGHPUT)));
    }

    private PacketEvent getCurrentPacketEvent() {
        PacketEvent nextEvent = eventQueue.poll();

        if (nextEvent == null) {
            setupInitialArrivalEvents();
            nextEvent = eventQueue.poll();
        }
        return nextEvent;
    }

    private void addGreenPacketArrivalEvent() {
        PacketEvent arrivalEvent = new PacketEvent(Enums.EVENT_TYPE.packetArrival,
                greenTrafficStream.generatePacket(),
                greenTrafficStream.getPacketArrivalTime(currentTime));

        eventQueue.add(arrivalEvent);
    }

    private void addBluePacketArrivalEvent() {
        PacketEvent arrivalEvent = new PacketEvent(Enums.EVENT_TYPE.packetArrival,
                blueTrafficStream.generatePacket(),
                blueTrafficStream.getPacketArrivalTime(currentTime));

        eventQueue.add(arrivalEvent);
    }

    private void setupInitialArrivalEvents() { // this method adds two event of arrival packet
        addBluePacketArrivalEvent();
        addGreenPacketArrivalEvent();
    }

}
