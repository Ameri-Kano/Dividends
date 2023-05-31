package com.amerikano.dividendsproject.service;

import com.amerikano.dividendsproject.exception.impl.NoCompanyException;
import com.amerikano.dividendsproject.model.*;
import com.amerikano.dividendsproject.model.constants.CacheKey;
import com.amerikano.dividendsproject.persist.*;
import com.amerikano.dividendsproject.persist.entity.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    // 데이터 캐싱을 위해 고려해야 할 것들
    // 요청이 자주 들어오는가? -> 자주 들어옴
    // 자주 변경되는 데이터인가? -> 한번 기록된 배당금 정보는 변하지 않음
    // 캐시를 구현하기에 적합
    @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
    public ScrapedResult getDividendByCompanyName(String companyName) {
        log.info("search company -> " + companyName);
        // 1. 회사명 기준으로 회사 정보 조회
        CompanyEntity company = companyRepository.findByName(companyName)
                .orElseThrow(() -> new NoCompanyException());

        // 2. 조회된 회사의 ID로 배당금 정보 조회
        List<DividendEntity> dividendEntities = dividendRepository.findAllByCompanyId(company.getId());

        // 3. 결과 조합 후 반환
//        List<Dividend> dividends = new ArrayList<>();
//
//        for (DividendEntity d : dividendEntities) {
//            dividends.add(Dividend.builder()
//                    .date(d.getDate())
//                    .dividend(d.getDividend())
//                    .build());
//        }

        // stream.map() 을 이용한 방법. 같은 결과
        List<Dividend> dividends = dividendEntities.stream()
                .map(e -> new Dividend(e.getDate(), e.getDividend()))
                .collect(Collectors.toList());

        return new ScrapedResult(new Company(company.getTicker(), company.getName()), dividends);
    }
}
