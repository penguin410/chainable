package jp.ac.dendai.sie.ics.util.chainable;

import lombok.NoArgsConstructor;

/**
 * Created by keisuke on 2015/12/09.
 */
@NoArgsConstructor
public class AlreadyChainedException extends RuntimeException {
    public AlreadyChainedException(String message) {
        super(message);
    }
}
