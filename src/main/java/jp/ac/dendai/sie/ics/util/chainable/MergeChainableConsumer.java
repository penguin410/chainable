package jp.ac.dendai.sie.ics.util.chainable;

import java.util.concurrent.TimeUnit;

/**
 * Created by keisuke on 2015/12/11.
 */
public abstract class MergeChainableConsumer<F, S> extends AbstractMergeChainable<F, S, Void> {
    public abstract F takeFirst() throws InterruptedException;

    public abstract S takeSecond() throws InterruptedException;

    public abstract F pollFirst();

    public abstract S pollSecond();

    public abstract F pollFirst(long timeout, TimeUnit unit) throws InterruptedException;

    public abstract S pollSecond(long timeout, TimeUnit unit) throws InterruptedException;
}
