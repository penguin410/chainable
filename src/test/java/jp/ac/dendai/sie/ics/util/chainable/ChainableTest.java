package jp.ac.dendai.sie.ics.util.chainable;

import jp.ac.dendai.sie.ics.util.chainable.*;
import org.junit.Test;


import java.util.concurrent.ArrayBlockingQueue;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * Created by keisuke on 2015/12/08.
 */
public class ChainableTest {
    @Test
    public void シンプルな一段フロー定義() throws Exception {
        StringChainableSupplier supplier = new StringChainableSupplier();
        StringChainableConsumer consumer = new StringChainableConsumer();
        StringToStringChainableFunction function1 = new StringToStringChainableFunction();
        StringToStringChainableFunction function2 = new StringToStringChainableFunction();

        Chain<Void, String, Void> chain_void_void = supplier.to(consumer);
        assertThat(chain_void_void.getFrom(), is(sameInstance(supplier)));
        assertThat(chain_void_void.getTo(), is(sameInstance(consumer)));

        Chain<Void, String, String> chain_void_string = supplier.to(function1);
        assertThat(chain_void_string.getFrom(), is(sameInstance(supplier)));
        assertThat(chain_void_string.getTo(), is(sameInstance(function1)));

        Chain<String, String, Void> chain_string_void = function1.to(consumer);
        assertThat(chain_string_void.getFrom(), is(sameInstance(function1)));
        assertThat(chain_string_void.getTo(), is(sameInstance(consumer)));

        Chain<String, String, String> chain_string_string = function1.to(function2);
        assertThat(chain_string_string.getFrom(), is(sameInstance(function1)));
        assertThat(chain_string_string.getTo(), is(sameInstance(function2)));
    }

    @Test
    public void 直線に二段フロー定義() throws Exception {
        StringChainableSupplier supplier = new StringChainableSupplier();
        StringChainableConsumer consumer = new StringChainableConsumer();
        StringToStringChainableFunction function = new StringToStringChainableFunction();

        Chain<Void, String, String> chain_void_string = supplier.to(function);
        assertThat(chain_void_string.getFrom(), is(sameInstance(supplier)));
        assertThat(chain_void_string.getTo(), is(sameInstance(function)));

        Chain<Void, String, Void> chain_void_void = chain_void_string.to(consumer);
        assertThat(chain_void_void.getFrom(), is(sameInstance(chain_void_string)));
        assertThat(chain_void_void.getTo(), is(sameInstance(consumer)));
    }

    @Test
    public void 直線に三段フロー定義() throws Exception {
        StringChainableSupplier supplier = new StringChainableSupplier();
        StringChainableConsumer consumer = new StringChainableConsumer();
        StringToStringChainableFunction function1 = new StringToStringChainableFunction();
        StringToStringChainableFunction function2 = new StringToStringChainableFunction();

        Chain<Void, String, String> chain_void_string = supplier.to(function1);
        Chain<Void, String, String> chain_void_string2 = chain_void_string.to(function2);
        Chain<Void, String, Void> chain_void_void = chain_void_string2.to(consumer);
        assertThat(chain_void_void.getFrom(), is(sameInstance(chain_void_string2)));
        assertThat(chain_void_void.getTo(), is(sameInstance(consumer)));
    }

    @Test
    public void 複数のConsumerをひとつのSupplierに接続() throws Exception {
        StringChainableSupplier supplier = new StringChainableSupplier();
        StringChainableConsumer consumer1 = new StringChainableConsumer();
        StringChainableConsumer consumer2 = new StringChainableConsumer();

        Chain<Void, String, Void> chain1 = supplier.to(consumer1);
        Chain<Void, String, Void> chain2 = supplier.to(consumer2);
        assertThat(chain1.getFrom(), is(sameInstance(supplier)));
        assertThat(chain1.getTo(), is(sameInstance(consumer1)));
        assertThat(chain2.getFrom(), is(sameInstance(supplier)));
        assertThat(chain2.getTo(), is(sameInstance(consumer2)));
    }

