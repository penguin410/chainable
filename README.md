# chainable

```
public class Main {
    public static void main() {
        TextReader reader = new TextReader();
        HeaderParser headerParser = new HeaderParser();
        BodyParser bodyParser = new BodyParser();
        TextView view = new TextView();

        // データフロー定義
        Chain chain_single = reader.to(headerParser, bodyParser);
        Chain chain_multiple = headerParser.toWithAnotherFrom(bodyParser, view);
        
        // BlockingQueueで連結
        chain_single.chain();
        chain_multiple.chain();

        // 各Chainableを独立に動かす
        Arrays.asList(
                (Runnable) () -> {
                    while (true) {
                        reader.read();
                    }
                },
                (Runnable) () -> {
                    while (true) {
                        headerParser.parse();
                    }
                },
                (Runnable) () -> {
                    while (true) {
                        bodyParser.parse();
                    }
                },
                (Runnable) () -> {
                    while (true) {
                        view.draw();
                    }
                }
        ).forEach(task -> new Thread(task).start());
        
        // 接続を切る
        chain_single.unchain();
        chain_multiple.unchain();
    }
}

class TextReader extends ChainableSuplierBase<String> {
    public void read() {
        String line = "header:inputbody"; // なんか文字列をファイルから読みこんだりする
        this.offer(line);
    }
}

class HeaderParser extends ChainableFunctionBase<String, TextHeader> {
    public void parse() {
        String line = this.src.poll();
        this.offer(() -> new TextHeader(line.split(":")[0]));
    }
}

class BodyParser extends ChainableFunctionBase<String, TextBody> {
    public void parse() {
        String line = this.src.poll();
        this.offer(() -> new TextBody(line.split(":")[1]));
    }
}

class TextView extends MergeChainableConsumerBase<TextHeader, TextBody> {
    public void draw() {
        TextHeader header = this.firstSrc.poll();
        TextBody body = this.secondSrc.poll();
        // 表示する処理とか
    }
}
```
