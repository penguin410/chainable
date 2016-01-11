package jp.ac.dendai.sie.ics.util.chainable;

import java.util.concurrent.TimeUnit;

/**
 * Created by keisuke on 2015/12/11.
 */
public abstract class ChainableConsumer<T> extends AbstractChainable<T, Void> {
    public abstract T take() throws InterruptedException;

    public abstract T poll();

    public abstract T poll(long timeout, TimeUnit unit) throws InterruptedException;
}
