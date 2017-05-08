# storm
learn storm from zero

1. __pull the project__
> git clone https://github.com/scutuyu/storm.git

2.  __load the project using idea__
> File >> Open >> find the project

3. __run the project__
> right click App.java >> Run 'App.main()'

### 详细介绍

本例是一个Apache Storm 的一个简单例子--单词计数，适合初学者

##### 1. 项目结构如下：
    ├── App.java
    ├── bolt
    │   ├── SplitSentence.java
    │   └── WordCount.java
    └── spout
        └── RandomSentenceSpout.java
##### 2. App.java
单词计数，运行5秒结束，本例spout有一个并发度，split也有一个并发度，count也有一个并发度

    public class App{
        public static void main( String[] args ) throws Exception
        {
            TopologyBuilder builder = new TopologyBuilder();
            builder.setSpout("spout", new RandomSentenceSpout(), 1);//并发度为1
            builder.setBolt("split", new SplitSentence(), 1).shuffleGrouping("spout");//并发度为1，随机发送
            builder.setBolt("count", new WordCount(), 1).fieldsGrouping("split", new Fields("word"));//并发度为1，按word字段发送

            Config config = new Config();
            config.setDebug(true);

            if (args != null && args.length > 0) {
                config.setNumWorkers(3);
                StormSubmitter.submitTopologyWithProgressBar(args[0], config, builder.createTopology());
            } else {
                LocalCluster cluster = new LocalCluster();
                cluster.submitTopology("word-count", config, builder.createTopology());
                Thread.sleep(5000);//运行5秒结束
                cluster.shutdown();
            }
        }
    }
##### 3. RandomSentenceSpout.java(spout)
RandomSentenceSpout这个spout随机从数组中选出一个句子随机（shuffleGrouping）发给下游（SplitSentence），每隔0.1秒发送一次

    public class RandomSentenceSpout extends BaseRichSpout {

    SpoutOutputCollector _collector;
    Random _random;

    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        _collector = spoutOutputCollector;
        _random = new Random();
    }

    public void nextTuple() {
        String[] sentences = new String[]{"hello my name is tuyu", "hi she is Lily", "how do you do", "that's great"};
        String sentence = sentences[_random.nextInt(sentences.length)];//随机发送
        _collector.emit(new Values(sentence));
        Utils.sleep(100);//每隔0.1秒发送一个句子
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("sentence"));//定义字段
    }
}
##### 4. SplitSentence.java(bolt)
SplitSentence这个bolt接收上游（RandomSentence）发送的句子，把句子按空格切分，把单词按域发给下游（WordCount）

    public class SplitSentence extends BaseBasicBolt {
        public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
            String sentence = tuple.getString(0);
            System.out.println("==> origin sentence : " + sentence);
            String[] words = sentence.split(" ");
            for (String string : words){
                basicOutputCollector.emit(new Values(string));
            }
        }

        public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
            outputFieldsDeclarer.declare(new Fields("word"));//定义字段
        }
    }
##### 5. WordCount.java(bolt)
WordCount这个bolt接收上游（SplitSentence）发送的单词，在它内部保存了一个hash map，key=单词，value=次数,对单词进行计数，实现cleanup接口，在结束时打印统计结果

    public class WordCount extends BaseBasicBolt {
        private HashMap<String, Integer> counter = new HashMap<String, Integer>();
        public void execute(Tuple tuple, BasicOutputCollector basicOutputCollector) {
            String word = tuple.getString(0);
            if (counter.containsKey(word)){
                int count = counter.get(word);
                count ++;
                System.out.println("wordCount receive " + word + " ------> " + count );
                counter.put(word, count);
            }else {
                System.out.println("wordCount receive " + word + " ------> " + 1 );
                counter.put(word, 1);
            }
        }

        @Override
        public void cleanup() {//实现cleanup接口，在cluster.shutdown()的时候调用，打印单词统计结果
            for (String key : counter.keySet()){
                System.out.println("total ---> " + key + " : " + counter.get(key));
            }
        }

        public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

        }
    }
