# leadbook

Clean a lead list in one command. Point it at the CSV you exported from a scraper,
a spreadsheet or a CRM and it:

- **merges duplicates** that are the same business written differently:
  `https://www.acme.com/contact`, `acme.com` and `info@acme.com` are one company,
  and the merged row keeps every phone number and email the duplicates had;
- **drops directory listings**: a Yelp, Facebook or Google Maps link is the place
  the business is listed, not the business;
- **flags bad email addresses** before they bounce;
- adds `domain` and `email_ok` columns and writes a clean CSV.

```
$ java -jar target/leadbook-0.1.0.jar examples/leads.csv
read 6 rows, kept 4
  merged duplicates:   1
  dropped directories: 1
  bad email addresses: 1
wrote examples/leads.clean.csv
```

## Build and run

Needs Java 21 and Maven. No runtime dependencies.

```bash
mvn -q package
java -jar target/leadbook-0.1.0.jar leads.csv            # writes leads.clean.csv
java -jar target/leadbook-0.1.0.jar leads.csv -o out.csv
```

## How columns are found

The first header row is used. The website column is the first header starting with
`website`, `url`, `site`, `domain` or `web`; the email column the first starting with
`email`, `e-mail` or `mail`. Case and spaces don't matter. When a row has no website,
its email's domain is used instead. Rows with neither are kept unchanged, because a
name and a phone number can still be a lead.

## How domains are compared

Scheme, `www.`, port, path, query and sub-domains are removed, so `shop.acme.com` and
`acme.com` match. For common two-part country suffixes (`co.uk`, `com.au`, `com.pk`,
`co.nz` and others) three labels are kept, so `brightsmile.co.uk` stays intact. This
is a practical list rather than the full public suffix list.

## Tests

```bash
mvn test
```

## License

MIT
