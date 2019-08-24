/*https://netjs.blogspot.com/2018/07/how-to-read-and-write-avro-file-hadoop-java.html

How to Read And Write Avro File in Hadoop

 
In this post we’ll see a Java program to read and write Avro files in Hadoop environment.

Required jars
For reading and writing an Avro file using Java API in Hadoop you will need to download following jars and add them to your project's classpath.

avro-1.8.2.jar
avro-tools-1.8.2.jar
The Avro Java implementation also depends on the Jackson JSON library. so you'll also need
jackson-mapper-asl-1.9.13.jar
jackson-core-asl-1.9.13.jar
Writing Avro file – Java program
To write an Avro file in Hadoop using Java API steps are as following.

You need an Avro schema.
In your program you will have to parse that scema.
Then you need to create records referring that parsed schema.
Write those records to file.
Avro Schema

Avro schema used for the program is called Person.avsc and it resides in the folder resources with in the project structure.

{
 "type": "record",
 "name": "personRecords",
 "doc": "Personnel Records",
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
   "name": "Address",
   "type": "string"
  }
 ]
}
Java Code
*/
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class AvroFileWrite {

    public static void main(String[] args) {
        Schema schema = parseSchema();
        writetoAvro(schema);
    }
    
    
    // parsing the schema
    private static Schema parseSchema() {
        Schema.Parser parser = new Schema.Parser();
        Schema schema = null;
        try {
            // Path to schema file
            schema = parser.parse(ClassLoader.getSystemResourceAsStream(
               "resources/Person.avsc"));
            
        } catch (IOException e) {
            e.printStackTrace();            
        }
        return schema;        
    }
    
    private static void writetoAvro(Schema schema) {
        GenericRecord person1 = new GenericData.Record(schema);
        person1.put("id", 1);
        person1.put("Name", "Jack");
        person1.put("Address", "1, Richmond Drive");
        
        GenericRecord person2 = new GenericData.Record(schema);
        person2.put("id", 2);
        person2.put("Name", "Jill");
        person2.put("Address", "2, Richmond Drive");
                    
        DatumWriter<GenericRecord> datumWriter = new 
                     GenericDatumWriter<GenericRecord>(schema);
        DataFileWriter<GenericRecord> dataFileWriter = null;
        try {
            //out file path in HDFS
            Configuration conf = new Configuration();
            // change the IP
            FileSystem fs = FileSystem.get(URI.create(
                       "hdfs://124.32.45.0:9000/test/out/person.avro"), conf);
            OutputStream out = fs.create(new Path(
                      "hdfs://124.32.45.0:9000/test/out/person.avro"));
                    
            dataFileWriter = new DataFileWriter<GenericRecord>(datumWriter);
            // for compression
            //dataFileWriter.setCodec(CodecFactory.snappyCodec());
            dataFileWriter.create(schema, out);
            
            dataFileWriter.append(person1);
            dataFileWriter.append(person2);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally {
            if(dataFileWriter != null) {
                try {
                    dataFileWriter.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
        }            
    }
} 

/*
To run this Java program in Hadoop environment export the class path where your .class file for the Java program resides.
$ export HADOOP_CLASSPATH=/home/netjs/eclipse-workspace/bin Then you can run the Java program using the following command.
$ hadoop org.netjs.AvroFileWrite


Reading Avro file – Java program
Java program to read back the Avro file written in the above program in Hadoop environment.
*/
import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.avro.mapred.FsInput;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

public class AvroFileRead {

    public static void main(String[] args) {
        Schema schema = parseSchema();
        readFromAvroFile(schema);
    }
    
    // parsing the schema
    private static Schema parseSchema() {
        Schema.Parser parser = new    Schema.Parser();
        Schema schema = null;
        try {
            // Path to schema file
            schema = parser.parse(ClassLoader.getSystemResourceAsStream(
                      "resources/Person.avsc"));
            
        } catch (IOException e) {
            e.printStackTrace();            
        }
        return schema;        
    }
        
    private static void readFromAvroFile(Schema schema) {
        
        Configuration conf = new Configuration();
        DataFileReader<GenericRecord> dataFileReader = null;
        try {
            // change the IP
            FsInput in = new FsInput(new Path(
                    "hdfs://124.32.45.0:9000/user/out/person.avro"), conf);
            DatumReader<GenericRecord> datumReader = new 
                     GenericDatumReader<GenericRecord>(schema);
            dataFileReader = new DataFileReader<GenericRecord>(in, datumReader);
            GenericRecord person = null;
            while (dataFileReader.hasNext()) {
                person = dataFileReader.next(person);
                System.out.println(person);
            }
        }catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally {
            if(dataFileReader != null) {
                try {
                    dataFileReader.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
        }       
    }
} 

/*
You can run the Java program using the following command.
$ hadoop org.netjs.AvroFileRead

{"id": 1, "Name": "Jack", "Address": "1, Richmond Drive"}
{"id": 2, "Name": "Jill", "Address": "2, Richmond Drive"}

*/