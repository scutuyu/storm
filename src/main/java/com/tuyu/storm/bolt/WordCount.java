package com.tuyu.storm.bolt;

import org.apache.storm.topology.BasicOutputCollector;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseBasicBolt;
import org.apache.storm.tuple.Tuple;

import java.util.HashMap;

/**
 * Created by tuyu on 5/8/17.
 */
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
