package create.foundation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TextureAssetTest {

    private static final Path TEXTURE_ROOT = Path.of(
            "src/main/resources/assets/create/textures");

    @Test
    void legacyTextures_matchCreateSixPointZeroEightSources() throws Exception {
        Map<String, String> expectedHashes = Map.ofEntries(
                Map.entry("blocks/shaft.png",
                        "0AE58AA6FCC369FEE5A49270827BFA572631078F57163B8344F97EA5FCDE2441"),
                Map.entry("blocks/cogwheel.png",
                        "011591A34282037145FA38A5970B5B38E1C31D6B9F8A823B4598DAA7E79C5B08"),
                Map.entry("blocks/large_cogwheel.png",
                        "FDEA9C2C9CB758AD27170DCE94CA4ADB73B187DBE89098EC81264B4C0CDD48AC"),
                Map.entry("blocks/belt.png",
                        "CDD7C1F9AB00E19EF56E9531538759B79E9F54F4FBABF2F5EB3C818A25CD11C4"),
                Map.entry("blocks/millstone.png",
                        "FFE5D934C4749F628A22DD8E3F57AC705A02A250DD5B48388EABF16CC4A194A3"),
                Map.entry("blocks/drill.png",
                        "33F5C77EA57316CB96F57CC652D59C4F9D4890E2D5509F9A80CA4794DD442BD1"),
                Map.entry("blocks/encased_fan.png",
                        "984A636EDC1F48B0430D51303F09A614D5599510DBBA0F0BE06503E599465D43"),
                Map.entry("blocks/creative_motor.png",
                        "3DDC146F07931888526559423E35BCA2CB8209AFE4E1F12A85EB8243A7430CDD"),
                Map.entry("blocks/water_wheel.png",
                        "A055C31D000967F698C8CEB2C2A1F8CC1BF078AC7DEA72B11AF277964B2AA67B"),
                Map.entry("items/andesite_alloy.png",
                        "5A8A613D80AB6EC52712CF0FAE044572B12DD6BB0DFBCA946D2863B712A05D0F"),
                Map.entry("items/belt_connector.png",
                        "C6EE941707F0DA0602568821FC9AA3F53FFD751A56C655A50EE2772069680488")
        );

        for (Map.Entry<String, String> entry : expectedHashes.entrySet()) {
            Path texture = TEXTURE_ROOT.resolve(entry.getKey());
            assertEquals(entry.getValue(), sha256(texture), entry.getKey());
        }
    }

    private static String sha256(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(Files.readAllBytes(file));
        StringBuilder result = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }
}
