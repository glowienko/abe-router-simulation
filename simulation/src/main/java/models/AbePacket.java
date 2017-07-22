package models;


import lombok.Getter;
import lombok.Setter;
import utils.Enums;

/**
 * Alternative best effort packet
 */
@Getter
@Setter
public class AbePacket extends Packet {

    private Enums.COLOR color;
    private double deadline;
}
