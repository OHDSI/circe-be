package org.ohdsi.circe;

public class Utils {
    public static String normalizeLineEnds(String s) {
        return s.replace("\r\n", "\n").replace('\r', '\n');
    }
}
