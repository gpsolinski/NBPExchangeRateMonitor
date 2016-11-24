package pl.parser.nbp;

import pl.parser.nbp.domain.Currency;
import pl.parser.nbp.domain.ExchangeRate;
import pl.parser.nbp.domain.NbpConnectionException;
import pl.parser.nbp.service.ExchangeRateCalculator;
import pl.parser.nbp.service.NbpCurrencyMonitor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MainClass {

    public static void main(String[] args) {

        // validation of the number of passed arguments
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

        // validation of the entered currency code
        List<Currency> supportedCurrency = new ArrayList<>();
        supportedCurrency.addAll(Arrays.asList(Currency.values()));
        if (!supportedCurrency.stream()
                .map(Currency::toString)
                .collect(Collectors.toSet())
                .contains(currency)) {

            System.out.println("Unsupported currency: " + currency);
            System.out.println("Supported currencies are:");
            supportedCurrency.stream().forEach(curr -> System.out.println(curr));
            return;

        }
        String dateFromString = args[1];
        String dateToString = args[2];

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate dateFrom = null;
        LocalDate dateTo;
        List<ExchangeRate> exchangeRates;
        try {
            dateFrom = LocalDate.parse(dateFromString, dateTimeFormatter);
            // walidacja wprowadzonej daty początkowej
            if (dateFrom.isBefore(LocalDate.of(2002, 1, 2))) {                 // NBP provides data since 2002-01-02
                System.out.println("Unsupported date: " + dateFromString);
                System.out.println("The earliest supported date is 2002-01-02");
                return;
            }
            dateTo = LocalDate.parse(dateToString, dateTimeFormatter);
            exchangeRates = NbpCurrencyMonitor.getExchangeRates(currency, dateFrom, dateTo);
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

        double avgBuyingExchangeRate = ExchangeRateCalculator.avgBuyingExchangeRate(exchangeRates);
        double stdDevSellingExchangeRate = ExchangeRateCalculator.stdDevSellingExchangeRate(exchangeRates);

        System.out.format("%.4f\n%.4f", avgBuyingExchangeRate, stdDevSellingExchangeRate);
    }

    private static void printUsage() {
        System.out.println("Usage: java " + MainClass.class.getName() + " <currency_code>(USD|EUR|CHF|GBP) " +
        "<from_date>(YYYY-MM-DD) <to_date>(YYYY-MM-DD)");
    }
}
