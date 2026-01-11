package dev.kofeychi.pcpalleteextractor.model;

import dev.kofeychi.pcpalleteextractor.util.Id;
import lombok.Data;

import java.util.HashMap;

@Data
public class ParsedModelFile {
    public Id parent;
    public HashMap<String,Id> textures;
}
