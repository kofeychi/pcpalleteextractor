package dev.kofeychi.pcpalleteextractor.util;

import java.nio.file.Path;
import java.util.Arrays;

public interface TexturePaths {
    Path ROOT = Path.of(".");
    Path BLOCK = ROOT.resolve("block");
    Path TEXTURE_ROOT = BLOCK.resolve("texture");
    Path MODEL_ROOT = BLOCK.resolve("model");
    Resolver TEXTURE = resolvable(TEXTURE_ROOT);
    Resolver MODEL = resolvable(MODEL_ROOT);

    static void prepare(){
        Arrays.stream(TexturePaths.class.getFields()).filter(f -> f.getType() == Path.class&&!f.getName().equals("ROOT")).forEach(f -> {
            f.setAccessible(true);
            try {
                Path p = (Path) f.get(null);
                p.toFile().mkdirs();
            } catch (Exception e) {
            }
        });
    }

    static Resolver resolvable(Path root){
        return (s) -> root.resolve(s).toAbsolutePath();
    }
    interface Resolver{
        Path resolve(String s);
    }
}
