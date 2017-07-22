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
import java.util.Random;
import java.util.stream.Collectors;

public class BetterThanBestEffort {
    private static final int TOTAL_SIMULATED_PACKETS = 10000;
    private static final int ROUTER_THROUGHPUT = 1000;
    private static final int VIRTUAL_QUEUE_SIZE = 10000;
    private static final int GREEN_PACKET_DEADLINE = 1;

    private boolean isRouterBusy;

    private PriorityQueue<PacketEvent> eventQueue;

    private Queue<AbePacket> virtualQueue;
    private Queue<AbePacket> blueQueue;
    private Queue<AbePacket> greenQueue;

    private TrafficStatistics statistics;

    private TrafficStream greenTrafficStream;
    private TrafficStream blueTrafficStream;

    private double currentTime;
    private Random random;

    public BetterThanBestEffort() {
        isRouterBusy = false;
        greenTrafficStream = new TrafficStream(Enums.COLOR.green, "greenStream");
        blueTrafficStream = new TrafficStream(Enums.COLOR.blue, "blueStream");
        statistics = new TrafficStatistics();
        virtualQueue = new LinkedList<>();
        greenQueue = new LinkedList<>();
        blueQueue = new LinkedList<>();

        eventQueue = new PriorityQueue<>(10000, new PacketEventComparator());

        currentTime = 0;
        random = new Random();
        statistics.setMaxVirtualQueueSize(VIRTUAL_QUEUE_SIZE);
    }

    public TrafficStatistics startSimulation() {

        currentTime = 0;

        setupInitialArrivalEvents();

        while (statistics.getTotalPacketsNumber() <= TOTAL_SIMULATED_PACKETS) {

            statistics.updateVirtualQueueOccupancy(virtualQueue);

            PacketEvent currentEvent = getCurrentPacketEvent();
            currentTime = currentEvent.getTime();

            if (!greenQueue.isEmpty()) {
                dropStaleGreenPackets();
            }

            if (currentEvent.getType().equals(Enums.EVENT_TYPE.packetArrival)) {
                handleArrivalEvent(currentEvent);
            }

            if (currentEvent.getType().equals(Enums.EVENT_TYPE.packetLeaving)) {
                handleLeavingEvent(currentEvent);
            }
        }

        return statistics;
    }


    private PacketEvent getCurrentPacketEvent() {
        PacketEvent nextEvent = eventQueue.poll();

        if (nextEvent == null) {
            setupInitialArrivalEvents();
            nextEvent = eventQueue.poll();
        }
        return nextEvent;
    }


    private void handleArrivalEvent(PacketEvent packetEvent) {
        AbePacket arrivingPacket = packetEvent.getPacket();
        statistics.addPacket(arrivingPacket);

        if (virtualQueue.isEmpty() && !isRouterBusy) { // queue is empty and router is not busy
            /*
            So we are starting to serving this packet immediately and we are creating new event of packet leaving
            New packetLeaving event is possible when router will finish serving this packet

            Summing up - packetLeaving event is event of end serving last packet and getting new one from queues according to DSD
             */

            arrivingPacket.setComingTime(currentTime);
            isRouterBusy = true;

            if (arrivingPacket.getColor().equals(Enums.COLOR.blue)) {
                arrivingPacket.setDeadline(currentTime);
                addBluePacketArrivalEvent();
            } else {
                arrivingPacket.setDeadline(currentTime + GREEN_PACKET_DEADLINE);
                addGreenPacketArrivalEvent();
            }
            //tutaj mozliwe ze trzeba jednak ustawic czas zdarzenia wyjscia za kolejnym przyjscia, pakiet nie byl obsluzony zanim przyjdzie kolejny
            // ale w sumie raczej trzeba manipulowac czasami przyjscia - zeby router wolniej obslugiwal niz przychodza pakieyty
            //wtedy kolejka prawie pelna caly czas ...
            eventQueue.add(new PacketEvent(Enums.EVENT_TYPE.packetLeaving, arrivingPacket, currentTime + (double) arrivingPacket.getLength() / (double)ROUTER_THROUGHPUT));

        } else if (QueueUtils.getQueueLength(virtualQueue) + arrivingPacket.getLength() > VIRTUAL_QUEUE_SIZE) { //check if queue is full
            statistics.addPacketLoss(arrivingPacket);

        } else {//queue not full and not empty or queue empty but router is busy serving some previous packet
            arrivingPacket.setComingTime(currentTime);

            if (arrivingPacket.getColor().equals(Enums.COLOR.blue)) {
                arrivingPacket.setDeadline(currentTime + (double) QueueUtils.getQueueLength(virtualQueue) / (double) ROUTER_THROUGHPUT);

                virtualQueue.add(arrivingPacket);
                blueQueue.add(arrivingPacket);

                addBluePacketArrivalEvent(); // add next packet arrival event from blue stream - according to the poisson probability distribution for this stream
            } else {    //packet is green
                arrivingPacket.setDeadline(currentTime + GREEN_PACKET_DEADLINE);
                GreenAcceptanceTest greenAcceptanceTest = GreenAcceptanceTest.builder()
                        .greenPacket(arrivingPacket)
                        .greenQueue(greenQueue)
                        .blueQueue(blueQueue)
                        .throughput(ROUTER_THROUGHPUT)
                        .build();

                if (greenAcceptanceTest.isPacketAccepted()) {
                    virtualQueue.add(arrivingPacket);
                    greenQueue.add(arrivingPacket);
                } else {
                    statistics.addPacketLoss(arrivingPacket);
                }

                addGreenPacketArrivalEvent(); // add next packet arrival event from green stream - according to the poisson probability distribution for this stream
            }
        }
    }

