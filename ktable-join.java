//--------------------------------------------------

public void TestJoinMultipleRecord()
{
    /*
bin/kafka-topics.sh --zookeeper $KAFKA_ZOOKEEPER_CONNECT --delete --topic fp1
bin/kafka-topics.sh --zookeeper $KAFKA_ZOOKEEPER_CONNECT --delete --topic fp2

bin/kafka-topics.sh --create --zookeeper $KAFKA_ZOOKEEPER_CONNECT  --replication-factor 1 --partitions 10 --topic fp1
bin/kafka-topics.sh --create --zookeeper $KAFKA_ZOOKEEPER_CONNECT  --replication-factor 1 --partitions 10 --topic fp2

docker -H beosbd01 exec -it kafka bin/kafka-console-producer.sh --broker-list beosbd01:9092 --topic fp1 --property "parse.key=true"  --property "key.separator=:"
docker -H beosbd01 exec -it kafka bin/kafka-console-producer.sh --broker-list beosbd01:9092 --topic fp2 --property "parse.key=true"  --property "key.separator=:"

// ref data --> to put in fp2 (ktable)
GEBABEBB:{"country":"belgium","description":"Generale de Banque"}
SOGEFRPP:{"country":"france","description":"Societe Generale"}
GKCCBEBB:{"country":"belgium","description":"Belfius"}


// test data --> to put in     fp1 (stream)
GEBABEBB:{"from_bic": "GEBABEBB", "app": "foobar", "amount": 32.95}
SOGEFRPP:{"from_bic": "SOGEFRPP", "app": "barfoo", "amount": 82.95}
GKCCBEBB:{"from_bic": "GKCCBEBB", "app": "testio", "amount": 22.95}

     */
    Properties props = new Properties();
    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "join-topics-fp");
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "beosbd01.swift.com:9092");
    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    //    props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG,0);

    final StreamsBuilder builder = new StreamsBuilder();
    KStream<String, String> leftStream = builder.stream("fp1");
    KTable<String, String> rightStream = builder.table("fp2");
    long parsedDelay = TimeUnit.SECONDS.toMillis(20);

    JoinWindows joinWindows = JoinWindows
            .of(0) // not use, we override it
            .until(TimeUnit.DAYS.toMillis(1)) // retention time
            .before(TimeUnit.SECONDS.toMillis(1)) // how much earlier can the "parsed" be ?
            .after(parsedDelay); // how late can the "parsed" be ?

    KStream<String, String> resultStream = leftStream.leftJoin(rightStream,
    (leftValue, rightValue) -> {
        return MergeLeftRight(leftValue,rightValue);
    });

    // display join result
    resultStream
            .foreach((k, v) -> {
                System.out.println(dateFormat.format(new Date())+"-- : KEY: [ " + k + " ]  VALUE: [ " + v + " ]");
            });

    KafkaStreams streams = new KafkaStreams(builder.build(), props);
    streams.cleanUp();
    System.out.println("Starting ...");
    streams.start();

    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
}

//--------------------------------------------------

String MergeLeftRight (String left, String right)
{
    System.out.println ("left" + left);
    if (right == null) return left;
    System.out.println ("right" + right.substring(1));
    return left.replace ("}", ","+right.substring(1));
}

    /*
    [fpolet@becssd01 ~]$ docker -H beosbd01 exec -it kafka bin/kafka-console-producer.sh --broker-list beosbd01:9092 --topic fp2 --property "parse.key=true"  --property "key.separator=:"
    >GEBABEBB:{"country":"belgium","description":"Generale de Banque"}
    >SOGEFRPP:{"country":"france","description":"Societe Generale"}
    >GKCCBEBB:{"country":"belgium","description":"Belfius"}
    >

    [fpolet@becssd01 ~]$ docker -H beosbd01 exec -it kafka bin/kafka-console-producer.sh --broker-list beosbd01:9092 --topic fp1 --property "parse.key=true"  --property "key.separator=:"
    >GEBABEBB:{"from_bic": "GEBABEBB", "app": "foobar", "amount": 32.95}
    >GEBABEBB:{"from_bic": "GEBABEBB", "app": "foobar", "amount": 32.95}
    >SOGEFRPP:{"from_bic": "SOGEFRPP", "app": "barfoo", "amount": 82.95}
    >SOGEFRPP:{"from_bic": "SOGEFRPP", "app": "barfoo", "amount": 82.95}
    >SOGEFRPP:{"from_bic": "SOGEFRPP", "app": "barfoo", "amount": 82.95}
    >GKCCBEBB:{"from_bic": "GKCCBEBB", "app": "testio", "amount": 22.95}
    >


    Starting ...
    left{"from_bic": "GEBABEBB", "app": "foobar", "amount": 32.95}
    right"country":"belgium","description":"Generale de Banque"}
    2019/06/07 13:35:30-- : KEY: [ GEBABEBB ]  VALUE: [ {"from_bic": "GEBABEBB", "app": "foobar", "amount": 32.95,"country":"belgium","description":"Generale de Banque"} ]

    left{"from_bic": "GEBABEBB", "app": "foobar", "amount": 32.95}
    right"country":"belgium","description":"Generale de Banque"}
    2019/06/07 13:35:37-- : KEY: [ GEBABEBB ]  VALUE: [ {"from_bic": "GEBABEBB", "app": "foobar", "amount": 32.95,"country":"belgium","description":"Generale de Banque"} ]

    left{"from_bic": "SOGEFRPP", "app": "barfoo", "amount": 82.95}
    right"country":"france","description":"Societe Generale"}
    2019/06/07 13:37:55-- : KEY: [ SOGEFRPP ]  VALUE: [ {"from_bic": "SOGEFRPP", "app": "barfoo", "amount": 82.95,"country":"france","description":"Societe Generale"} ]

    left{"from_bic": "SOGEFRPP", "app": "barfoo", "amount": 82.95}
    right"country":"france","description":"Societe Generale"}
    2019/06/07 13:37:57-- : KEY: [ SOGEFRPP ]  VALUE: [ {"from_bic": "SOGEFRPP", "app": "barfoo", "amount": 82.95,"country":"france","description":"Societe Generale"} ]

    left{"from_bic": "SOGEFRPP", "app": "barfoo", "amount": 82.95}
    right"country":"france","description":"Societe Generale"}
    2019/06/07 13:37:59-- : KEY: [ SOGEFRPP ]  VALUE: [ {"from_bic": "SOGEFRPP", "app": "barfoo", "amount": 82.95,"country":"france","description":"Societe Generale"} ]

    left{"from_bic": "GKCCBEBB", "app": "testio", "amount": 22.95}
    right"country":"belgium","description":"Belfius"}
    2019/06/07 13:38:05-- : KEY: [ GKCCBEBB ]  VALUE: [ {"from_bic": "GKCCBEBB", "app": "testio", "amount": 22.95,"country":"belgium","description":"Belfius"} ]

    Process finished with exit code 1

     */


//--------------------------------------------------