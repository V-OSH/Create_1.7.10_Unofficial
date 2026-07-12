package com.simibubi.create.foundation.utility.legacy.render;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraftforge.common.util.ForgeDirection;

/** Exact legacy rendering data loaded from Create 6.0.8's original motor JSON models. */
public final class CreativeMotorModel {

    private static final String HORIZONTAL_MODEL =
        "/assets/create/models/block/creative_motor/block.json";
    private static final String VERTICAL_MODEL =
        "/assets/create/models/block/creative_motor/block_vertical.json";

    public enum Texture {
        CASING, MOTOR, AXIS, FLAP
    }

    public enum Direction {
        DOWN(0, -1, 0), UP(0, 1, 0), NORTH(0, 0, -1), SOUTH(0, 0, 1), WEST(-1, 0, 0), EAST(1, 0, 0);

        private final int x;
        private final int y;
        private final int z;

        Direction(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    public record Cuboid(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {}

    public record Uv(double minU, double minV, double maxU, double maxV) {}

    public record Face(Texture texture, Uv uv, int rotation) {}

    public record Element(Cuboid bounds, Texture sideTexture, Texture capTexture, Map<Direction, Face> faces) {}

    private static final List<Element> HORIZONTAL = loadModel(HORIZONTAL_MODEL, ForgeDirection.SOUTH);
    private static final List<Element> VERTICAL = loadModel(VERTICAL_MODEL, ForgeDirection.UP);

    private CreativeMotorModel() {}

    public static List<Element> baseHorizontalElements() {
        return HORIZONTAL;
    }

    public static List<Element> baseElementsFor(ForgeDirection facing) {
        ForgeDirection safeFacing = safeFacing(facing);
        return safeFacing.offsetY == 0 ? HORIZONTAL : VERTICAL;
    }

    public static ForgeDirection baseFacingFor(ForgeDirection facing) {
        return safeFacing(facing).offsetY == 0 ? ForgeDirection.SOUTH : ForgeDirection.UP;
    }

    public static List<Element> elementsFor(ForgeDirection facing) {
        ForgeDirection safeFacing = safeFacing(facing);
        List<Element> source = baseElementsFor(safeFacing);
        ForgeDirection sourceFacing = baseFacingFor(safeFacing);
        if (safeFacing == sourceFacing) {
            return source;
        }
        List<Element> transformed = new ArrayList<>(source.size());
        for (Element element : source) {
            Map<Direction, Face> faces = new EnumMap<>(Direction.class);
            for (Map.Entry<Direction, Face> entry : element.faces().entrySet()) {
                faces.put(transformDirection(entry.getKey(), sourceFacing, safeFacing), entry.getValue());
            }
            transformed.add(new Element(transform(element.bounds(), sourceFacing, safeFacing), element.sideTexture(),
                element.capTexture(), Map.copyOf(faces)));
        }
        return List.copyOf(transformed);
    }

    public static double[] transformPoint(double x, double y, double z, ForgeDirection source,
        ForgeDirection target) {
        return source == ForgeDirection.SOUTH ? rotateFromSouth(x, y, z, target) : rotateFromUp(x, y, z, target);
    }

    public static Direction transformDirection(Direction direction, ForgeDirection source, ForgeDirection target) {
        double[] start = transformPoint(.5, .5, .5, source, target);
        double[] end = transformPoint(.5 + direction.x * .25, .5 + direction.y * .25,
            .5 + direction.z * .25, source, target);
        int x = (int) Math.signum(end[0] - start[0]);
        int y = (int) Math.signum(end[1] - start[1]);
        int z = (int) Math.signum(end[2] - start[2]);
        for (Direction candidate : Direction.values()) {
            if (candidate.x == x && candidate.y == y && candidate.z == z) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("Unable to transform face direction");
    }

    private static List<Element> loadModel(String path, ForgeDirection axis) {
        try (InputStream stream = CreativeMotorModel.class.getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("Missing upstream motor model: " + path);
            }
            JsonObject root = new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8))
                .getAsJsonObject();
            List<Element> elements = new ArrayList<>();
            for (JsonElement jsonElement : root.getAsJsonArray("elements")) {
                JsonObject object = jsonElement.getAsJsonObject();
                Cuboid bounds = bounds(object.getAsJsonArray("from"), object.getAsJsonArray("to"));
                Map<Direction, Face> faces = faces(object.getAsJsonObject("faces"));
                Texture side = representativeTexture(faces, axis, false);
                Texture cap = representativeTexture(faces, axis, true);
                elements.add(new Element(bounds, side, cap, Map.copyOf(faces)));
            }
            return List.copyOf(elements);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read upstream motor model: " + path, exception);
        }
    }

