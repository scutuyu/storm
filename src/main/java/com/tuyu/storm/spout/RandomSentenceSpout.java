package com.tuyu.storm.spout;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.util.Map;
import java.util.Random;

/**
 * Created by tuyu on 5/8/17.
 */
public class RandomSentenceSpout extends BaseRichSpout {

    SpoutOutputCollector _collector;
    Random _random;


    /**
     * 初始化
     * @param map
     * @param topologyContext
     * @param spoutOutputCollector
     */
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
