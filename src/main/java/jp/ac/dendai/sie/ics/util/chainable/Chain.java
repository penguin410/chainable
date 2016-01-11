package jp.ac.dendai.sie.ics.util.chainable;

import java.util.concurrent.BlockingQueue;

/**
 * Created by keisuke on 2015/12/11.
 */
public abstract class Chain<T, U, R> extends AbstractChainable<T, R> {
    public abstract boolean isAlreadyChaind();

    public abstract Chain<T, U, R> queue(BlockingQueue<U> blockingQueue);

    public abstract Chain<T, U, R> chain();

    public abstract void unchain();

    abstract Chainable<T, U> getFrom();

    abstract Chainable<U, R> getTo();

    abstract void innerChain();
}
