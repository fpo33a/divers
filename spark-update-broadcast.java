https://stackoverflow.com/questions/33372264/how-can-i-update-a-broadcast-variable-in-spark-streaming

public class BroadcastWrapper {

    private Broadcast<ReferenceData> broadcastVar;
    private Date lastUpdatedAt = Calendar.getInstance().getTime();

    private static BroadcastWrapper obj = new BroadcastWrapper();

    private BroadcastWrapper(){}

    public static BroadcastWrapper getInstance() {
        return obj;
    }

    public JavaSparkContext getSparkContext(SparkContext sc) {
       JavaSparkContext jsc = JavaSparkContext.fromSparkContext(sc);
       return jsc;
    }

    public Broadcast<ReferenceData> updateAndGet(SparkContext sparkContext){
        Date currentDate = Calendar.getInstance().getTime();
        long diff = currentDate.getTime()-lastUpdatedAt.getTime();
        if (var == null || diff > 60000) { //Lets say we want to refresh every 1 min = 60000 ms
            if (var != null)
               var.unpersist();
            lastUpdatedAt = new Date(System.currentTimeMillis());

            //Your logic to refresh
            ReferenceData data = getRefData();

            var = getSparkContext(sparkContext).broadcast(data);
       }
       return var;
   }
}


public void startSparkEngine() {

    final JavaDStream<MyObject> filteredStream = objectStream.transform(stream -> {
        Broadcast<ReferenceData> refdataBroadcast = BroadcastWrapper.getInstance().updateAndGet(stream.context());

        stream.filter(obj -> obj.getField().equals(refdataBroadcast.getValue().getField()));
    });

    filteredStream.foreachRDD(rdd -> {
        rdd.foreach(obj -> {
        // Final processing of filtered objects
        });
        return null;
    });
}

import java.io.{ ObjectInputStream, ObjectOutputStream }
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.streaming.StreamingContext
import scala.reflect.ClassTag

/* wrapper lets us update brodcast variables within DStreams' foreachRDD
 without running into serialization issues */
case class BroadcastWrapper[T: ClassTag](
 @transient private val ssc: StreamingContext,
  @transient private val _v: T) {

  @transient private var v = ssc.sparkContext.broadcast(_v)

  def update(newValue: T, blocking: Boolean = false): Unit = {

    v.unpersist(blocking)
    v = ssc.sparkContext.broadcast(newValue)
  }

  def value: T = v.value

  private def writeObject(out: ObjectOutputStream): Unit = {
    out.writeObject(v)
  }

  private def readObject(in: ObjectInputStream): Unit = {
    v = in.readObject().asInstanceOf[Broadcast[T]]
  }
}