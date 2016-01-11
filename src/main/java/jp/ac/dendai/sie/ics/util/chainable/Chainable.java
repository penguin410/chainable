package jp.ac.dendai.sie.ics.util.chainable;


import java.util.List;

/**
 * Created by keisuke on 2015/12/08.
 */
public interface Chainable<T, U> {
    <R> Chain<T, U, R> to(AbstractChainable<U, R> consumer);

    Chain<T, U, ?> to(AbstractChainable<U, ?> consumer1, AbstractChainable<U, ?> consumer2);

    Chain<T, U, ?> to(AbstractChainable<U, ?> consumer1, AbstractChainable<U, ?> consumer2, AbstractChainable<U, ?> consumer3);

    Chain<T, U, ?> to(AbstractChainable<U, ?> consumer1, AbstractChainable<U, ?> consumer2, AbstractChainable<U, ?> consumer3, AbstractChainable<U, ?> consumer4);

    Chain<T, U, ?> to(List<AbstractChainable<U, ?>> tos);

    <C, D, E> MergeChain<T, U, C, D, E> toWithAnotherFrom(AbstractChainable<C, D> from2, AbstractMergeChainable<U, D, E> to);
}















