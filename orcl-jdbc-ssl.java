 /*

You need first truststore with MYCA

 

THE BELLOW CODE ALLOWED ME TO CONNECT TO EXA22 WITH THIN DRIVER AND TCPS

 

C:\Program Files (x86)\Java\jdk1.8.0_45\jre\bin>keytool -import -alias myca -file c:\temp\myca.crt -storetype JKS -keystore c:\temp\myca.truststore

[...]


C:\Program Files (x86)\Java\jdk1.8.0_45\jre\bin>

 

It can be executed in verbose mode with the following command

 

java -Djavax.net.debug=all -jar dbconnect.jar

 

Output result:

 

url = jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=tcps)(HOST=mydbserver)(PORT=1501))(CONNECT_DATA=(SERVICE_NAME=myservice)))

Thin :exa22-scan2.swift.corp:1501/wdwdev_ssl.swift.com: Connected with user test

select sys_context('USERENV','NETWORK_PROTOCOL') as protocol from dual

used protocol = tcps

*/


 

import java.io.IOException;

import java.sql.Connection;

import java.sql.DriverManager;

import java.sql.ResultSet;

import java.sql.Statement;

import java.util.Properties;

 

import javax.net.ssl.SSLServerSocket;

import javax.net.ssl.SSLServerSocketFactory;

 

 

public class DBConnect

{

 

               //---------------------------------------------------------

 

               public static void main(String[] args)

               {

                              /* this will list protocols & ciphers ... very useful to set the props

                              // Get the SSLServerSocket

                              SSLServerSocketFactory ssl;

                              SSLServerSocket sslServerSocket;

                              ssl = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

                              try {

                                             sslServerSocket = (SSLServerSocket) ssl.createServerSocket();

              

                                             // Get the list of all supported cipher suites.

                                             String[] cipherSuites = sslServerSocket.getSupportedCipherSuites();

                                             for (String suite : cipherSuites)

                                               System.out.println(suite);

 

                                             // Get the list of all supported protocols.

                                             String[] protocols = sslServerSocket.getSupportedProtocols();

                                             for (String protocol : protocols)

                                               System.out.println(protocol);

 

                              } catch (IOException e) {

                                             // TODO Auto-generated catch block

                                             e.printStackTrace();

                              }

 

               */

                              DBConnect.connectDB("mydbserver","mydbservice","1501","test","Abcd1234");

               }

 

               //---------------------------------------------------------

              

               public static void connectDB (String server,String service,String port,String user,String pwd)

               {

                              try

                              {

                                             Class.forName("oracle.jdbc.driver.OracleDriver");

                                             /*

                                             String connectionString = "jdbc:oracle:thin:@"+server+":"+port+"/"+service;

                                             Connection con = DriverManager.getConnection(connectionString, user, pwd);

                                             */

                                            

                                              String url = "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=tcps)(HOST="+server+")(PORT="+port+"))(CONNECT_DATA=(SERVICE_NAME="+service+")))";

                                             System.out.println ("url = "+url);

                                             Properties props = new Properties();

                                             props.setProperty("user", "test");

                                             props.setProperty("password", "Abcd1234");

                                             props.setProperty("javax.net.ssl.trustStore", "c:\\temp\\myca.truststore");

                                             props.setProperty("javax.net.ssl.trustStoreType","JKS");

                                             props.setProperty("javax.net.ssl.trustStorePassword","Abcd1234");

                                             props.setProperty("oracle.net.ssl_client_authentication", "false");

                                             props.setProperty("oracle.net.ssl_version","TLSv1.2");

                                             props.setProperty("oracle.net.ssl_cipher_suites","(TLS_RSA_WITH_AES_256_GCM_SHA384)");

                                             Connection con = DriverManager.getConnection(url, props);

                                            

                                              

                                              System.out.println ("Thin :"+server+":"+port+"/"+service+": Connected with user "+user);

                                            

                                              String query= "select sys_context('USERENV','NETWORK_PROTOCOL') as protocol from dual";                                                                                  

                                              Statement stmt = con.createStatement();

                                             System.out.println (query);

                                             ResultSet rs = stmt.executeQuery(query);                             

                                             if (rs.next())

                                             {

                                                            System.out.println (" used protocol = "+rs.getString(1));

                                             }

                                             rs.close();

                                             stmt.close();

                                             con.close();

                              }

                              catch (Exception e)

                              {

                                             e.printStackTrace();

                              }

 

               }

              

               //---------------------------------------------------------

              

 

}

