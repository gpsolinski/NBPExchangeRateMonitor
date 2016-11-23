package pl.parser.nbp.service;

import pl.parser.nbp.domain.ExchangeRate;

import java.util.List;

/**
 * Created by Grzegorz on 23.11.2016.
 */
public interface ExchangeRateCalculator {

    double avgBuyingExchangeRate(List<ExchangeRate> exchangeRateTables);
    double stdDevSellingExchangeRate(List<ExchangeRate> exchangeRateTables);
}
