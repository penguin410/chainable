package jp.ac.dendai.sie.ics.util.chainable;

import lombok.Getter;
import lombok.NonNull;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * fromとtoが一対一の関係を表現するChain
 * Created by keisuke on 2015/12/17.
 */
public final class SingleChain<T, U, R> extends Chain<T, U, R> {
    @Getter
    private final AbstractChainable<T, U> from;
    @Getter
    private final AbstractChainable<U, R> to;
    private final BlockingQueue<U> queue;
    private final AtomicBoolean isAlreadyChainedLatch = new AtomicBoolean(false);


    public static <T, U, R> SingleChain<T, U, R> create(@NonNull AbstractChainable<T, U> from, @NonNull AbstractChainable<U, R> to) {
        return new SingleChain<>(from, to);
    }

    private SingleChain(@NonNull AbstractChainable<T, U> from, @NonNull AbstractChainable<U, R> to) {
        this(from, to, new LinkedBlockingQueue<>());
    }

    private SingleChain(@NonNull AbstractChainable<T, U> from, @NonNull AbstractChainable<U, R> to, @NonNull BlockingQueue<U> blockingQueue) {
        if (from instanceof Chain) {
            if (((Chain) from).isAlreadyChaind()) {
                throw new AlreadyChainedException("このChainは既に使用されています");
            }
        }
        if (to instanceof Chain) {
            if (((Chain) to).isAlreadyChaind()) {
                throw new AlreadyChainedException("このChainは既に使用されています");
            }
        }

        this.from = from;
        this.to = to;
        this.queue = blockingQueue;
    }

    @Override
    public boolean isAlreadyChaind() {
        return this.isAlreadyChainedLatch.get();
    }

    @Override
    public synchronized SingleChain<T, U, R> queue(@NonNull BlockingQueue<U> queue) {
        if (this.isAlreadyChaind()) {
            throw new AlreadyChainedException("このChainは既に使用されています");
        }

        this.isAlreadyChainedLatch.set(true);
        return new SingleChain<>(this.from, this.to, queue);
    }

    @Override
    public synchronized SingleChain<T, U, R> chain() {
        if (this.isAlreadyChaind()) {
            throw new AlreadyChainedException("このChainは既に使用されています");
        }

        if (this.from instanceof Chain) {
            ((SingleChain) this.from).innerChain();
        }
        if (this.to instanceof Chain) {
            ((Chain) this.to).innerChain();
        }

        this.from.onChainDestination(queue);
        this.to.onChainSource(queue);
        this.isAlreadyChainedLatch.set(true);
        return this;
    }

    protected synchronized void innerChain() {
        // TODO: 条件は正しいか?
        if (this.from instanceof Chain) {
            ((Chain) this.from).innerChain();
        }
        if (this.to instanceof Chain) {
            ((Chain) this.to).innerChain();
        }

        if (this.isAlreadyChaind()) {
            return;
        }

        this.from.onChainDestination(queue);
        this.to.onChainSource(queue);
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
        if (this.to instanceof Chain) {
            ((Chain) this.to).unchain();
        }

        this.from.onUnchainDestination(queue);
        this.to.onUnchainSource(queue);
    }

    @Override
    public synchronized void onChainSource(@NonNull BlockingQueue<T> src) {
        this.from.onChainSource(src);
    }

    @Override
    public synchronized void onChainDestination(@NonNull BlockingQueue<R> dest) {
        this.to.onChainDestination(dest);
    }

    @Override
    public synchronized void onUnchainSource(BlockingQueue<T> src) {
        this.from.onUnchainSource(src);
    }

    @Override
    public synchronized void onUnchainDestination(BlockingQueue<R> dest) {
        this.to.onUnchainDestination(dest);
    }
}