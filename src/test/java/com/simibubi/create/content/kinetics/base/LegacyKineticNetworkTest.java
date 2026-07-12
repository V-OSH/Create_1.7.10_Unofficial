package com.simibubi.create.content.kinetics.base;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import net.minecraftforge.common.util.ForgeDirection;

class LegacyKineticNetworkTest {

    @Test
    void motorPowersConnectedShaftsAndDisconnectClearsThem() {
        MemoryNetwork world = new MemoryNetwork();
        LegacyKineticNetwork.Position motor = new LegacyKineticNetwork.Position(0, 0, 0);
        LegacyKineticNetwork.Position firstShaft = new LegacyKineticNetwork.Position(1, 0, 0);
        LegacyKineticNetwork.Position secondShaft = new LegacyKineticNetwork.Position(2, 0, 0);
        world.put(motor, new Node(32f, ForgeDirection.EAST));
        world.put(firstShaft, new Node(null, ForgeDirection.WEST, ForgeDirection.EAST));
        world.put(secondShaft, new Node(null, ForgeDirection.WEST, ForgeDirection.EAST));

        LegacyKineticNetwork.rebuildAt(world, motor);
        assertEquals(32f, world.speed(firstShaft));
        assertEquals(32f, world.speed(secondShaft));

        world.remove(motor);
        LegacyKineticNetwork.rebuildAt(world, motor);
        assertEquals(0f, world.speed(firstShaft));
        assertEquals(0f, world.speed(secondShaft));
    }

    @Test
    void fasterSourceOverpowersSlowerSourceButEqualOppositesStop() {
        MemoryNetwork world = new MemoryNetwork();
        LegacyKineticNetwork.Position leftMotor = new LegacyKineticNetwork.Position(-1, 0, 0);
        LegacyKineticNetwork.Position shaft = new LegacyKineticNetwork.Position(0, 0, 0);
        LegacyKineticNetwork.Position rightMotor = new LegacyKineticNetwork.Position(1, 0, 0);
        world.put(leftMotor, new Node(32f, ForgeDirection.EAST));
        world.put(shaft, new Node(null, ForgeDirection.WEST, ForgeDirection.EAST));
        world.put(rightMotor, new Node(-16f, ForgeDirection.WEST));

        LegacyKineticNetwork.rebuildAt(world, shaft);
        assertEquals(32f, world.speed(shaft));

        world.put(rightMotor, new Node(-32f, ForgeDirection.WEST));
        LegacyKineticNetwork.rebuildAt(world, shaft);
        assertEquals(0f, world.speed(shaft));
    }

    private static final class MemoryNetwork implements LegacyKineticNetwork.NetworkView {

        private final Map<LegacyKineticNetwork.Position, Node> nodes = new HashMap<>();

        void put(LegacyKineticNetwork.Position position, Node node) {
            nodes.put(position, node);
        }

        void remove(LegacyKineticNetwork.Position position) {
            nodes.remove(position);
        }

        float speed(LegacyKineticNetwork.Position position) {
            return nodes.get(position).speed;
        }

        @Override
        public boolean isKinetic(LegacyKineticNetwork.Position position) {
            return nodes.containsKey(position);
        }

        @Override
        public boolean connects(LegacyKineticNetwork.Position position, ForgeDirection direction) {
            Node node = nodes.get(position);
            if (node == null) {
                return false;
            }
            for (ForgeDirection connection : node.connections) {
                if (connection == direction) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Float sourceSpeed(LegacyKineticNetwork.Position position) {
            return nodes.get(position).sourceSpeed;
        }

        @Override
        public void setSpeed(LegacyKineticNetwork.Position position, float speed) {
            nodes.get(position).speed = speed;
        }
    }

    private static final class Node {

        private final Float sourceSpeed;
        private final ForgeDirection[] connections;
        private float speed;

        Node(Float sourceSpeed, ForgeDirection... connections) {
            this.sourceSpeed = sourceSpeed;
            this.connections = connections;
        }
    }
}
