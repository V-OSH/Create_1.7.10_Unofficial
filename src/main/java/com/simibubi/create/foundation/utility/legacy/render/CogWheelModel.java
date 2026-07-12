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

/** Cached rendering data loaded from Create 6.0.8's unchanged cogwheel model. */
public final class CogWheelModel {

    private static final String MODEL = "/assets/create/models/block/cogwheel.json";

    public enum Texture {
        AXIS, AXIS_TOP, COGWHEEL
    }

    public enum Direction {
        DOWN, UP, NORTH, SOUTH, WEST, EAST
    }

    public record Cuboid(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {}

    public record Uv(double minU, double minV, double maxU, double maxV) {}

    public record Face(Texture texture, Uv uv, int rotation) {}

    public record Rotation(double angle, char axis, double originX, double originY, double originZ) {}

    public record Element(Cuboid bounds, Rotation rotation, Map<Direction, Face> faces) {}

    private static final ModelData DATA = load();

    private CogWheelModel() {}

    public static List<Element> elements() {
        return DATA.elements();
    }

    static Uv shrinkUv(Uv uv, Texture texture) {
        // Minecraft 1.20.1 FaceBakery moves each edge toward the face centre by
        // TextureAtlasSprite.uvShrinkRatio() (4 / sprite pixel width) before atlas interpolation.
        double shrink = 4.0 / textureWidth(texture);
        double centreU = (uv.minU() + uv.maxU()) / 2;
        double centreV = (uv.minV() + uv.maxV()) / 2;
        return new Uv(lerp(shrink, uv.minU(), centreU), lerp(shrink, uv.minV(), centreV),
            lerp(shrink, uv.maxU(), centreU), lerp(shrink, uv.maxV(), centreV));
    }

    static double[][] uvCorners(Face face, Direction direction) {
        Uv uv = shrinkUv(face.uv(), face.texture());
        if (face.texture() == Texture.COGWHEEL && direction != Direction.UP && direction != Direction.DOWN) {
            double pixel = 16.0 / textureWidth(face.texture());
            uv = new Uv(uv.minU() + pixel, uv.minV() + pixel, uv.maxU() - pixel, uv.maxV() - pixel);
        }
        return new double[][] {{uv.minU(), uv.minV()}, {uv.minU(), uv.maxV()}, {uv.maxU(), uv.maxV()},
            {uv.maxU(), uv.minV()}};
    }

    private static int textureWidth(Texture texture) {
        return texture == Texture.COGWHEEL ? 32 : 16;
    }

    private static double lerp(double amount, double start, double end) {
        return start + amount * (end - start);
    }

    private static ModelData load() {
        try (InputStream stream = CogWheelModel.class.getResourceAsStream(MODEL)) {
            if (stream == null) {
                throw new IllegalStateException("Missing upstream cogwheel model");
            }
            JsonObject root = new JsonParser().parse(new InputStreamReader(stream, StandardCharsets.UTF_8))
                .getAsJsonObject();
            List<Element> elements = new ArrayList<>();
            for (JsonElement jsonElement : root.getAsJsonArray("elements")) {
                JsonObject object = jsonElement.getAsJsonObject();
                elements.add(new Element(bounds(object), rotation(object), faces(object.getAsJsonObject("faces"))));
            }
            return new ModelData(List.copyOf(elements));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read upstream cogwheel model", exception);
        }
    }

    private static Cuboid bounds(JsonObject object) {
        JsonArray from = object.getAsJsonArray("from");
        JsonArray to = object.getAsJsonArray("to");
        return new Cuboid(coordinate(from, 0), coordinate(from, 1), coordinate(from, 2), coordinate(to, 0),
            coordinate(to, 1), coordinate(to, 2));
    }

    private static double coordinate(JsonArray coordinates, int index) {
        return coordinates.get(index).getAsDouble() / 16;
    }

    private static Rotation rotation(JsonObject object) {
        if (!object.has("rotation")) {
            return new Rotation(0, 'y', .5, .5, .5);
        }
        JsonObject rotation = object.getAsJsonObject("rotation");
        JsonArray origin = rotation.getAsJsonArray("origin");
        return new Rotation(rotation.get("angle").getAsDouble(), rotation.get("axis").getAsString().charAt(0),
            coordinate(origin, 0), coordinate(origin, 1), coordinate(origin, 2));
    }

    private static Map<Direction, Face> faces(JsonObject object) {
        Map<Direction, Face> faces = new EnumMap<>(Direction.class);
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            JsonObject face = entry.getValue().getAsJsonObject();
            JsonArray uv = face.getAsJsonArray("uv");
            int rotation = face.has("rotation") ? face.get("rotation").getAsInt() : 0;
            faces.put(Direction.valueOf(entry.getKey().toUpperCase()),
                new Face(texture(face.get("texture").getAsString()),
                    new Uv(uv.get(0).getAsDouble(), uv.get(1).getAsDouble(), uv.get(2).getAsDouble(),
                        uv.get(3).getAsDouble()),
                    rotation));
        }
        return Map.copyOf(faces);
    }

    private static Texture texture(String reference) {
        return switch (reference) {
            case "#0" -> Texture.AXIS;
            case "#3" -> Texture.AXIS_TOP;
            case "#1_2" -> Texture.COGWHEEL;
            default -> throw new IllegalArgumentException("Unknown cogwheel texture reference: " + reference);
        };
    }

    private record ModelData(List<Element> elements) {}
}
