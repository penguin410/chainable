package jp.ac.dendai.sie.ics.util.chainable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by keisuke on 2015/12/11.
 */
@Value
public final class MergeChain<A, B, C, D, E> extends AbstractMergeChainable<A, C, E> {
    @Getter(AccessLevel.PACKAGE)
    AbstractChainable<A, B> from1;
    @Getter(AccessLevel.PACKAGE)
    AbstractChainable<C, D> from2;
    @Getter(AccessLevel.PACKAGE)
    AbstractMergeChainable<B, D, E> to;
    @Getter(AccessLevel.PRIVATE)
    BlockingQueue<B> queue1;
    @Getter(AccessLevel.PRIVATE)
    BlockingQueue<D> queue2;
    @Getter(AccessLevel.PACKAGE)
    AtomicBoolean isAlreadyChainedLatch = new AtomicBoolean(false);

    public static <A, B, C, D, E> MergeChain<A, B, C, D, E> create(@NonNull AbstractChainable<A, B> from1, @NonNull AbstractChainable<C, D> from2, @NonNull AbstractMergeChainable<B, D, E> to) {
        return new MergeChain<>(from1, from2, to);
    }

    private MergeChain(@NonNull AbstractChainable<A, B> from1, @NonNull AbstractChainable<C, D> from2, @NonNull AbstractMergeChainable<B, D, E> to) {
        this(from1, from2, to, new LinkedBlockingQueue<>(), new LinkedBlockingQueue<>());
    }

    private MergeChain(@NonNull AbstractChainable<A, B> from1, @NonNull AbstractChainable<C, D> from2, @NonNull AbstractMergeChainable<B, D, E> to, @NonNull BlockingQueue<B> firstQueue, @NonNull BlockingQueue<D> secondQueue) {
        if (from1 instanceof Chain) {
            if (((Chain) from1).isAlreadyChaind()) {
                throw new AlreadyChainedException("このChainは既に使用されています");
            }
        }
        if (from2 instanceof Chain) {
            if (((Chain) from2).isAlreadyChaind()) {
                throw new AlreadyChainedException("このChainは既に使用されています");
            }
        }
        if (to instanceof MergeChain) {
            if (((MergeChain) to).getIsAlreadyChainedLatch().get()) {
                throw new AlreadyChainedException("このChainは既に使用されています");
            }
        }

        this.from1 = from1;
        this.from2 = from2;
        this.to = to;
        this.queue1 = firstQueue;
        this.queue2 = secondQueue;
    }

    public synchronized MergeChain<A, B, C, D, E> queue(@NonNull BlockingQueue<B> firstQueue, @NonNull BlockingQueue<D> secondQueue) {
        this.getIsAlreadyChainedLatch().set(true);
        return new MergeChain<>(this.from1, this.from2, this.to, firstQueue, secondQueue);
    }

    public synchronized MergeChain<A, B, C, D, E> chain() {
        if (this.getIsAlreadyChainedLatch().get()) {
            throw new AlreadyChainedException("このMergeChainは既に使用されています");
        }

        if (this.from1 instanceof SingleChain) {
            ((SingleChain) this.from1).innerChain();
        } else if (this.from1 instanceof MultipleChain) {
            ((MultipleChain) this.from1).innerChain();
        }
        if (this.from2 instanceof SingleChain) {
            ((SingleChain) this.from2).innerChain();
        } else if (this.from2 instanceof MultipleChain) {
            ((MultipleChain) this.from2).innerChain();
        }

        if (this.to instanceof MergeChain) {
            throw new UnsupportedOperationException("未実装");
        }

        this.from1.onChainDestination(queue1);
        this.to.onChainFirstSource(queue1);
        this.from2.onChainDestination(queue2);
        this.to.onChainSecondSource(queue2);
        this.getIsAlreadyChainedLatch().set(true);
        return this;
    }

    public synchronized void unchain() {
        if (this.from1 instanceof Chain) {
            ((Chain) this.from1).unchain();
        }
        if (this.from2 instanceof Chain) {
            ((Chain) this.from2).unchain();
        }
        if (this.to instanceof MergeChain) {
            throw new UnsupportedOperationException("未実装");
        }

        this.from1.onUnchainDestination(this.queue1);
        this.from2.onUnchainDestination(this.queue2);
        this.to.onUnchainSource(this.queue1, this.queue2);
    }

    @Override
    public synchronized void onChainFirstSource(BlockingQueue<A> src) {
        this.from1.onChainSource(src);
    }

    @Override
    public synchronized void onChainSecondSource(BlockingQueue<C> src) {
        this.from2.onChainSource(src);
    }

    @Override
    public synchronized void onChainDestination(BlockingQueue<E> dest) {
        this.to.onChainDestination(dest);
    }

    @Override
    public synchronized void onUnchainSource(BlockingQueue<A> firstSrc, BlockingQueue<C> secondSrc) {
        this.from1.onUnchainSource(firstSrc);
        this.from2.onUnchainSource(secondSrc);
    }

    @Override
    public synchronized void onUnchainDestination(BlockingQueue<E> dest) {
        this.to.onUnchainDestination(dest);
    }
}
