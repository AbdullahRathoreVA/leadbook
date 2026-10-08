package dev.abdullahrathore.leadbook;

import java.util.ArrayList;
import java.util.List;

/**
 * Just enough RFC 4180 CSV: quoted fields, doubled quotes, commas and line breaks
 * inside quotes, and both \n and \r\n line endings. Lead lists exported from
 * spreadsheets and CRMs use all of these.
 */
public final class Csv {

    private Csv() {
    }

    public static List<List<String>> parse(String text) {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        int i = 0;
        if (text.startsWith("﻿")) {
            i = 1; // Excel's byte order mark
        }
        for (; i < text.length(); i++) {
            char c = text.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        quoted = false;
                    }
                } else {
                    field.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                row.add(field.toString());
                field.setLength(0);
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                }
                row.add(field.toString());
                field.setLength(0);
                if (!(row.size() == 1 && row.get(0).isEmpty())) {
                    rows.add(row);
                }
                row = new ArrayList<>();
            } else {
                field.append(c);
            }
        }
        if (field.length() > 0 || !row.isEmpty()) {
            row.add(field.toString());
            rows.add(row);
        }
        return rows;
    }

    public static String write(List<List<String>> rows) {
        StringBuilder sb = new StringBuilder();
        for (List<String> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(escape(row.get(i)));
            }
            sb.append("\r\n");
        }
        return sb.toString();
    }

    static String escape(String value) {
        String v = value == null ? "" : value;
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}
