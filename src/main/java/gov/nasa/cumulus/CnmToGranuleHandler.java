package gov.nasa.cumulus;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.util.StringUtils;
import com.google.gson.*;
import cumulus_message_adapter.message_parser.AdapterLogger;
import cumulus_message_adapter.message_parser.ITask;
import cumulus_message_adapter.message_parser.MessageAdapterException;
import cumulus_message_adapter.message_parser.MessageParser;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

public class CnmToGranuleHandler implements ITask, RequestHandler<String, String> {
    String className = this.getClass().getName();

    public String handleRequest(String input, Context context) {
        MessageParser parser = new MessageParser();
        try {
            AdapterLogger.LogDebug(this.className + " handleRequest Input: " + input);
            return parser.RunCumulusTask(input, context, new CnmToGranuleHandler());
        } catch (MessageAdapterException e) {
            AdapterLogger.LogError(this.className + " handleRequest Error: " + e.getMessage());
            return e.getMessage();
        }
    }

    public void handleRequestStreams(InputStream inputStream, OutputStream outputStream, Context context) throws IOException, MessageAdapterException {
        MessageParser parser = new MessageParser();

        String input = IOUtils.toString(inputStream, "UTF-8");
        AdapterLogger.LogDebug(this.className + " Input: " + input);
        String output = parser.RunCumulusTask(input, context, new CnmToGranuleHandler());
        AdapterLogger.LogDebug(this.className + " Output: " + output);
        outputStream.write(output.getBytes(Charset.forName("UTF-8")));
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
        AdapterLogger.LogDebug(this.className + " Entered PerformFunction");

        //convert CNM to GranuleObject
        JsonElement jelement = new JsonParser().parse(input);
        JsonObject inputKey = jelement.getAsJsonObject();

        JsonObject cnmObject = inputKey.getAsJsonObject("input");

        JsonObject granule = new JsonObject();

        // Parse config values
        JsonObject config = inputKey.getAsJsonObject("config");
        String granuleIdExtraction = config.getAsJsonObject("collection").get("granuleIdExtraction").getAsString();

        String granuleId = cnmObject.getAsJsonObject("product").get("name").getAsString();
        granuleId = granuleId.substring(granuleId.indexOf("/") + 1);
        Pattern pattern = Pattern.compile(granuleIdExtraction);
        Matcher matcher = pattern.matcher(granuleId);
        if (matcher.find()) {
            granuleId = matcher.group(1);
        }

        JsonArray files = new JsonArray();
        granule.addProperty("granuleId", granuleId);
        granule.addProperty("version", config.getAsJsonObject("collection").get("version").getAsString());
        granule.addProperty("dataType", config.getAsJsonObject("collection").get("name").getAsString());

        JsonArray inputFiles = cnmObject.getAsJsonObject("product").getAsJsonArray("files");

        // 'files' Json array does not exist. Assume CNM contains filegroups instead
        if (inputFiles == null) {
            inputFiles = new JsonArray();

            // Add files from each list of files in "filegroups"
            StreamSupport.stream(cnmObject.getAsJsonObject("product")
                            .getAsJsonArray("filegroups")
                            .spliterator(), false
                    )
                    .flatMap(fs -> StreamSupport
                            .stream(fs.getAsJsonObject()
                                    .getAsJsonArray("files")
                                    .spliterator(), false
                            )
                    )
                    .forEach(inputFiles::add);
        }

        for (JsonElement file : inputFiles) {
            JsonObject cnmFile = file.getAsJsonObject();
            JsonObject granuleFile = null;
            String uri = StringUtils.trim(cnmFile.get("uri").getAsString());
            if (StringUtils.beginsWithIgnoreCase(uri, "s3://")) {
                granuleFile = buildS3GranuleFile(cnmFile);
            } else if (StringUtils.beginsWithIgnoreCase(uri, "https://") ||
                    StringUtils.beginsWithIgnoreCase(uri, "http://")) {
                granuleFile = buildHttpsGranuleFile(cnmFile);
            } else if (StringUtils.beginsWithIgnoreCase(uri, "sftp://")) {
                granuleFile = buildSftpGranuleFile(cnmFile);
            }
            files.add(granuleFile);
        }

        granule.add("files", files);

        JsonObject output = new JsonObject();
        JsonArray granuleArray = new JsonArray();
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
        AdapterLogger.LogDebug(this.className + " PerformFunction output : " + outp);

        return outp;
    }

