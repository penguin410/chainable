package jp.ac.dendai.sie.ics.util.chainable;

import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.concurrent.BlockingQueue;

/**
 * //TODO: rename
 * Created by keisuke on 2015/12/17.
 */
@AllArgsConstructor
@Value
public class ChainableAndBlockingQueuePair<U, R> {
    AbstractChainable<U, R> to;
    BlockingQueue<U> queue;
}
