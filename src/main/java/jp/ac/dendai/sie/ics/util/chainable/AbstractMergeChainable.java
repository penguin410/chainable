package jp.ac.dendai.sie.ics.util.chainable;

import java.util.concurrent.BlockingQueue;

/**
 * Created by keisuke on 2015/12/17.
 */
public abstract class AbstractMergeChainable<F, S, U> implements MergeChainable<F, S, U> {
    protected abstract void onChainFirstSource(BlockingQueue<F> src);

    protected abstract void onChainSecondSource(BlockingQueue<S> src);

    protected abstract void onChainDestination(BlockingQueue<U> dest);

    protected abstract void onUnchainSource(BlockingQueue<F> firstSrc, BlockingQueue<S> secondSrc);

    protected abstract void onUnchainDestination(BlockingQueue<U> dest);
}
