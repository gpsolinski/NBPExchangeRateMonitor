package pl.parser.nbp.service;

import pl.parser.nbp.domain.ExchangeRate;
import pl.parser.nbp.domain.NbpConnectionException;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by Grzegorz on 23.11.2016.
 */
public interface NbpCurrencyMonitor {
    String NBP_BASE_URL = "http://www.nbp.pl/kursy/xml/";

    List<ExchangeRate> getExchangeRates(String currency, LocalDate dateFrom, LocalDate dateTo)
            throws NbpConnectionException;
}
