package pl.parser.nbp.domain;

import java.io.IOException;

/**
 * Created by Grzegorz on 23.11.2016.
 */
public class NbpConnectionException extends IOException {

    public NbpConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
