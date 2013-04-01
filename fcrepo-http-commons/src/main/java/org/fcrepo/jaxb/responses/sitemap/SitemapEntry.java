package org.fcrepo.jaxb.responses.sitemap;

import javax.jcr.RepositoryException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.Calendar;

@XmlRootElement(name = "url",  namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
public class SitemapEntry {
    @XmlElement(namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
    private final URI loc;

    @XmlElement(namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
    private final Calendar lastmod;

    @XmlElement(namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
    private final String changefreq = "monthly";

    @XmlElement(namespace = "http://www.sitemaps.org/schemas/sitemap/0.9")
    private final double priority = 0.8;

    public SitemapEntry() {
        loc = null;
        lastmod = null;
    }


    public SitemapEntry(URI loc) throws RepositoryException {
        this.loc = loc;
        this.lastmod = Calendar.getInstance();
    }

    public SitemapEntry(URI loc, Calendar lastmod) throws RepositoryException {
        this.loc = loc;
        this.lastmod = lastmod;
    }


}