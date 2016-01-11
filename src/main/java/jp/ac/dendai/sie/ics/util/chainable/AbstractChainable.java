package jp.ac.dendai.sie.ics.util.chainable;

import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by keisuke on 2015/12/17.
 */
public abstract class AbstractChainable<T, U> implements Chainable<T, U> {
    protected abstract void onChainSource(BlockingQueue<T> src);

    protected abstract void onChainDestination(BlockingQueue<U> dest);

    protected abstract void onUnchainSource(BlockingQueue<T> src);

    protected abstract void onUnchainDestination(BlockingQueue<U> dest);

    @Override
    public <R> Chain<T, U, R> to(@NonNull AbstractChainable<U, R> consumer) {
        return SingleChain.create(this, consumer);
    }

    @Override
    public Chain<T, U, ?> to(@NonNull AbstractChainable<U, ?> consumer1, @NonNull AbstractChainable<U, ?> consumer2) {
        return MultipleChain.create(this, Arrays.asList(consumer1, consumer2));
    }

    @Override
    public Chain<T, U, ?> to(@NonNull AbstractChainable<U, ?> consumer1, @NonNull AbstractChainable<U, ?> consumer2, @NonNull AbstractChainable<U, ?> consumer3) {
        return MultipleChain.create(this, Arrays.asList(consumer1, consumer2, consumer3));
    }

    @Override
    public Chain<T, U, ?> to(@NonNull AbstractChainable<U, ?> consumer1, @NonNull AbstractChainable<U, ?> consumer2, @NonNull AbstractChainable<U, ?> consumer3, @NonNull AbstractChainable<U, ?> consumer4) {
        return MultipleChain.create(this, Arrays.asList(consumer1, consumer2, consumer3, consumer4));
    }

    @Override
    public Chain<T, U, ?> to(@NonNull List<AbstractChainable<U, ?>> tos) {
        return MultipleChain.create(this, tos);
    }

    @Override
    public <C, D, E> MergeChain<T, U, C, D, E> toWithAnotherFrom(AbstractChainable<C, D> from2, AbstractMergeChainable<U, D, E> to) {
        return MergeChain.create(this, from2, to);
    }
}