    @Test
    public void 並列なデータフローを定義() {
        StringChainableSupplier supplier = new StringChainableSupplier();
        StringChainableConsumer consumer1 = new StringChainableConsumer();
        supplier.to(consumer1);

        StringToStringChainableFunction function1 = new StringToStringChainableFunction();
        StringChainableConsumer consumer2 = new StringChainableConsumer();
        supplier.to(function1).to(consumer2);

        StringToStringChainableFunction function2 = new StringToStringChainableFunction();
        StringToStringChainableFunction function3 = new StringToStringChainableFunction();
        StringChainableConsumer consumer3 = new StringChainableConsumer();
        Chain<String, String, Void> chain_string_void = function2.to(function3).to(consumer3);

        Chain<Void, String, Void> chain_void_void = supplier.to(chain_string_void);
        assertThat(chain_void_void.getTo(), is(sameInstance(chain_string_void)));
        assertThat(chain_void_void.getFrom(), is(sameInstance(supplier)));
    }

    @Test
    public void 数回直列接続したのちに並列接続しchain() throws Exception {
        StringChainableSupplier supplier = new StringChainableSupplier();
        StringToStringChainableFunction function1 = new StringToStringChainableFunction();
        StringToStringChainableFunction function2 = new StringToStringChainableFunction();
        StringToStringChainableFunction function3 = new StringToStringChainableFunction();
        StringChainableConsumer consumer1 = new StringChainableConsumer();
        StringChainableConsumer consumer2 = new StringChainableConsumer();
        StringChainableConsumer consumer3 = new StringChainableConsumer();

        supplier.to(function1).to(
                function2.to(consumer1),
                function3.to(consumer2),
                consumer3
        ).chain();

        assertThat(supplier.isDestChained(), is(true));
        assertThat(function1.isDestChained(), is(true));
        assertThat(function1.isSrcChained(), is(true));
        assertThat(function2.isDestChained(), is(true));
        assertThat(function2.isSrcChained(), is(true));
        assertThat(function3.isDestChained(), is(true));
        assertThat(function3.isSrcChained(), is(true));
        assertThat(consumer1.isSrcChained(), is(true));
        assertThat(consumer2.isSrcChained(), is(true));
        assertThat(consumer3.isSrcChained(), is(true));
    }

    @Test
    public void MergeChainを含めて数回直列接続したのちに並列接続しchain() throws Exception {
        StringChainableSupplier supplier = new StringChainableSupplier();
        StringToStringChainableFunction function1 = new StringToStringChainableFunction();
        StringToStringChainableFunction function2 = new StringToStringChainableFunction();
        StringToStringChainableFunction function3 = new StringToStringChainableFunction();
        StringChainableConsumer consumer1 = new StringChainableConsumer();
        StringChainableConsumer consumer2 = new StringChainableConsumer();
        StringAndStringMergeChainableConsumer mergeConsumer = new StringAndStringMergeChainableConsumer();

        supplier.to(function1).to(
                function2.to(consumer1),
                function3,
                consumer2
        ).chain();
        supplier.toWithAnotherFrom(function3, mergeConsumer).chain();

        assertThat(supplier.isDestChained(), is(true));
        assertThat(function1.isDestChained(), is(true));
        assertThat(function1.isSrcChained(), is(true));
        assertThat(function2.isDestChained(), is(true));
        assertThat(function2.isSrcChained(), is(true));
        assertThat(function3.isDestChained(), is(true));
        assertThat(function3.isSrcChained(), is(true));
        assertThat(consumer1.isSrcChained(), is(true));
        assertThat(consumer2.isSrcChained(), is(true));
        assertThat(mergeConsumer.isSrcChained(), is(true));
    }

