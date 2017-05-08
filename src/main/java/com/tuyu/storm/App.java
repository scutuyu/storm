package com.tuyu.storm;

import com.tuyu.storm.bolt.SplitSentence;
import com.tuyu.storm.bolt.WordCount;
import com.tuyu.storm.spout.RandomSentenceSpout;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

/**
 * storm流式计算学习实例!
 * 单词计数，运行5秒结束，本例spout有一个并发度，split也有一个并发度，count也有一个并发度
 * RandomSentenceSpout这个spout随机从数组中选出一个句子随机（shuffleGrouping）发给下游（SplitSentence），每隔0.1秒发送一次
 * SplitSentence这个bolt接收上游（RandomSentence）发送的句子，把句子按空格切分，把单词按域发给下游（WordCount）
 * WordCount这个bolt接收上游（SplitSentence）发送的单词，在它内部保存了一个hash map，key=单词，value=次数,对单词进行计数，实现cleanup接口，在结束时打印统计结果
 *
 */
public class App 
{
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
