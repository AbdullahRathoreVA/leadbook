package dev.abdullahrathore.leadbook;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Cleans a lead list:
 *
 * <ol>
 *   <li>works out each row's business domain from its website column, or its email
 *       column when the website is blank;</li>
 *   <li>drops rows whose domain is a directory or social network;</li>
 *   <li>merges rows with the same domain, keeping the first row and filling its empty
 *       cells from the duplicates, so no phone number or email is lost;</li>
 *   <li>adds a {@code domain} column and an {@code email_ok} column.</li>
 * </ol>
 *
 * Rows with no usable domain are kept as they are, because a name and a phone number
 * can still be a lead.
 */
public final class Cleaner {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[a-z]{2,}$", Pattern.CASE_INSENSITIVE);

    public record Result(List<List<String>> rows, Stats stats) {
    }

    public record Stats(int read, int kept, int merged, int directoriesDropped, int badEmails) {
    }

    private Cleaner() {
    }

    public static Result clean(List<List<String>> input) {
        if (input.isEmpty()) {
            return new Result(List.of(), new Stats(0, 0, 0, 0, 0));
        }
        List<String> header = input.get(0);
        int site = find(header, "website", "url", "site", "domain", "web");
        int email = find(header, "email", "e-mail", "mail");

        List<String> outHeader = new ArrayList<>(header);
        outHeader.add("domain");
        outHeader.add("email_ok");

        Map<String, List<String>> byDomain = new LinkedHashMap<>();
        List<List<String>> noDomain = new ArrayList<>();
        int merged = 0;
        int dropped = 0;
        int read = input.size() - 1;

        for (List<String> raw : input.subList(1, input.size())) {
            List<String> row = pad(raw, header.size());
            String domain = Domains.of(cell(row, site));
            if (domain == null) {
                domain = Domains.of(cell(row, email));
            }
            if (Domains.isDirectory(domain)) {
                dropped++;
                continue;
            }
            if (domain == null) {
                noDomain.add(row);
                continue;
            }
            List<String> first = byDomain.get(domain);
            if (first == null) {
                byDomain.put(domain, row);
            } else {
                merged++;
                for (int i = 0; i < first.size(); i++) {
                    if (first.get(i).isBlank() && !row.get(i).isBlank()) {
                        first.set(i, row.get(i));
                    }
                }
            }
        }

        List<List<String>> out = new ArrayList<>();
        out.add(outHeader);
        int bad = 0;
        for (Map.Entry<String, List<String>> e : byDomain.entrySet()) {
            bad += addRow(out, e.getValue(), e.getKey(), email);
        }
        for (List<String> row : noDomain) {
            bad += addRow(out, row, "", email);
        }
        return new Result(out, new Stats(read, out.size() - 1, merged, dropped, bad));
    }

    private static int addRow(List<List<String>> out, List<String> row, String domain, int emailCol) {
        List<String> r = new ArrayList<>(row);
        String e = cell(row, emailCol).trim();
        boolean ok = !e.isEmpty() && EMAIL.matcher(e).matches();
        r.add(domain);
        r.add(e.isEmpty() ? "" : (ok ? "yes" : "no"));
        out.add(r);
        return !e.isEmpty() && !ok ? 1 : 0;
    }

    /** Index of the first header that matches one of the names, ignoring case and spaces; -1 if none. */
    static int find(List<String> header, String... names) {
        for (String name : names) {
            for (int i = 0; i < header.size(); i++) {
                String h = header.get(i).trim().toLowerCase(Locale.ROOT).replace(" ", "");
                if (h.equals(name) || h.startsWith(name)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static String cell(List<String> row, int index) {
        return index >= 0 && index < row.size() ? row.get(index) : "";
    }

    private static List<String> pad(List<String> row, int size) {
        List<String> r = new ArrayList<>(row);
        while (r.size() < size) {
            r.add("");
        }
        return r;
    }
}
