package dev.abdullahrathore.leadbook;

import java.util.Locale;
import java.util.Set;

/**
 * Turns whatever is in a "website" or "email" column into the domain that
 * identifies the business, so duplicates can be found.
 *
 * "https://www.Acme.com/contact?utm_source=x", "acme.com" and "info@acme.com" are
 * the same company. Sub-domains are folded too ("shop.acme.com"), except under the
 * second-level suffixes listed below, where the registrable part is three labels.
 * That list covers the common country suffixes, not the full public suffix list.
 */
public final class Domains {

    private static final Set<String> TWO_PART_SUFFIXES = Set.of(
            "co.uk", "org.uk", "ac.uk", "gov.uk", "com.au", "net.au", "org.au", "co.nz", "org.nz",
            "com.pk", "org.pk", "edu.pk", "co.in", "com.br", "com.mx", "co.za", "com.sg", "com.my",
            "com.tr", "com.cn", "co.jp", "co.kr", "com.ar", "com.co", "com.ng", "com.eg", "com.sa", "ae.org");

    /** Hosts that list businesses rather than being one. A lead pointing here is not a lead. */
    private static final Set<String> DIRECTORIES = Set.of(
            "yelp.com", "yellowpages.com", "yell.com", "tripadvisor.com", "angi.com", "angieslist.com",
            "thumbtack.com", "bbb.org", "houzz.com", "homeadvisor.com", "manta.com", "mapquest.com",
            "facebook.com", "instagram.com", "linkedin.com", "twitter.com", "x.com", "tiktok.com",
            "youtube.com", "pinterest.com", "google.com", "goo.gl", "g.page", "bing.com",
            "wikipedia.org", "crunchbase.com", "glassdoor.com", "indeed.com", "alibaba.com",
            "amazon.com", "ebay.com", "etsy.com", "upwork.com", "fiverr.com", "clutch.co");

    private Domains() {
    }

    /** The business domain for a URL, bare host or email address; null when there isn't one. */
    public static String of(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim().toLowerCase(Locale.ROOT);
        if (v.isEmpty()) {
            return null;
        }
        int at = v.lastIndexOf('@');
        if (at >= 0) {
            v = v.substring(at + 1);
        }
        v = v.replaceFirst("^[a-z][a-z0-9+.-]*://", "");
        int cut = indexOfAny(v, "/?#");
        if (cut >= 0) {
            v = v.substring(0, cut);
        }
        int colon = v.indexOf(':');
        if (colon >= 0) {
            v = v.substring(0, colon);
        }
        v = v.replaceAll("\\.+$", "");
        if (!v.contains(".") || !v.matches("[a-z0-9.-]+")) {
            return null;
        }
        String[] labels = v.split("\\.");
        int keep = labels.length >= 3 && TWO_PART_SUFFIXES.contains(labels[labels.length - 2] + "." + labels[labels.length - 1]) ? 3 : 2;
        if (labels.length <= keep) {
            return v.startsWith("www.") ? v.substring(4) : v;
        }
        return String.join(".", java.util.Arrays.copyOfRange(labels, labels.length - keep, labels.length));
    }

    public static boolean isDirectory(String domain) {
        return domain != null && DIRECTORIES.contains(domain);
    }

    private static int indexOfAny(String s, String chars) {
        int best = -1;
        for (char c : chars.toCharArray()) {
            int i = s.indexOf(c);
            if (i >= 0 && (best < 0 || i < best)) {
                best = i;
            }
        }
        return best;
    }
}
