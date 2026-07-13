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

    @Test
    void meshedSmallCogwheelsReverseRotationWithoutChangingSpeed() {
        MemoryNetwork world = new MemoryNetwork();
        LegacyKineticNetwork.Position drivingCog = new LegacyKineticNetwork.Position(0, 0, 0);
        LegacyKineticNetwork.Position drivenCog = new LegacyKineticNetwork.Position(1, 0, 0);
        LegacyKineticNetwork.Position outputShaft = new LegacyKineticNetwork.Position(1, 1, 0);
        world.put(drivingCog, Node.withModifiers(32f, ForgeDirection.EAST, -1));
        world.put(drivenCog, Node.withModifiers(null, ForgeDirection.WEST, -1, ForgeDirection.UP, 1));
        world.put(outputShaft, new Node(null, ForgeDirection.DOWN));

        LegacyKineticNetwork.rebuildAt(world, drivingCog);

        assertEquals(-32f, world.speed(drivenCog));
        assertEquals(-32f, world.speed(outputShaft));
    }

    @Test
    void diagonalLargeCogwheelDoublesConnectedSmallCogwheelSpeed() {
        GraphNetwork world = new GraphNetwork();
        LegacyKineticNetwork.Position largeCog = new LegacyKineticNetwork.Position(0, 0, 0);
        LegacyKineticNetwork.Position smallCog = new LegacyKineticNetwork.Position(1, 0, 1);
        LegacyKineticNetwork.Position outputShaft = new LegacyKineticNetwork.Position(1, 1, 1);
        world.put(largeCog, 32f);
        world.put(smallCog, null);
        world.put(outputShaft, null);
        world.connect(largeCog, smallCog, -2, -.5f);
        world.connect(smallCog, outputShaft, 1, 1);

        LegacyKineticNetwork.rebuildAt(world, largeCog);

        assertEquals(-64f, world.speed(smallCog));
        assertEquals(-64f, world.speed(outputShaft));

        world.remove(largeCog);
        LegacyKineticNetwork.rebuildAt(world, smallCog);
        assertEquals(0f, world.speed(smallCog));
        assertEquals(0f, world.speed(outputShaft));
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
        public float speedModifier(LegacyKineticNetwork.Position position, ForgeDirection direction) {
            Node node = nodes.get(position);
            return node == null ? 0 : node.modifiers.getOrDefault(direction, 0f);
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
        private final Map<ForgeDirection, Float> modifiers = new HashMap<>();
        private float speed;

        Node(Float sourceSpeed, ForgeDirection... connections) {
            this.sourceSpeed = sourceSpeed;
            this.connections = connections;
            for (ForgeDirection connection : connections) {
                modifiers.put(connection, 1f);
            }
        }

        static Node withModifiers(Float sourceSpeed, Object... directionAndModifier) {
            Node node = new Node(sourceSpeed);
            for (int index = 0; index < directionAndModifier.length; index += 2) {
                ForgeDirection direction = (ForgeDirection) directionAndModifier[index];
                float modifier = ((Number) directionAndModifier[index + 1]).floatValue();
                node.modifiers.put(direction, modifier);
            }
            return node;
        }
    }

    private static final class GraphNetwork implements LegacyKineticNetwork.NetworkView {

        private record Edge(LegacyKineticNetwork.Position from, LegacyKineticNetwork.Position to) {}

        private final Map<LegacyKineticNetwork.Position, Float> sources = new HashMap<>();
        private final Map<LegacyKineticNetwork.Position, Float> speeds = new HashMap<>();
        private final Map<Edge, Float> modifiers = new HashMap<>();

        void put(LegacyKineticNetwork.Position position, Float sourceSpeed) {
            sources.put(position, sourceSpeed);
            speeds.put(position, 0f);
        }

        void connect(LegacyKineticNetwork.Position from, LegacyKineticNetwork.Position to, float forward,
            float backward) {
            modifiers.put(new Edge(from, to), forward);
            modifiers.put(new Edge(to, from), backward);
        }

        void remove(LegacyKineticNetwork.Position position) {
            sources.remove(position);
            speeds.remove(position);
        }

        float speed(LegacyKineticNetwork.Position position) {
            return speeds.get(position);
        }

        @Override
        public boolean isKinetic(LegacyKineticNetwork.Position position) {
            return sources.containsKey(position);
        }

        @Override
        public boolean connects(LegacyKineticNetwork.Position position, ForgeDirection direction) {
            return false;
        }

        @Override
        public Iterable<LegacyKineticNetwork.Position> neighbours(LegacyKineticNetwork.Position position) {
            return modifiers.keySet().stream().filter(edge -> edge.from().equals(position)).map(Edge::to).toList();
        }

        @Override
        public float speedModifier(LegacyKineticNetwork.Position position,
            LegacyKineticNetwork.Position neighbour) {
            return modifiers.getOrDefault(new Edge(position, neighbour), 0f);
        }

        @Override
        public Float sourceSpeed(LegacyKineticNetwork.Position position) {
            return sources.get(position);
        }

        @Override
        public void setSpeed(LegacyKineticNetwork.Position position, float speed) {
            speeds.put(position, speed);
        }
    }
}
