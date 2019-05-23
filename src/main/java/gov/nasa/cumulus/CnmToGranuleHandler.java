package gov.nasa.cumulus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cumulus_message_adapter.message_parser.ITask;
import cumulus_message_adapter.message_parser.MessageAdapterException;
import cumulus_message_adapter.message_parser.MessageParser;


public class CnmToGranuleHandler implements  ITask, RequestHandler<String, String>{

	public static void main( String[] args ) throws Exception
    {
		CnmToGranuleHandler c = new CnmToGranuleHandler();
		
		String input = "{"
			+"\"input\":{"
			 +" \"version\":\"v1.0\","
			 +" \"provider\": \"PODAAC\","
			  +"\"deliveryTime\":\"2018-03-12T16:50:23.458100\","
			  +"\"collection\": \"L2_HR_LAKE_AVG\","
			  +"\"identifier\": \"c5c828ac328c97b5d3d1036d08898b30-12\","
			  +"\"product\":"
			  +"  {"
			  +"    \"name\": \"L2_HR_LAKE_AVG/product_0001-of-0019.h5\","
			  +"    \"dataVersion\": \"1\","
			  +"    \"files\": ["
			  +"      {"
			  +"        \"type\": \"data\","
			  +"        \"uri\": \"s3://podaac-dev-cumulus-test-input/L2_HR_LAKE_AVG/product_0001-of-0019.h5\","
			  +"        \"name\":\"product_0001-of-0019.h5\","
			  +"        \"checksumType\": \"md5\","
			  +"        \"checksum\": \"123454321abc\","
			  +"        \"size\": 96772640"
			  +"      }"
			  +"    ]"
			  +"  }"
			  +"},"
			  + "\"config\": {}"
			  +"}";
		
		String output = c.PerformFunction(input, null);
		System.out.println(output);
    }


	public String handleRequest(String input, Context context) {
		MessageParser parser = new MessageParser();
		try
		{
			return parser.RunCumulusTask(input, context, new CnmToGranuleHandler());
		}
		catch(MessageAdapterException e)
		{
			return e.getMessage();
		}
	}
	
	public void handleRequestStreams(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {

		MessageParser parser = new MessageParser();

		try
		{
			String input =IOUtils.toString(inputStream, "UTF-8");
			context.getLogger().log(input);
			String output = parser.RunCumulusTask(input, context, new CnmToGranuleHandler());
			System.out.println("Output: " + output);
			//return parser.HandleMessage(input, context, new MetadataAggregatorLambda(), null);
			outputStream.write(output.getBytes(Charset.forName("UTF-8")));
		}
		catch(MessageAdapterException e)
		{
			e.printStackTrace();
			outputStream.write(e.getMessage().getBytes(Charset.forName("UTF-8")));
		}

	}

	/*
	 * 
{
  "version":"v1.0",
  "provider": "PODAAC",
  "deliveryTime":"2018-03-12T16:50:23.458100",
  "collection": "L2_HR_LAKE_AVG",
  "identifier": ""c5c828ac328c97b5d3d1036d08898b30-12"",
  "product":
    {
      "name": "L2_HR_LAKE_AVG/product_0001-of-0019.h5",
      "dataVersion": "1",
      "files": [
        {
          "type": "data",
          "uri": "s3://podaac-dev-cumulus-test-input/L2_HR_LAKE_AVG/product_0001-of-0019.h5",
          "name":"product_0001-of-0019.h5",
          "checksumType": "md5",
          "checksum": "123454321abc",
          "size": 96772640
        }
      ]
    }
}
	 */
	
	/*
	 * Payload:
{
    "granules": [
      {
        "granuleId": "20170123045500-JPL-L2P_GHRSST-SSTskin-MODIS_T-D-v02.0-fv01.0",
        "files": [
          {
            "name": "20170123045500-JPL-L2P_GHRSST-SSTskin-MODIS_T-D-v02.0-fv01.0.nc",
            "bucket": "podaac-dev-cumulus-protected",
            "time": 1485129600000,
            "path": "allData/ghrsst/data/GDS2/L2P/MODIS_T/JPL/v2014.0/2017/023/",
            "url_path": "",
	        "type": "data",
            "size": 23304519,
            "checksumType": "md5",
            "checksumValue": "123454321abc"
          }
        ]
      }
    ]
  }
	 * 
	 */
	

	public String PerformFunction(String input, Context context) throws Exception {
		System.out.println("Processing " + input);

		//convert CNM to GranuleObject
		JsonElement jelement = new JsonParser().parse(input);
		JsonObject inputKey = jelement.getAsJsonObject();
		
		JsonObject  cnmObject =inputKey.getAsJsonObject("input");
		
	    //JsonObject  cnmObject = jelement.getAsJsonObject();
		
	    JsonObject granule = new JsonObject();

		// Parse config values
		JsonObject config = inputKey.getAsJsonObject("config");
		String granuleIdExtraction = config.getAsJsonObject("collection").get("granuleIdExtraction").getAsString();

	    String granuleId = cnmObject.getAsJsonObject("product").get("name").getAsString();
	    if(granuleId.contains("/"));
	    	granuleId = granuleId.substring(granuleId.indexOf("/")+1);
        Pattern pattern = Pattern.compile(granuleIdExtraction);
        Matcher matcher = pattern.matcher(granuleId);
        if (matcher.find()) {
            granuleId = matcher.group(1);
        }

	    JsonObject cnmFile = (JsonObject) cnmObject.getAsJsonObject("product").getAsJsonArray("files").get(0);
	    
	    JsonArray files = new JsonArray();
	    granule.addProperty("granuleId", granuleId);
	    
	    
	    String uri = cnmFile.get("uri").getAsString();
	    System.out.println(uri);
	    String path = uri.replace("s3://", "");
	    String bucket = path.substring(0, path.indexOf("/")); 
	    //TODO What if there is no / in the path name?
 	    String url_path = path.substring(path.indexOf("/")+1, path.lastIndexOf("/")); 
	    
	    
	    //iterate here to create multiple files
		JsonObject granuleFile = new JsonObject();
		granuleFile.addProperty("name", cnmFile.get("name").getAsString());
		granuleFile.addProperty("path", url_path);
		granuleFile.addProperty("url_path", cnmFile.get("uri").getAsString());
		granuleFile.addProperty("bucket", bucket);
		granuleFile.addProperty("size", cnmFile.get("size").getAsLong());
		granuleFile.addProperty("checksumType", cnmFile.has("checksumType") ? cnmFile.get("checksumType").getAsString() : "md5");
		granuleFile.addProperty("checksum", cnmFile.get("checksum").getAsString());
		granuleFile.addProperty("type", cnmFile.get("type").getAsString());

		files.add(granuleFile);
		granule.add("files", files);
	    JsonObject output = new JsonObject();
	    JsonArray granuleArray= new JsonArray();
	    granuleArray.add(granule);
	    
	    TimeZone tz = TimeZone.getTimeZone("UTC");
	    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
	    df.setTimeZone(tz);
	    String nowAsISO = df.format(new Date());
	    cnmObject.addProperty("receivedTime", nowAsISO);

	    //We need to write out the original CNM message so that we register it for later use.
	    output.add("cnm", cnmObject);
	    
	    JsonObject granuleOutput = new JsonObject();
	    granuleOutput.add("granules", granuleArray);
	    output.add("output", granuleOutput);
	    
	    String outp = new Gson().toJson(output);
	    System.out.println(outp);
	    
		return outp;
		
	}

}
