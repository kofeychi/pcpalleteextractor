package dev.kofeychi.pcpalleteextractor.util;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Id {
    public String namespace;
    public String path;

    public static Id parse(String str) {
        var arr = str.split(":");
        return new Id(arr[0], arr[1]);
    }
}
