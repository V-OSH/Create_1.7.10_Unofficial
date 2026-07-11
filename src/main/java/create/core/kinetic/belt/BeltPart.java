package create.core.kinetic.belt;

/** Identifies a belt block's position within its chain. */
public enum BeltPart {
    START,   // index 0, owns BeltInventory, connects to shaft
    MIDDLE,  // index 1..length-2, no shaft connection
    END      // index length-1, connects to shaft
}
