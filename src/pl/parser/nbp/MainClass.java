package pl.parser.nbp;

import pl.parser.nbp.domain.ExchangeRate;
import pl.parser.nbp.domain.NbpConnectionException;
import pl.parser.nbp.service.ExchangeRateCalculator;
import pl.parser.nbp.service.NbpCurrencyMonitor;
import pl.parser.nbp.service.impl.ExchangeRateCalculatorImpl;
import pl.parser.nbp.service.impl.NbpCurrencyMonitorImpl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class MainClass {

    public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("Insufficient number of arguments");
            printUsage();
            return;
        }

        if (args.length > 3) {
            System.out.println("Unrecognised argument: " + args[3]);
            printUsage();
            return;
        }

        String currency = args[0];
        String dateFromString = args[1];
        String dateToString = args[2];

        NbpCurrencyMonitor nbpCurrencyMonitor = new NbpCurrencyMonitorImpl();
        ExchangeRateCalculator exchangeRateCalculator = new ExchangeRateCalculatorImpl();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate dateFrom = null;
        LocalDate dateTo;
        List<ExchangeRate> exchangeRates;
        try {
            dateFrom = LocalDate.parse(dateFromString, dateTimeFormatter);
            dateTo = LocalDate.parse(dateToString, dateTimeFormatter);
            exchangeRates = nbpCurrencyMonitor.getExchangeRates(currency, dateFrom, dateTo);
        } catch (DateTimeParseException e) {
            System.out.print("Unrecognized date format: ");
            if (dateFrom == null) {
                System.out.println(dateFromString);
            } else {
                System.out.println(dateToString);
            }
            printUsage();
            return;
        } catch (NbpConnectionException e) {
            System.out.println(e.getMessage());
            return;
        }

        double avgBuyingExchangeRate = exchangeRateCalculator.avgBuyingExchangeRate(exchangeRates);
        double stdDevSellingExchangeRate = exchangeRateCalculator.stdDevSellingExchangeRate(exchangeRates);

        System.out.format("%.4f\n%.4f", avgBuyingExchangeRate, stdDevSellingExchangeRate);
    }

    private static void printUsage() {
        System.out.println("Usage: java " + MainClass.class.getName() + " <currency_code>(USD|EUR|CHF|GBP) " +
        "<from_date>(YYYY-MM-DD) <to_date>(YYYY-MM-DD)");
    }
}
