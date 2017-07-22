package models;

import lombok.*;
import utils.Enums;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PacketEvent {

    private Enums.EVENT_TYPE type;

    @NonNull
    private AbePacket packet;

    private double time; // event launch - czas nastąpienia zdarzenia
}
