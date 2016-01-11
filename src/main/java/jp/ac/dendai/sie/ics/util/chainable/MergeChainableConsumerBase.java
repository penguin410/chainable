package jp.ac.dendai.sie.ics.util.chainable;

import lombok.NonNull;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by keisuke on 2015/12/11.
 */
public abstract class MergeChainableConsumerBase<F, S> extends MergeChainableConsumer<F, S> {
    private BlockingQueue<F> firstSource;
    private BlockingQueue<S> secondSource;

    @Override
    protected synchronized void onChainFirstSource(@NonNull BlockingQueue<F> src) {
        if (this.firstSource != null) {
            throw new AlreadyChainedException("this source queue is already exists.");
        }
        this.firstSource = src;
    }

    @Override
    protected synchronized void onChainSecondSource(@NonNull BlockingQueue<S> src) {
        if (this.secondSource != null) {
            throw new AlreadyChainedException("this source queue is already exists.");
        }
        this.secondSource = src;
    }

    @Override
    protected final void onChainDestination(@NonNull BlockingQueue<Void> dest) {
        throw new AlreadyChainedException("VoidのBlockingQueueっておま...");
    }

    @Override
    protected synchronized void onUnchainSource(BlockingQueue<F> firstSource, BlockingQueue<S> secondSource) {
        this.firstSource = null;
        this.secondSource = null;
    }

    @Override
    protected final void onUnchainDestination(BlockingQueue<Void> dest) {
        throw new AlreadyChainedException("VoidのBlockingQueueっておま...");
    }

    @Override
    public F takeFirst() throws InterruptedException {
        if (this.firstSource == null) {
            throw new IllegalStateException("FirstSourceQueueが null を参照しています。既にunchainされているか、不正な操作が行われた可能性があります。");
        }
        return this.firstSource.take();
    }

    @Override
    public S takeSecond() throws InterruptedException {
        if (this.secondSource == null) {
            throw new IllegalStateException("SecondSourceQueueが null を参照しています。既にunchainされているか、不正な操作が行われた可能性があります。");
        }
        return this.secondSource.take();
    }

    @Override
    public F pollFirst() {
        if (this.firstSource == null) {
            throw new IllegalStateException("FirstSourceQueueが null を参照しています。既にunchainされているか、不正な操作が行われた可能性があります。");
        }
        return this.firstSource.poll();
    }

    @Override
    public F pollFirst(long timeout, TimeUnit unit) throws InterruptedException {
        if (this.firstSource == null) {
            throw new IllegalStateException("FirstSourceQueueが null を参照しています。既にunchainされているか、不正な操作が行われた可能性があります。");
        }
        return this.firstSource.poll(timeout, unit);
    }

    @Override
    public S pollSecond() {
        if (this.secondSource == null) {
            throw new IllegalStateException("SecondSourceQueueが null を参照しています。既にunchainされているか、不正な操作が行われた可能性があります。");
        }
        return this.secondSource.poll();
    }

    @Override
    public S pollSecond(long timeout, TimeUnit unit) throws InterruptedException {
        if (this.secondSource == null) {
            throw new IllegalStateException("SecondSourceQueueが null を参照しています。既にunchainされているか、不正な操作が行われた可能性があります。");
        }
        return this.secondSource.poll(timeout, unit);
    }


    @Deprecated
    public boolean isSrcChained() {
        return this.firstSource != null && this.secondSource != null;
    }
}
