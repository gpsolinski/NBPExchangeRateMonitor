package pl.parser.nbp.domain;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Grzegorz on 23.11.2016.
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
