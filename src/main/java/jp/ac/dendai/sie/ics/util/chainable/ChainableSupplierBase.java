package jp.ac.dendai.sie.ics.util.chainable;

import lombok.NonNull;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * Created by keisuke on 2015/12/11.
 */
public abstract class ChainableSupplierBase<R> extends ChainableSupplier<R> {
    private final List<BlockingQueue<R>> destinations = new CopyOnWriteArrayList<>();

    @Override
    protected void onChainSource(BlockingQueue<Void> src) {
        throw new AlreadyChainedException("VoidのBlockingQueueっておま...");
    }

    @Override
    protected synchronized void onChainDestination(@NonNull BlockingQueue<R> dest) {
        if (this.destinations.contains(dest)) {
            throw new AlreadyChainedException("this destination queue is already exists.");
        }
        this.destinations.add(dest);
    }

    @Override
    protected final void onUnchainSource(BlockingQueue<Void> src) {
        throw new AlreadyChainedException("VoidのBlockingQueueっておま...");
    }

    @Override
    protected synchronized void onUnchainDestination(@NonNull BlockingQueue<R> dest) {
        this.destinations.remove(dest);
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
    public boolean isDestChained() {
        return this.destinations.size() > 0;
    }
}