    @Test
    public void MergeChainableCnosumerフロー定義テスト() throws Exception {
        StringChainableSupplier supplier1 = new StringChainableSupplier();
        StringChainableSupplier supplier2 = new StringChainableSupplier();
        StringAndStringMergeChainableConsumer biConsumer = new StringAndStringMergeChainableConsumer();

        MergeChain<Void, String, Void, String, Void> mergeChain = supplier1.toWithAnotherFrom(supplier2, biConsumer);
        assertThat(mergeChain.getTo(), is(sameInstance(biConsumer)));
        assertThat(mergeChain.getFrom1(), is(sameInstance(supplier1)));
        assertThat(mergeChain.getFrom2(), is(sameInstance(supplier2)));
    }

    @Test
    public void データフロー定義後に末尾だけchainメソッドを呼び出すことで全て連結される() throws Exception {
        StringChainableSupplier supplier = new StringChainableSupplier();
        StringToStringChainableFunction function = new StringToStringChainableFunction();
        StringChainableConsumer consumer = new StringChainableConsumer();

        supplier.to(function).to(consumer).chain();

        assertThat(supplier.isDestChained(), is(true));
        assertThat(function.isDestChained(), is(true));
        assertThat(function.isSrcChained(), is(true));
        assertThat(consumer.isSrcChained(), is(true));
    }

    @Test
    public void 直線的chain後にunchainで接続を解除できる() throws Exception {
        StringChainableSupplier supplier = new StringChainableSupplier();
        StringToStringChainableFunction function = new StringToStringChainableFunction();
        StringChainableConsumer consumer = new StringChainableConsumer();

        assertThat(supplier.isDestChained(), is(false));
        assertThat(function.isDestChained(), is(false));
        assertThat(function.isSrcChained(), is(false));
        assertThat(consumer.isSrcChained(), is(false));

        Chain<Void, String, Void> chain = supplier.to(function).to(consumer);
        chain.chain();

        assertThat(supplier.isDestChained(), is(true));
        assertThat(function.isDestChained(), is(true));
        assertThat(function.isSrcChained(), is(true));
        assertThat(consumer.isSrcChained(), is(true));

        chain.unchain();

        assertThat(supplier.isDestChained(), is(false));
        assertThat(function.isDestChained(), is(false));
        assertThat(function.isSrcChained(), is(false));
        assertThat(consumer.isSrcChained(), is(false));
    }

    @Test
    public void 複雑なchain後もunchainで接続解除できる() throws Exception {
        StringChainableSupplier supplier = new StringChainableSupplier();
        StringToStringChainableFunction function1 = new StringToStringChainableFunction();
        StringToStringChainableFunction function2 = new StringToStringChainableFunction();
        StringToStringChainableFunction function3 = new StringToStringChainableFunction();
        StringChainableConsumer consumer1 = new StringChainableConsumer();
        StringChainableConsumer consumer2 = new StringChainableConsumer();
        StringChainableConsumer consumer3 = new StringChainableConsumer();

        Chain<Void, String, ?> chain = supplier.to(
                function3.to(consumer1),
                function1.to(function2).to(consumer2),
                consumer3
        );
        chain.chain();

        assertThat(supplier.isDestChained(), is(true));
        assertThat(function1.isDestChained(), is(true));
        assertThat(function1.isSrcChained(), is(true));
        assertThat(function2.isDestChained(), is(true));
        assertThat(function2.isSrcChained(), is(true));
        assertThat(function3.isDestChained(), is(true));
        assertThat(function3.isSrcChained(), is(true));
        assertThat(consumer1.isSrcChained(), is(true));
        assertThat(consumer2.isSrcChained(), is(true));
        assertThat(consumer3.isSrcChained(), is(true));

        chain.unchain();

        assertThat(supplier.isDestChained(), is(false));
        assertThat(function1.isDestChained(), is(false));
        assertThat(function1.isSrcChained(), is(false));
        assertThat(function2.isDestChained(), is(false));
        assertThat(function2.isSrcChained(), is(false));
        assertThat(function3.isDestChained(), is(false));
        assertThat(function3.isSrcChained(), is(false));
        assertThat(consumer1.isSrcChained(), is(false));
        assertThat(consumer2.isSrcChained(), is(false));
        assertThat(consumer3.isSrcChained(), is(false));
    }

