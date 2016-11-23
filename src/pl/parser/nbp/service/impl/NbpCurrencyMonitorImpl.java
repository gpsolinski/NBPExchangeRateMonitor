package pl.parser.nbp.service.impl;

import org.xml.sax.*;
import pl.parser.nbp.domain.ExchangeRate;
import pl.parser.nbp.domain.ExchangeRateTable;
import pl.parser.nbp.domain.NbpConnectionException;
import pl.parser.nbp.service.NbpCurrencyMonitor;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Grzegorz on 23.11.2016.
 */
public class NbpCurrencyMonitorImpl implements NbpCurrencyMonitor {

    @Override
    public List<ExchangeRate> getExchangeRates(String currency, LocalDate dateFrom, LocalDate dateTo)
            throws NbpConnectionException {

        int currentYear = LocalDate.now().getYear();
        int yearTo = dateTo.getYear() >= currentYear ? currentYear : dateTo.getYear();
        List<String> fileNames = getFileNamesForYears(dateFrom.getYear(), yearTo);

        List<ExchangeRateTable> exchangeRateTables = new ArrayList<>(fileNames.size());
        for (String fileName : filterRelevantFileNames(fileNames, dateFrom, dateTo)) {

            String nbpExchangeRatesFileUrlString = NBP_BASE_URL + fileName + ".xml";
            try {
                URL nbpExchangeRatesFileUrl = new URL(nbpExchangeRatesFileUrlString);
                exchangeRateTables.add(JAXB.unmarshal(nbpExchangeRatesFileUrl, ExchangeRateTable.class));
            } catch (MalformedURLException e) {
                throw new NbpConnectionException("Malformed URL: " + nbpExchangeRatesFileUrlString, e);
            }
        }

        return exchangeRateTables.stream()
                .filter(exchangeRateTable -> !exchangeRateTable.getPublicationDate().isBefore(dateFrom) &&
                                !exchangeRateTable.getPublicationDate().isAfter(dateTo)
                )
                .flatMap(exchangeRateTable -> exchangeRateTable.getExchangeRates().stream())
                .filter(exchangeRate -> exchangeRate.getCurrencyCode().equals(currency))
                .collect(Collectors.toList());
    }

    private static List<String> getFileNamesForYears(int yearFrom, int yearTo) throws NbpConnectionException {
        List<String> fileNames = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        for (int year : IntStream.rangeClosed(yearFrom, yearTo).toArray()) {

            String nbpExchangeRatesFileUrl = NBP_BASE_URL + (year == currentYear ? "dir.txt" : "dir" + year + ".txt");
            InputStream is = null;
            try {
                is = new URL(nbpExchangeRatesFileUrl).openConnection().getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                fileNames.addAll(reader.lines().collect(Collectors.toList()));

            } catch (MalformedURLException e) {
                throw new NbpConnectionException("Malformed URL: " + nbpExchangeRatesFileUrl, e);
            } catch (IOException e) {
                throw new NbpConnectionException("Unable to connect with URL: " + nbpExchangeRatesFileUrl, e);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    throw new NbpConnectionException("Unable to close stream from URL: " + nbpExchangeRatesFileUrl, e);
                }
            }
        }

        return fileNames;
    }

    private static List<String> filterRelevantFileNames(List<String> fileNames, LocalDate dateFrom, LocalDate dateTo) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return fileNames.stream().filter(
                file -> {
                    try {
                        LocalDate dateOfFile = LocalDate.parse("20" + file.substring(5), dateFormatter);
                        return file.charAt(0) == 'c' && !dateOfFile.isBefore(dateFrom) && !dateOfFile.isAfter(dateTo);
                    } catch (DateTimeParseException e) {
                        return false;
                    }
                }
        ).collect(Collectors.toList());
    }
}
