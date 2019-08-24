mport org.apache.kafka.common.serialization.Serdes;

import org.apache.kafka.streams.KafkaStreams;

import org.apache.kafka.streams.StreamsBuilder;

import org.apache.kafka.streams.StreamsConfig;

import org.apache.kafka.streams.kstream.*;

 

import java.util.Properties;

import java.util.concurrent.TimeUnit;

 

public class KafkaStreamSeparateUnjoined {

  public static void main(String[] args) {

 

    Properties props = new Properties();

    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "join-topics");

    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:59092");

    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());

    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

 

    final StreamsBuilder builder = new StreamsBuilder();

    KStream<String, String> sourceStream = builder.stream("source_topic_1");

    KStream<String, String> parsedStream = builder.stream("parsed_topic");

 

    long parsedDelay = TimeUnit.SECONDS.toMillis(2);

 

    JoinWindows joinWindows = JoinWindows

        .of(0) // not use, we override it

        .until(TimeUnit.DAYS.toMillis(1)) // retention time

        .before(TimeUnit.SECONDS.toMillis(1)) // how much earlier can the "parsed" be ?

        .after(parsedDelay); // how late can the "parsed" be ?

 

    KStream<String, String> resultStream = sourceStream.leftJoin(parsedStream,

        (sourceValue, parsedValue) -> parsedValue,

        joinWindows,

        Joined.with(

            Serdes.String(),

            Serdes.String(),

            Serdes.String())

    );

 

    // as soon as there's a join, print it

    resultStream

        .filter((k, v) -> v != null)

        .foreach((k, v) -> {

          System.out.println("JOIN STREAM: KEY: [ " + k + " ]           VALUE: [ " + v + " ]");

        });

 

 

    // we group arriving event. After a certain delay, we print unmatched events

    KStream<Windowed<String>, String> reducedStream = resultStream

        .groupByKey()

        .windowedBy(TimeWindows.of(parsedDelay))

        .reduce(KafkaStreamSeparateUnjoined::keepNotNull)

        .toStream();

 

 

    reducedStream.foreach((k, v) -> {

      System.out.println("REDUCED STREAM: KEY: [ " + k + " ]           VALUE: [ " + v + " ]");

    });

 

    reducedStream

        .filter((k, v) -> v == null)

        .foreach((k, v) -> {

          System.out.println("       NOT JOINED STREAM: KEY: [ " + k + " ]           VALUE: [ " + v + " ]");

        });

 

    KafkaStreams streams = new KafkaStreams(builder.build(), props);

    streams.start();

  }

 

  private static String keepNotNull(String a, String b) {

    if (a == null && b == null) {

      return null;

    }

 

    if (a == null) {

      return b;

    }

 

    if (b == null) {

      return a;

    }

 

    return a + b;

  }

}

 