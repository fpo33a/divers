https://netjs.blogspot.com/2018/07/how-to-read-and-write-parquet-file-hadoop.html

/* schema
{
 "type": "record",
 "name": "empRecords",
 "doc": "Employee Records",
 "fields": 
  [{
   "name": "id", 
   "type": "int"
   
  }, 
  {
   "name": "Name",
   "type": "string"
  },
  {
   "name": "Dept",
   "type": "string"
  }
 ]
}

*/

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

public class ParquetFileWrite {

    public static void main(String[] args) {
        // First thing - parse the schema as it will be used
        Schema schema = parseSchema();
        List<GenericData.Record> recordList = getRecords(schema);
        writeToParquet(recordList, schema);
    }
    
    private static Schema parseSchema() {
        Schema.Parser parser = new    Schema.Parser();
        Schema schema = null;
        try {
            // pass path to schema
            schema = parser.parse(ClassLoader.getSystemResourceAsStream(
                "resources/EmpSchema.avsc"));
            
        } catch (IOException e) {
            e.printStackTrace();            
        }
        return schema;
        
    }
    
    private static List<GenericData.Record> getRecords(Schema schema){
        List<GenericData.Record> recordList = new ArrayList<GenericData.Record>();
        GenericData.Record record = new GenericData.Record(schema);
        // Adding 2 records
        record.put("id", 1);
        record.put("Name", "emp1");
        record.put("Dept", "D1");
        recordList.add(record);
        
        record = new GenericData.Record(schema);
        record.put("id", 2);
        record.put("Name", "emp2");
        record.put("Dept", "D2");
        recordList.add(record);
        
        return recordList;
    }
    
    
    private static void writeToParquet(List<GenericData.Record> recordList, Schema schema) {
        // Path to Parquet file in HDFS
        Path path = new Path("/test/EmpRecord.parquet");
        ParquetWriter<GenericData.Record> writer = null;
        // Creating ParquetWriter using builder
        try {
            writer = AvroParquetWriter.
                <GenericData.Record>builder(path)
                .withRowGroupSize(ParquetWriter.DEFAULT_BLOCK_SIZE)
                .withPageSize(ParquetWriter.DEFAULT_PAGE_SIZE)
                .withSchema(schema)
                .withConf(new Configuration())
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .withValidation(false)
                .withDictionaryEncoding(false)
                .build();
            
            for (GenericData.Record record : recordList) {
                writer.write(record);
            }
            
        }catch(IOException e) {
            e.printStackTrace();
        }finally {
            if(writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

/*
To run this Java program in Hadoop environment export the class path where your .class file for the Java program resides.

$ export HADOOP_CLASSPATH=/home/netjs/eclipse-workspace/bin 
Then you can run the Java program using the following command.

$ hadoop org.netjs.ParquetFileWrite 

 18/07/05 19:56:41 INFO compress.CodecPool: Got brand-new compressor [.snappy] 
 18/07/05 19:56:41 INFO hadoop.InternalParquetRecordWriter:Flushing mem columnStore to file. allocated memory: 3072 
*/

import java.io.IOException;

import org.apache.avro.generic.GenericData;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;

public class ParquetFileRead {

    public static void main(String[] args) {
        readParquetFile();
    }
        
    private static void readParquetFile() {
        ParquetReader<GenericData.Record> reader = null;
        Path path =    new    Path("/test/EmpRecord.parquet");
        try {
            reader = AvroParquetReader
                    .<GenericData.Record>builder(path)
                    .withConf(new Configuration())
                    .build();
            GenericData.Record record;
            while ((record = reader.read()) != null) {
                System.out.println(record);
            }
        }catch(IOException e) {
            e.printStackTrace();
        }finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}   
/*
Using parquet-tools jar
You can also download parquet-tools jar and use it to see the content of a Parquet file, file metadata of the Parquet file, Parquet schema etc. As example to see the content of a Parquet file-

$ hadoop jar /parquet-tools-1.10.0.jar cat /test/EmpRecord.parquet 
That's all for this topic How to Read And Write Parquet File in Hadoop. If you have any doubt or any suggestions to make please drop a comment. Thanks!
*/