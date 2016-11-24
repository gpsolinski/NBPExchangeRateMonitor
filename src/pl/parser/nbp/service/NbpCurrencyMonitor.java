package pl.parser.nbp.service;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import pl.parser.nbp.domain.ExchangeRate;
import pl.parser.nbp.domain.ExchangeRateTable;
import pl.parser.nbp.domain.NbpConnectionException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
 * Used for downloading exchange rates from NBP (Narodowy Bank Polski) website.
 *
 * @author Grzegorz Soliński
 */
public class NbpCurrencyMonitor {

    private static final String NBP_BASE_URL = "http://www.nbp.pl/kursy/xml/";

    /**
     * Downloads from NBP and assembles a list of ExchangeRate objects, containing exchange rates
     * for a given currency between given dates.
     *
     * @param currency currency code, for which the exchange rates will be found
     * @param dateFrom date telling since when the exchange rates will be included (inclusive)
     * @param dateTo   date telling until when the exchange rates will be included (inclusive)
     *
     * @return List of ExchangeRate objects, contatining exchange rates for a given currency between
     * given dates
     *
     * @throws pl.parser.nbp.domain.NbpConnectionException to indicate problems with connecting
     * to NBP site
     */
    public static List<ExchangeRate> getExchangeRates(String currency, LocalDate dateFrom,
                                                      LocalDate dateTo) throws NbpConnectionException {

        List<ExchangeRateTable> exchangeRateTables = downloadExchangeRates(dateFrom, dateTo);
        return exchangeRateTables.stream()
                .filter(exchangeRateTable -> !exchangeRateTable.getPublicationDate().isBefore(dateFrom) &&
                                !exchangeRateTable.getPublicationDate().isAfter(dateTo)
                )
                .flatMap(exchangeRateTable -> exchangeRateTable.getExchangeRates().stream())
                .filter(exchangeRate -> exchangeRate.getCurrencyCode().equals(currency))
                .collect(Collectors.toList());
    }

    private static List<ExchangeRateTable> downloadExchangeRates(LocalDate dateFrom, LocalDate dateTo)
            throws NbpConnectionException {

        JAXBContext jaxbContext;
        Unmarshaller unmarshaller = null;

        try {
            jaxbContext = JAXBContext.newInstance(ExchangeRateTable.class);
            unmarshaller = jaxbContext.createUnmarshaller();
        } catch (JAXBException e) {
            System.out.println("Error occured while initializing JAXB");
        }


        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            spf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            spf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        } catch (ParserConfigurationException e) {
            System.out.println("Error occured while setting features XML parser.");
            e.printStackTrace();
        } catch (SAXNotRecognizedException e) {
            System.out.println("Error occured while setting features for XML parser.");
            e.printStackTrace();
        } catch (SAXNotSupportedException e) {
            System.out.println("Error occured while setting features for XML parser.");
            e.printStackTrace();
        }

        int currentYear = LocalDate.now().getYear();
        int yearTo = dateTo.getYear() >= currentYear ? currentYear : dateTo.getYear();
        List<String> fileNames = getFileNamesForYears(dateFrom.getYear(), yearTo);

        List<ExchangeRateTable> exchangeRateTables = new ArrayList<>(fileNames.size());
        for (String fileName : filterRelevantFileNames(fileNames, dateFrom, dateTo)) {

            String nbpExchangeRatesFileUrlString = NBP_BASE_URL + fileName + ".xml";
            try {
                URL nbpExchangeRatesFileUrl = new URL(nbpExchangeRatesFileUrlString);
                InputSource inputSource = new InputSource(nbpExchangeRatesFileUrl
                        .openConnection().getInputStream());
                Source xmlSource = new SAXSource(spf.newSAXParser().getXMLReader(), inputSource);
                exchangeRateTables.add((ExchangeRateTable) unmarshaller.unmarshal(xmlSource));
            } catch (MalformedURLException e) {
                throw new NbpConnectionException("Malformed URL: " +
                        nbpExchangeRatesFileUrlString, e);
            } catch (JAXBException e) {
                System.out.println("Error occured while unmarshalling XML.");
                e.printStackTrace();
            } catch (SAXException e) {
                System.out.println("Error occured while parsing XML.");
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                System.out.println("Error occured while creating XML parser.");
                e.printStackTrace();
            } catch (IOException e) {
                throw new NbpConnectionException("Unable to connect with URL: " +
                        nbpExchangeRatesFileUrlString, e);
            }
        }
        return exchangeRateTables;
    }

    private static List<String> getFileNamesForYears(int yearFrom, int yearTo) throws NbpConnectionException {
        List<String> fileNames = new ArrayList<>();
        int currentYear = LocalDate.now().getYear();
        for (int year : IntStream.rangeClosed(yearFrom, yearTo).toArray()) {

            String nbpExchangeRatesFileUrl = NBP_BASE_URL + (year == currentYear ? "dir.txt" :
                    "dir" + year + ".txt");
            InputStream is = null;
            try {
                is = new URL(nbpExchangeRatesFileUrl).openConnection().getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                fileNames.addAll(reader.lines().collect(Collectors.toList()));

            } catch (MalformedURLException e) {
                throw new NbpConnectionException("Malformed URL: " + nbpExchangeRatesFileUrl, e);
            } catch (IOException e) {
                throw new NbpConnectionException("Unable to connect with URL: " +
                        nbpExchangeRatesFileUrl, e);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    throw new NbpConnectionException("Unable to close stream from URL: " +
                            nbpExchangeRatesFileUrl, e);
                }
            }
        }

        return fileNames;
    }

    private static List<String> filterRelevantFileNames(List<String> fileNames, LocalDate dateFrom,
                                                        LocalDate dateTo) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return fileNames.stream().filter(
                file -> {
                    try {
                        LocalDate dateOfFile = LocalDate.parse("20" + file.substring(5),
                                dateFormatter);
                        return file.charAt(0) == 'c'
                                && !dateOfFile.isBefore(dateFrom) && !dateOfFile.isAfter(dateTo);
                    } catch (DateTimeParseException e) {
                        return false;
                    }
                }
        ).collect(Collectors.toList());
    }
}
