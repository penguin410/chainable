package jp.ac.dendai.sie.ics.util.chainable;

import lombok.*;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * toを複数持状況を表現するChain
 * Created by keisuke on 2015/12/17.
 */
public final class MultipleChain<T, U, R> extends Chain<T, U, R> {
    @Getter
    private AbstractChainable<T, U> from;
    private AtomicBoolean isAlreadyChainedLatch = new AtomicBoolean(false);
    @Getter(AccessLevel.PRIVATE)
    private final List<ChainableAndBlockingQueuePair<U, ?>> toAndQueuePairs;

    public static <T, U> MultipleChain<T, U, ?> create(@NonNull AbstractChainable<T, U> from, @NonNull List<AbstractChainable<U, ?>> tos) {
        return new MultipleChain<>(from, tos);
    }

    public MultipleChain(@NonNull AbstractChainable<T, U> from, @NonNull List<AbstractChainable<U, ?>> tos) {
        if (from instanceof Chain) {
            if (((Chain) from).isAlreadyChaind()) {
                throw new AlreadyChainedException("このChainは既に使用されています");
            }
        }
        if (tos.stream().filter(to -> to instanceof Chain).anyMatch(to -> ((Chain) to).isAlreadyChaind())) {
            throw new AlreadyChainedException("このChainは既に使用されています");
        }

        this.from = from;
        this.toAndQueuePairs = tos.stream().map(to -> new ChainableAndBlockingQueuePair<>(to, new LinkedBlockingQueue<>())).collect(Collectors.toList());
    }

    @Override
    public boolean isAlreadyChaind() {
        return this.isAlreadyChainedLatch.get();
    }

    @Override
    public MultipleChain<T, U, R> queue(BlockingQueue<U> blockingQueue) {
        throw new UnsupportedOperationException("未実装");
    }

    @Override
    public synchronized MultipleChain<T, U, R> chain() {
        if (this.isAlreadyChaind()) {
            throw new AlreadyChainedException("このChainは既に使用されています");
        }

        if (this.from instanceof Chain) {
            ((Chain) this.from).innerChain();
        }
        this.toAndQueuePairs.stream().filter(pair -> pair.getTo() instanceof Chain).forEach(pair -> ((Chain) pair.getTo()).innerChain());

        this.toAndQueuePairs.forEach(pair -> this.from.onChainDestination(pair.getQueue()));
        this.toAndQueuePairs.forEach(pair -> pair.getTo().onChainSource(pair.getQueue()));
        this.isAlreadyChainedLatch.set(true);
        return this;
    }

    synchronized void innerChain() {
        if (this.from instanceof SingleChain) {
            ((SingleChain) this.from).innerChain();
        } else if (this.from instanceof MultipleChain) {
            ((MultipleChain) this.from).innerChain();
        }
        this.toAndQueuePairs.stream().filter(pair -> pair.getTo() instanceof Chain).forEach(pair -> ((Chain) pair.getTo()).innerChain());

        if (this.isAlreadyChaind()) {
            return;
        }

        this.toAndQueuePairs.forEach(pair -> this.from.onChainDestination(pair.getQueue()));
        this.toAndQueuePairs.forEach(pair -> pair.getTo().onChainSource(pair.getQueue()));
        this.isAlreadyChainedLatch.set(true);
    }

    @Override
    public synchronized void unchain() {
        if (!this.isAlreadyChaind()) {
            throw new AlreadyChainedException("このChainはまだchainされていません");
        }

        if (this.from instanceof Chain) {
            ((Chain) this.from).unchain();
        }
        this.toAndQueuePairs.stream().filter(pair -> pair.getTo() instanceof Chain).forEach(pair -> ((Chain) pair.getTo()).unchain());

        this.toAndQueuePairs.forEach(pair -> this.from.onUnchainDestination(pair.getQueue()));
        this.toAndQueuePairs.forEach(pair -> pair.getTo().onUnchainSource(pair.getQueue()));
    }

    @Override
    public Chainable<U, R> getTo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public synchronized void onChainSource(@NonNull BlockingQueue<T> src) {
        this.from.onChainSource(src);
    }

    @Override
    public synchronized void onChainDestination(@NonNull BlockingQueue<R> dest) {
        throw new UnsupportedOperationException("未実装");
    }

    @Override
    public synchronized void onUnchainSource(BlockingQueue<T> src) {
        this.from.onUnchainSource(src);
    }

    @Override
    public synchronized void onUnchainDestination(BlockingQueue<R> dest) {
        throw new UnsupportedOperationException("未実装");
    }
}


