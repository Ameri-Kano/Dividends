package com.amerikano.dividendsproject.scraper;

import com.amerikano.dividendsproject.model.*;

public interface Scraper {
    Company scrapCompanyByTicker(String ticker);
    ScrapedResult scrap(Company company);
}
