package dev.abdullahrathore.leadbook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class LeadbookTest {

    @Test
    void urlsHostsAndEmailsReduceToTheSameDomain() {
        assertEquals("acme.com", Domains.of("https://www.Acme.com/contact?utm_source=x"));
        assertEquals("acme.com", Domains.of("acme.com"));
        assertEquals("acme.com", Domains.of("info@acme.com"));
        assertEquals("acme.com", Domains.of("http://shop.acme.com:8080/"));
        assertEquals("brightsmile.co.uk", Domains.of("www.brightsmile.co.uk"));
        assertEquals("tilesbyali.com.pk", Domains.of("https://shop.tilesbyali.com.pk"));
        assertNull(Domains.of("n/a"));
        assertNull(Domains.of(""));
    }

    @Test
    void csvHandlesQuotesCommasAndLineBreaks() {
        List<List<String>> rows = Csv.parse("name,note\r\n\"Smith, Jones & Co\",\"said \"\"hi\"\"\nthen left\"\n");
        assertEquals(List.of("name", "note"), rows.get(0));
        assertEquals(List.of("Smith, Jones & Co", "said \"hi\"\nthen left"), rows.get(1));
        assertEquals(rows, Csv.parse(Csv.write(rows)));
    }

    @Test
    void duplicatesAreMergedAndKeepEveryContactDetail() {
        List<List<String>> in = Csv.parse("""
                Company,Website,Email,Phone
                Acme Plumbing,https://www.acme.com,,512-555-0100
                Acme,acme.com/about,info@acme.com,
                Bob's Bakery,,bob@bobsbakery.com,
                """);
        Cleaner.Result r = Cleaner.clean(in);
        assertEquals(1, r.stats().merged());
        assertEquals(2, r.stats().kept());
        List<String> acme = r.rows().get(1);
        assertEquals(List.of("Acme Plumbing", "https://www.acme.com", "info@acme.com", "512-555-0100", "acme.com", "yes"), acme);
        assertEquals("bobsbakery.com", r.rows().get(2).get(4));
    }

    @Test
    void directoryListingsAreDroppedAndBadEmailsFlagged() {
        List<List<String>> in = Csv.parse("""
                name,url,email
                Joe's Cafe,https://www.yelp.com/biz/joes-cafe,
                Real Cafe,realcafe.com,hello@realcafe
                """);
        Cleaner.Result r = Cleaner.clean(in);
        assertEquals(1, r.stats().directoriesDropped());
        assertEquals(1, r.stats().badEmails());
        assertEquals("no", r.rows().get(1).get(4));
    }

    @Test
    void rowsWithoutAnyDomainAreKept() {
        Cleaner.Result r = Cleaner.clean(Csv.parse("name,phone\nWalk-in Barber,555-0199\n"));
        assertEquals(1, r.stats().kept());
        assertTrue(r.rows().get(1).get(2).isEmpty());
    }
}
