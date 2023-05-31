package com.amerikano.dividendsproject.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Data
public class ScrapedResult {

    private Company company;

    private List<Dividend> dividends;

    public ScrapedResult() { this.dividends = new ArrayList<>(); }
}
