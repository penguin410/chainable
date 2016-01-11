package jp.ac.dendai.sie.ics.util.chainable;

import lombok.NonNull;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by keisuke on 2015/12/11.
 */

public abstract class ChainableConsumerBase<T> extends ChainableConsumer<T> {
    private BlockingQueue<T> source;

    @Override
    protected synchronized void onChainSource(@NonNull BlockingQueue<T> src) {
        if (this.source != null) {
            throw new AlreadyChainedException("this source queue is already exists.");
        }
        this.source = src;
    }

    @Override
    protected final void onChainDestination(@NonNull BlockingQueue<Void> dest) {
        throw new AlreadyChainedException("VoidのBlockingQueueっておま...");
    }

    @Override
    protected synchronized void onUnchainSource(BlockingQueue<T> src) {
        this.source = null;
    }

    @Override
    protected final void onUnchainDestination(BlockingQueue<Void> dest) {
        throw new AlreadyChainedException("VoidのBlockingQueueっておま...");
    }

    @Override
    public T take() throws InterruptedException {
        if (this.source == null) {
            throw new IllegalStateException("SourceQueueが null を参照しています。既にunchainされているか、不正な操作が行われた可能性があります。");
        }
        return this.source.take();
    }

    @Override
    public T poll() {
        if (this.source == null) {
            throw new IllegalStateException("SourceQueueが null を参照しています。既にunchainされているか、不正な操作が行われた可能性があります。");
        }
        return this.source.poll();
    }

    @Override
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        if (this.source == null) {
            throw new IllegalStateException("SourceQueueが null を参照しています。既にunchainされているか、不正な操作が行われた可能性があります。");
        }
        return this.source.poll(timeout, unit);
    }

    @Deprecated
    public boolean isSrcChained() {
        return this.source != null;
    }
}
