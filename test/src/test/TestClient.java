/**
 * 
 */
package test;


import naga.NIOService;
import java.util.Map;

import util.Util;

import clients.ClientThread;


/**
 * @author account
 *
 */
public class TestClient {

	/**
	 * @param args
	 */

	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Hello, world!");
		   try
           {
                   // Parse arguments.
                   String host = "127.0.0.1";
                   
			 //  String host = "test.ios-radio.com";
                  int port = 8081;

                   
                  final String data = "deviceFamily=iPhone%20Simulator&userLocale=uk&region_id=rx0001&version=1.5&id=1&method=radio.getCategories&iso_code=RU&deviceId=c37b2e6fe99536283ca3f658580c4cdd&deviceOS=6.0";
                  
                  Map<String, String> maps =  Util.getQueryMap(data);
                  System.out.println( util.writer.json.JsonWriter.toJSONString(maps));
       
                  
                  //           util.writer.json.JsonWriter.toJSONString(response).getBytes("UTF-8");
/*
                   // Prepare the login packet, packing two UTF strings together
                   // using a data output stream.
                   ByteArrayOutputStream stream = new ByteArrayOutputStream();
                   DataOutputStream dataStream = new DataOutputStream(stream);
                  // dataStream.writeUTF(account);
                 //  dataStream.writeUTF(password);
                   dataStream.writeUTF(data);
                   dataStream.flush();
             //      final byte[] content = stream.toByteArray();
                    
      //            final byte[] content = data.getBytes("UTF-8");
                 
                  dataStream.close();
*/
                   // Start up the service.
                   NIOService service = new NIOService();
                   
                   int i = 1;
                   
                while ( i<=3)	   
                {
                	     ClientThread client = new ClientThread(service, host, port, i); 
                         client.start();
                         System.out.println("Start Client: " + i );
                         i++;
                }
               
           }
           catch (Exception e)
           {
                   e.printStackTrace();
           }
	}

}
