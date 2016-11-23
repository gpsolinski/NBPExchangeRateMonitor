package pl.parser.nbp.domain;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.LocalDate;
import java.util.List;

/**
 * Created by Grzegorz on 23.11.2016.
 */
@XmlRootElement(name = "tabela_kursow")
public class ExchangeRateTable {

    private LocalDate publicationDate;
    private List<ExchangeRate> exchangeRates;

    @XmlElement(name = "data_publikacji")
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }

    @XmlElement(name = "pozycja")
    public List<ExchangeRate> getExchangeRates() {
        return exchangeRates;
    }

    public void setExchangeRates(List<ExchangeRate> exchangeRates) {
        this.exchangeRates = exchangeRates;
    }
}