    @Test(expected = AlreadyChainedException.class)
    public void 二重にChainすると例外を吐く_supplier_consumer() throws Exception {
        StringChainableSupplier supplier = new StringChainableSupplier();
        StringChainableConsumer consumer = new StringChainableConsumer();

        supplier.to(consumer).chain();
        supplier.to(consumer).chain();
    }

    @Test(expected = AlreadyChainedException.class)
    public void 二重にChainすると例外を吐く_supplier_function() throws Exception {
        StringChainableSupplier supplier = new StringChainableSupplier();
        StringToStringChainableFunction function = new StringToStringChainableFunction();

        supplier.to(function).chain();
        supplier.to(function).chain();
    }

    @Test(expected = AlreadyChainedException.class)
    public void 二重にChainすると例外を吐く_function_consumer() throws Exception {
        StringToStringChainableFunction function = new StringToStringChainableFunction();
        StringChainableConsumer consumer = new StringChainableConsumer();

        function.to(consumer).chain();
        function.to(consumer).chain();
    }

    @Test(expected = AlreadyChainedException.class)
    public void 二重にChainすると例外を吐く_supplier_mergeConsumer() throws Exception {
        StringChainableSupplier supplier1 = new StringChainableSupplier();
        StringChainableSupplier supplier2 = new StringChainableSupplier();
        StringAndStringMergeChainableConsumer mergeConsumer = new StringAndStringMergeChainableConsumer();

        supplier1.toWithAnotherFrom(supplier2, mergeConsumer).chain();
        supplier1.toWithAnotherFrom(supplier2, mergeConsumer).chain();
    }

    @Test(expected = AlreadyChainedException.class)
    public void 先に中間のデータフローのChainがchain済みである場合例外を吐く() throws Exception {
        StringChainableSupplier supplier = new StringChainableSupplier();
        StringToStringChainableFunction function1 = new StringToStringChainableFunction();
        StringToStringChainableFunction function2 = new StringToStringChainableFunction();
        StringChainableConsumer consumer = new StringChainableConsumer();

        assertThat(supplier.isDestChained(), is(false));
        assertThat(function1.isDestChained(), is(false));
        assertThat(function1.isSrcChained(), is(false));
        assertThat(function2.isDestChained(), is(false));
        assertThat(function2.isSrcChained(), is(false));
        assertThat(consumer.isSrcChained(), is(false));

        Chain<String, String, String> func1_func2 = function1.to(function2);
        func1_func2.chain();

        Chain<Void, String, Void> chain = supplier.to(func1_func2).to(consumer);
        chain.chain();

        assertThat(supplier.isDestChained(), is(true));
        assertThat(function1.isDestChained(), is(true));
        assertThat(function1.isSrcChained(), is(true));
        assertThat(function2.isDestChained(), is(true));
        assertThat(function2.isSrcChained(), is(true));
        assertThat(consumer.isSrcChained(), is(true));
    }

    @Test(expected = AlreadyChainedException.class)
    public void queueメソッドを呼ぶ前のChainは利用できない() throws Exception {
        StringChainableSupplier supplier = new StringChainableSupplier();
        StringToStringChainableFunction function = new StringToStringChainableFunction();
        StringChainableConsumer consumer = new StringChainableConsumer();

        Chain<Void, String, String> chain = supplier.to(function);
        chain.queue(new ArrayBlockingQueue<>(10));

        chain.to(consumer);
    }

