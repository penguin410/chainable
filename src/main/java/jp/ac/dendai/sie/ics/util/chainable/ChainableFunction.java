package jp.ac.dendai.sie.ics.util.chainable;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Created by keisuke on 2015/12/11.
 */
public abstract class ChainableFunction<T, R> extends AbstractChainable<T, R> {
    public abstract boolean offer(R r);

    public abstract boolean offer(Supplier<R> supplier);

    public abstract T take() throws InterruptedException;

    public abstract T poll();

    public abstract T poll(long timeout, TimeUnit unit) throws InterruptedException;
}
