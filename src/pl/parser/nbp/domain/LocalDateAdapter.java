package pl.parser.nbp.domain;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;

/**
 * Used to enable the usage of java.time.LocalDate (instead of java.util.Date)type in the JAXB
 * template classes.
 *
 * @author Grzegorz Soliński
 */
public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

    public LocalDate unmarshal(String v) throws Exception {
        return LocalDate.parse(v);
    }

    public String marshal(LocalDate v) throws Exception {
        return v.toString();
    }
}