    private void handleLeavingEvent(PacketEvent packetEvent) {
        AbePacket leavingPacket = packetEvent.getPacket();

        leavingPacket.setLeavingTime(currentTime);
        statistics.addPacketDelay(packetEvent.getPacket(), currentTime);

        if (greenQueue.isEmpty() && blueQueue.isEmpty()) {
            //both queues are empty - so we have situation that packet arrived to the empty queue and was started to be serving in router'
            // so we are now have event of leaving this packet  - we are marking router as not busy, and that's all

            // ! next event in event queue should be event of packet arrival to the queue

            isRouterBusy = false;
            return;
        } else {
            virtualQueue.poll(); // if queues not empty, we are taking head of virtual queue and delete it
        }

        //queues not empty  -- >  Packet Serving Algorithm - page 5 from ABE description (provided materials)
        if (greenQueue.isEmpty() && !blueQueue.isEmpty()) {
            serveBluePacket();
            return;
        }

        if (blueQueue.isEmpty() && !greenQueue.isEmpty()) {
            serveGreenPacket();
            return;
        }

        if (!blueQueue.isEmpty() && !greenQueue.isEmpty()) {
            double pG = (double)greenQueue.peek().getLength() / (double)ROUTER_THROUGHPUT; //pg - green transmission delay
            double deadG = greenQueue.peek().getDeadline();
            double pB = (double)blueQueue.peek().getLength() / (double)ROUTER_THROUGHPUT; //pb - green transmission delay
            double deadB = blueQueue.peek().getDeadline();

            if (currentTime > deadB - pG) {// same as -> currentTime + pG > deadB - so when we send green blue would be dead (stale)
                serveBluePacket();
            } else if (currentTime > deadG - pB) { // blue can wait but green cannot - if green wait more it would be stale
                serveGreenPacket();
            } else { // both colors can wait and they wont be stale - serving green and blue with 50% probability
                double randomValue = random.nextDouble();

                if (randomValue <= 0.6) {
                    serveBluePacket();
                } else {
                    serveGreenPacket();
                }
            }
        }
    }

    private void dropStaleGreenPackets() {
        greenQueue.stream()  //we are adding stale green packet as a lost
                .filter(packet -> currentTime > packet.getDeadline())
                .forEach(packet -> statistics.addPacketLoss(packet));

        greenQueue = greenQueue.stream() //reject stale green packets from green queue
                .filter(packet -> currentTime < packet.getDeadline())
                .collect(Collectors.toCollection(LinkedList<AbePacket>::new));

        virtualQueue = virtualQueue.stream()//reject stale green packets from virtual queue
                .filter(packet -> packet.getColor().equals(Enums.COLOR.green))
                .filter(packet -> currentTime < packet.getDeadline())
                .collect(Collectors.toCollection(LinkedList<AbePacket>::new));
    }


    private void serveBluePacket() {
        AbePacket bluePacket = blueQueue.poll();
        eventQueue.add(new PacketEvent(Enums.EVENT_TYPE.packetLeaving, bluePacket, currentTime + (bluePacket.getLength() / ROUTER_THROUGHPUT)));
    }

    private void serveGreenPacket() {
        AbePacket greenPacket = greenQueue.poll();
        eventQueue.add(new PacketEvent(Enums.EVENT_TYPE.packetLeaving, greenPacket, currentTime + (greenPacket.getLength() / ROUTER_THROUGHPUT)));
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

    private void setupInitialArrivalEvents() { // this method adds one event of arrival green packet and one arrival event for blue packet
        addGreenPacketArrivalEvent();
        addBluePacketArrivalEvent();
    }

}
