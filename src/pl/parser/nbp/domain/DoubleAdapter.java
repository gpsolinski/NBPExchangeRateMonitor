package pl.parser.nbp.domain;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Used to enable parsing double values from XML with a coma as a decimal separator.
 *
 * @author Grzegorz Soliński
 */
public class DoubleAdapter extends XmlAdapter<String, Double> {

    @Override
    public Double unmarshal(String v) throws Exception {
        return NumberFormat.getInstance(Locale.getDefault()).parse(v).doubleValue();
    }

    @Override
    public String marshal(Double v) throws Exception {
        return NumberFormat.getInstance(Locale.getDefault()).format(v);
    }
}
