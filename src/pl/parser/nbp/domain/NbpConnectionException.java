package pl.parser.nbp.domain;

import java.io.IOException;

/**
 * Exception used to indicate problems with connecting to the NBP site.
 *
 * @author Grzegorz Soliński
 */
public class NbpConnectionException extends IOException {

    public NbpConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