    public JsonObject buildS3GranuleFile(JsonObject cnmFile) {
        String uri = StringUtils.trim(cnmFile.get("uri").getAsString());
        AdapterLogger.LogInfo(this.className + " uri: " + uri);
        String path = uri.replace("s3://", "");
        String bucket = path.substring(0, path.indexOf("/"));
        //TODO What if there is no / in the path name?
        String url_path = path.substring(path.indexOf("/") + 1, path.lastIndexOf("/"));

        JsonObject granuleFile = new JsonObject();
        granuleFile.addProperty("name", cnmFile.get("name").getAsString());
        granuleFile.addProperty("path", url_path);
        granuleFile.addProperty("url_path", cnmFile.get("uri").getAsString());
        granuleFile.addProperty("bucket", bucket);
        // PODAAC-3208 - add 'source_bucket' key
        granuleFile.addProperty("source_bucket", bucket);
        granuleFile.addProperty("size", cnmFile.get("size").getAsLong());
        if (cnmFile.has("checksumType")) {
            granuleFile.addProperty("checksumType", cnmFile.get("checksumType").getAsString());
        }
        if (cnmFile.has("checksum")) {
            granuleFile.addProperty("checksum", cnmFile.get("checksum").getAsString());
        }
        granuleFile.addProperty("type", cnmFile.get("type").getAsString());
        granuleFile.addProperty("fileName", cnmFile.get("name").getAsString());
        granuleFile.addProperty("key", url_path);
        return granuleFile;
    }

    public JsonObject buildHttpsGranuleFile(JsonObject cnmFile){
        String uri = StringUtils.trim(cnmFile.get("uri").getAsString());
        AdapterLogger.LogInfo(this.className + " uri: " + uri);
        String path = uri.replace("https://", "");
        path = path.replace("http://", ""); // Work-a-around to working with http links also
        //find the path by getting character from (first / plus 1) to lastIndex of /
        String url_path = path.substring(path.indexOf("/") + 1, path.lastIndexOf("/"));

        JsonObject granuleFile = new JsonObject();
        granuleFile.addProperty("name", cnmFile.get("name").getAsString());//
        granuleFile.addProperty("path", url_path);//
        granuleFile.addProperty("size", cnmFile.get("size").getAsLong());
        if (cnmFile.has("checksumType")) {
            granuleFile.addProperty("checksumType", cnmFile.get("checksumType").getAsString());
        }
        if (cnmFile.has("checksum")) {
            granuleFile.addProperty("checksum", cnmFile.get("checksum").getAsString());
        }
        granuleFile.addProperty("type", cnmFile.get("type").getAsString());
        return granuleFile;
    }

    public JsonObject buildSftpGranuleFile(JsonObject cnmFile){
        String uri = StringUtils.trim(cnmFile.get("uri").getAsString());
        AdapterLogger.LogInfo(this.className + " uri: " + uri);
        String path = uri.replace("sftp://", "");
        String url_path = path.substring(path.indexOf("/") + 1, path.lastIndexOf("/"));

        JsonObject granuleFile = new JsonObject();
        granuleFile.addProperty("name", cnmFile.get("name").getAsString());
        granuleFile.addProperty("path", url_path);
        granuleFile.addProperty("url_path", cnmFile.get("uri").getAsString());
        granuleFile.addProperty("size", cnmFile.get("size").getAsLong());
        if (cnmFile.has("checksumType")) {
            granuleFile.addProperty("checksumType", cnmFile.get("checksumType").getAsString());
        }
        if (cnmFile.has("checksum")) {
            granuleFile.addProperty("checksum", cnmFile.get("checksum").getAsString());
        }
        granuleFile.addProperty("type", cnmFile.get("type").getAsString());
        return granuleFile;
    }
}
