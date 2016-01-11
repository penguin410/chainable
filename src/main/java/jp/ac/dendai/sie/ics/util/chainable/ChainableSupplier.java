package jp.ac.dendai.sie.ics.util.chainable;

import java.util.function.Supplier;

/**
 * Created by keisuke on 2015/12/11.
 */
public abstract class ChainableSupplier<R> extends AbstractChainable<Void, R> {
    public abstract boolean offer(R r);

    public abstract boolean offer(Supplier<R> supplier);
}