    private static Cuboid bounds(JsonArray from, JsonArray to) {
        return new Cuboid(coordinate(from, 0), coordinate(from, 1), coordinate(from, 2), coordinate(to, 0),
            coordinate(to, 1), coordinate(to, 2));
    }

    private static double coordinate(JsonArray coordinates, int index) {
        return coordinates.get(index).getAsDouble() / 16.0;
    }

    private static Map<Direction, Face> faces(JsonObject object) {
        Map<Direction, Face> faces = new EnumMap<>(Direction.class);
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            JsonObject face = entry.getValue().getAsJsonObject();
            JsonArray uv = face.getAsJsonArray("uv");
            Uv coordinates = new Uv(uv.get(0).getAsDouble(), uv.get(1).getAsDouble(), uv.get(2).getAsDouble(),
                uv.get(3).getAsDouble());
            int rotation = face.has("rotation") ? face.get("rotation").getAsInt() : 0;
            faces.put(Direction.valueOf(entry.getKey().toUpperCase()),
                new Face(texture(face.get("texture").getAsString()), coordinates, rotation));
        }
        return faces;
    }

    private static Texture texture(String reference) {
        return switch (reference) {
            case "#5" -> Texture.CASING;
            case "#6" -> Texture.MOTOR;
            case "#7" -> Texture.FLAP;
            case "#1_0" -> Texture.AXIS;
            default -> throw new IllegalArgumentException("Unknown motor texture reference: " + reference);
        };
    }

    private static Texture representativeTexture(Map<Direction, Face> faces, ForgeDirection axis, boolean cap) {
        for (Map.Entry<Direction, Face> entry : faces.entrySet()) {
            Direction direction = entry.getKey();
            boolean alongAxis = axis.offsetX != 0 && direction.x != 0 || axis.offsetY != 0 && direction.y != 0
                || axis.offsetZ != 0 && direction.z != 0;
            if (alongAxis == cap) {
                return entry.getValue().texture();
            }
        }
        return faces.values().iterator().next().texture();
    }

    private static ForgeDirection safeFacing(ForgeDirection facing) {
        return facing == null || facing == ForgeDirection.UNKNOWN ? ForgeDirection.SOUTH : facing;
    }

    private static Cuboid transform(Cuboid bounds, ForgeDirection source, ForgeDirection target) {
        double minX = 1;
        double minY = 1;
        double minZ = 1;
        double maxX = 0;
        double maxY = 0;
        double maxZ = 0;
        for (double x : new double[] {bounds.minX(), bounds.maxX()}) {
            for (double y : new double[] {bounds.minY(), bounds.maxY()}) {
                for (double z : new double[] {bounds.minZ(), bounds.maxZ()}) {
                    double[] point = transformPoint(x, y, z, source, target);
                    minX = Math.min(minX, point[0]);
                    minY = Math.min(minY, point[1]);
                    minZ = Math.min(minZ, point[2]);
                    maxX = Math.max(maxX, point[0]);
                    maxY = Math.max(maxY, point[1]);
                    maxZ = Math.max(maxZ, point[2]);
                }
            }
        }
        return new Cuboid(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static double[] rotateFromSouth(double x, double y, double z, ForgeDirection target) {
        return switch (target) {
            case NORTH -> new double[] {1 - x, y, 1 - z};
            case EAST -> new double[] {z, y, 1 - x};
            case WEST -> new double[] {1 - z, y, x};
            default -> new double[] {x, y, z};
        };
    }

    private static double[] rotateFromUp(double x, double y, double z, ForgeDirection target) {
        return target == ForgeDirection.DOWN ? new double[] {x, 1 - y, 1 - z} : new double[] {x, y, z};
    }
}
