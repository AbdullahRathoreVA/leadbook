package dev.abdullahrathore.leadbook;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * <pre>
 *   java -jar leadbook.jar leads.csv                 # writes leads.clean.csv
 *   java -jar leadbook.jar leads.csv -o out.csv
 * </pre>
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        Path in = null;
        Path out = null;
        for (int i = 0; i < args.length; i++) {
            if ((args[i].equals("-o") || args[i].equals("--out")) && i + 1 < args.length) {
                out = Path.of(args[++i]);
            } else if (args[i].equals("-h") || args[i].equals("--help")) {
                usage();
                return;
            } else {
                in = Path.of(args[i]);
            }
        }
        if (in == null) {
            usage();
            System.exit(2);
        }
        if (out == null) {
            String name = in.getFileName().toString().replaceFirst("\\.csv$", "");
            out = in.resolveSibling(name + ".clean.csv");
        }

        Cleaner.Result result = Cleaner.clean(Csv.parse(Files.readString(in, StandardCharsets.UTF_8)));
        Files.writeString(out, Csv.write(result.rows()), StandardCharsets.UTF_8);

        Cleaner.Stats s = result.stats();
        System.out.printf("read %d rows, kept %d%n", s.read(), s.kept());
        System.out.printf("  merged duplicates:   %d%n", s.merged());
        System.out.printf("  dropped directories: %d%n", s.directoriesDropped());
        System.out.printf("  bad email addresses: %d%n", s.badEmails());
        System.out.println("wrote " + out);
    }

    private static void usage() {
        System.out.println("usage: java -jar leadbook.jar <leads.csv> [-o clean.csv]");
    }
}
