package jp.ac.dendai.sie.ics.util.chainable;

import lombok.NonNull;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Created by keisuke on 2015/12/11.
 */
public abstract class ChainableFunctionBase<T, R> extends ChainableFunction<T, R> {
    private BlockingQueue<T> source;
    private final List<BlockingQueue<R>> destinations = new CopyOnWriteArrayList<>();

    @Override
    protected synchronized void onChainSource(@NonNull BlockingQueue<T> src) {
        if (this.source != null) {
            throw new AlreadyChainedException("this source queue is already exists.");
        }
        this.source = src;
    }

    @Override
    protected synchronized void onChainDestination(@NonNull BlockingQueue<R> dest) {
        if (this.destinations.contains(dest)) {
            throw new AlreadyChainedException("this destination queue is already exists.");
        }
        this.destinations.add(dest);
    }

    @Override
    protected synchronized void onUnchainSource(@NonNull BlockingQueue<T> src) {
        this.source = null;
    }

    @Override
    protected synchronized void onUnchainDestination(@NonNull BlockingQueue<R> dest) {
        this.destinations.remove(dest);
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

    @Override
    public boolean offer(R r) {
        return this.destinations.stream().allMatch(dest -> dest.offer(r));
    }

    @Override
    public boolean offer(Supplier<R> supplier) {
        return this.destinations.stream().allMatch(dest -> dest.offer(supplier.get()));
    }

    @Deprecated
    public boolean isSrcChained() {
        return this.source != null;
    }

    @Deprecated
    public boolean isDestChained() {
        return this.destinations.size() > 0;
    }
}
