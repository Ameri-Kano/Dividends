package com.amerikano.dividendsproject.scheduler;

import com.amerikano.dividendsproject.model.*;
import com.amerikano.dividendsproject.model.constants.CacheKey;
import com.amerikano.dividendsproject.persist.*;
import com.amerikano.dividendsproject.persist.entity.*;
import com.amerikano.dividendsproject.scraper.Scraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@EnableCaching
@AllArgsConstructor
public class ScraperScheduler {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    private final Scraper yahooFinanceScraper;

    // Thread Pool 처리를 하지 않을 시 하나에 스레드에서만 실행되므로 올바른 처리가 되지 않을 수 있음
//    @Scheduled(fixedDelay = 1000)
//    public void test1() throws InterruptedException{
//        Thread.sleep(10000);
//        System.out.println(Thread.currentThread().getName() + " -> 테스트 1 : " + LocalDateTime.now());
//    }
//
//    public void test2() {
//        System.out.println(Thread.currentThread().getName() + " -> 테스트 2 : " + LocalDateTime.now());
//    }

    // 일정 주기마다 수행 + 캐시 정리
    @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true)
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling() {
        log.info("scraping scheduler is started");
        // 저장된 회사 목록을 조회
        List<CompanyEntity> companies = companyRepository.findAll();

        // 회사마다 배당금 정보를 새로 스크래핑
        for (CompanyEntity company : companies) {
            log.info("scraping scheduler is started ->" + company.getName());
            ScrapedResult scrapedResult = yahooFinanceScraper
                    .scrap(new Company(company.getTicker(), company.getName()));

            // 스크래핑한 배당금 정보 중 데이터베이스에 없다면 저장
            scrapedResult.getDividends().stream()
                    // Diviend -> DividendEntity 로 매핑
                    .map(e -> new DividendEntity(company.getId(), e))
                    // 하나씩 DiviendRepository에 삽입
                    .forEach(e -> {
                        boolean exists = dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                        if (!exists) {
                            dividendRepository.save(e);
                            log.info("insert new dividend -> " + e);
                        }
                    });

            // 연속적으로 스크래핑 대상 사이트에 요청을 날리지 않도록 일시정지
            try {
                Thread.sleep(3000); // 3초
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }
}
