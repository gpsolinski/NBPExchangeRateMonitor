package pl.parser.nbp.domain;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by Grzegorz on 23.11.2016.
 */
@XmlRootElement(name = "pozycja")
public class ExchangeRate {

    String currencyCode;
    Double buyingRate;
    Double sellingRate;

    @XmlElement(name = "kod_waluty")
    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    @XmlElement(name = "kurs_kupna")
    @XmlJavaTypeAdapter(DoubleAdapter.class)
    public Double getBuyingRate() {
        return buyingRate;
    }

    public void setBuyingRate(Double buyingRate) {
        this.buyingRate = buyingRate;
    }

    @XmlElement(name = "kurs_sprzedazy")
    @XmlJavaTypeAdapter(DoubleAdapter.class)
    public Double getSellingRate() {
        return sellingRate;
    }

    public void setSellingRate(Double sellingRate) {
        this.sellingRate = sellingRate;
    }
}