    @Test
    public void 中間のデータフローがchain済みでなければ全体としてchainできる() {
        StringChainableSupplier supplier = new StringChainableSupplier();
        StringToStringChainableFunction function1 = new StringToStringChainableFunction();
        StringToStringChainableFunction function2 = new StringToStringChainableFunction();
        StringChainableConsumer consumer = new StringChainableConsumer();

        assertThat(supplier.isDestChained(), is(false));
        assertThat(function1.isDestChained(), is(false));
        assertThat(function1.isSrcChained(), is(false));
        assertThat(function2.isDestChained(), is(false));
        assertThat(function2.isSrcChained(), is(false));
        assertThat(consumer.isSrcChained(), is(false));

        supplier.to(function1.to(function2)).to(consumer).chain();

        assertThat(supplier.isDestChained(), is(true));
        assertThat(function1.isDestChained(), is(true));
        assertThat(function1.isSrcChained(), is(true));
        assertThat(function2.isDestChained(), is(true));
        assertThat(function2.isSrcChained(), is(true));
        assertThat(consumer.isSrcChained(), is(true));
    }

    @Test(timeout = 1000)
    public void データ流し込みテスト_supplier_consumer() throws Exception {
        StringChainableSupplier supplier = new StringChainableSupplier();
        StringChainableConsumer consumer = new StringChainableConsumer();
        supplier.to(consumer).chain();

        final String expected = "input text";
        supplier.offer(expected);
        assertThat(consumer.take(), is(expected));
    }

    @Test(timeout = 1000)
    public void データ流し込みテスト_supplier_function_consumer() throws Exception {
        StringChainableSupplier supplier = new StringChainableSupplier();
        StringToStringChainableFunction function = new StringToStringChainableFunction();
        StringChainableConsumer consumer = new StringChainableConsumer();

        supplier.to(function).to(consumer).chain();

        final String expected = "input text";
        supplier.offer(expected);
        function.proc();
        assertThat(consumer.take(), is(expected));
    }

    @Test(timeout = 1000)
    public void データ流し込みテスト_supplier_function_function_consumer() throws Exception {
        StringChainableSupplier supplier = new StringChainableSupplier();
        StringToStringChainableFunction function1 = new StringToStringChainableFunction();
        StringToStringChainableFunction function2 = new StringToStringChainableFunction();
        StringChainableConsumer consumer = new StringChainableConsumer();

        supplier.to(function1).to(function2).to(consumer).chain();

        final String expected = "input text";
        supplier.offer(expected);
        function1.proc();
        function2.proc();
        assertThat(consumer.take(), is(expected));
    }

    @Test(timeout = 1000)
    public void データ流し込みテスト_入れ子構造_supplier_function_function_consumer() throws Exception {
        StringChainableSupplier supplier = new StringChainableSupplier();
        StringToStringChainableFunction function1 = new StringToStringChainableFunction();
        StringToStringChainableFunction function2 = new StringToStringChainableFunction();
        StringChainableConsumer consumer = new StringChainableConsumer();

        supplier.to(function1.to(function2)).to(consumer).chain();

        final String expected = "input text";
        supplier.offer(expected);
        function1.proc();
        function2.proc();
        assertThat(consumer.take(), is(expected));
    }

    @Test(timeout = 1000)
    public void データ流し込みテスト_直列と入れ子構造_supplier_function_function_consumer() throws Exception {
        StringChainableSupplier supplier = new StringChainableSupplier();
        StringToStringChainableFunction function1 = new StringToStringChainableFunction();
        StringToStringChainableFunction function2 = new StringToStringChainableFunction();
        StringToStringChainableFunction function3 = new StringToStringChainableFunction();
        StringChainableConsumer consumer = new StringChainableConsumer();

        supplier.to(function1.to(function2)).to(function3).to(consumer).chain();

        final String expected = "input text";
        supplier.offer(expected);
        function1.proc();
        function2.proc();
        function3.proc();
        assertThat(consumer.take(), is(expected));
    }

    final class StringChainableSupplier extends ChainableSupplierBase<String> {
    }

    final class StringChainableConsumer extends ChainableConsumerBase<String> {
    }

    final class StringToStringChainableFunction extends ChainableFunctionBase<String, String> {
        public void proc() throws InterruptedException {
            offer(take());
        }
    }

    final class StringAndStringMergeChainableConsumer extends MergeChainableConsumerBase<String, String> {
    }
}
