https://www.arm64.ca/post/reading-parquet-files-java/

<dependencies>
    <dependency>
        <groupId>org.apache.parquet</groupId>
        <artifactId>parquet-avro</artifactId>
        <version>1.10.0</version>
    </dependency>
    <dependency>
        <groupId>org.apache.hadoop</groupId>
        <artifactId>hadoop-common</artifactId>
        <version>3.1.0</version>
    </dependency>
</dependencies>

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.column.page.PageReadStore;
import org.apache.parquet.example.data.simple.SimpleGroup;
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.ColumnIOFactory;
import org.apache.parquet.io.MessageColumnIO;
import org.apache.parquet.io.RecordReader;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.Type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParquetReaderUtils {

    public static Parquet getParquetData(String filePath) throws IOException {
        List<SimpleGroup> simpleGroups = new ArrayList<>();
        ParquetFileReader reader = ParquetFileReader.open(HadoopInputFile.fromPath(new Path(filePath), new Configuration()));
        MessageType schema = reader.getFooter().getFileMetaData().getSchema();
        List<Type> fields = schema.getFields();
        PageReadStore pages;
        while ((pages = reader.readNextRowGroup()) != null) {
            long rows = pages.getRowCount();
            MessageColumnIO columnIO = new ColumnIOFactory().getColumnIO(schema);
            RecordReader recordReader = columnIO.getRecordReader(pages, new GroupRecordConverter(schema));

            for (int i = 0; i < rows; i++) {
                SimpleGroup simpleGroup = (SimpleGroup) recordReader.read();
                simpleGroups.add(simpleGroup);
            }
        }
        reader.close();
        return new Parquet(simpleGroups, fields);
    }
}


import org.apache.parquet.example.data.simple.SimpleGroup;
import org.apache.parquet.schema.Type;

import java.util.List;

public class Parquet {
    private List<SimpleGroup> data;
    private List<Type> schema;

    public Parquet(List<SimpleGroup> data, List<Type> schema) {
        this.data = data;
        this.schema = schema;
    }

    public List<SimpleGroup> getData() {
        return data;
    }

    public List<Type> getSchema() {
        return schema;
    }
}



https://creativedata.atlassian.net/wiki/spaces/SAP/pages/52199514/Java+-+Read+Write+files+with+HDFS

Common part
Maven Dependencies
<dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-client</artifactId>
            <version>${hadoop.version}</version>
 </dependency>
HDFS URI
HDFS URI are like that : hdfs://namenodedns:port/user/hdfs/folder/file.csv

Default port is 8020.

Init HDFS FileSystem Object
// ====== Init HDFS File System Object
Configuration conf = new Configuration();
// Set FileSystem URI
conf.set("fs.defaultFS", hdfsuri);
// Because of Maven
conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
// Set HADOOP user
System.setProperty("HADOOP_USER_NAME", "hdfs");
System.setProperty("hadoop.home.dir", "/");
//Get the filesystem - HDFS
FileSystem fs = FileSystem.get(URI.create(hdfsuri), conf);
Init Subfolders
//==== Create folder if not exists
Path workingDir=fs.getWorkingDirectory();
Path newFolderPath= new Path(path);
if(!fs.exists(newFolderPath)) {
   // Create new Directory
   fs.mkdirs(newFolderPath);
   logger.info("Path "+path+" created.");
}
How to write a file to HDFS with Java?
Code example
//==== Write file
logger.info("Begin Write file into hdfs");
//Create a path
Path hdfswritepath = new Path(newFolderPath + "/" + fileName);
//Init output stream
FSDataOutputStream outputStream=fs.create(hdfswritepath);
//Cassical output stream usage
outputStream.writeBytes(fileContent);
outputStream.close();
logger.info("End Write file into hdfs");
How to read a file from HDFS with Java?
Code example
//==== Read file
logger.info("Read file from hdfs");
//Create a path
Path hdfsreadpath = new Path(newFolderPath + "/" + fileName);
//Init input stream
FSDataInputStream inputStream = fs.open(hdfsreadpath);
//Classical input stream usage
String out= IOUtils.toString(inputStream, "UTF-8");
logger.info(out);
inputStream.close();
fs.close();
