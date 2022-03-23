package gov.nasa.cumulus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Set;

/**
 * Unit test for CnmToGranuleHandler.
 */
public class CnmToGranuleHandlerTest
        extends TestCase {
    private final String CUMULUS_SCHEMA_FILE_NAME = "cumulus10.1.2-files-schema.json";
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public CnmToGranuleHandlerTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(CnmToGranuleHandlerTest.class);
    }

    /**
     * Test PerformFunction
     */
    public void testPerformFunction() {
        ClassLoader classLoader = getClass().getClassLoader();
        File inputJsonFile = new File(classLoader.getResource("input.json").getFile());

        String input = "";
        try {
            input = new String(Files.readAllBytes(inputJsonFile.toPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        JsonObject inputJson = JsonParser.parseString(input).getAsJsonObject();
        CnmToGranuleHandler c = new CnmToGranuleHandler();
        try {
            String output = c.PerformFunction(input, null);
            System.out.println(output);

            JsonElement jelement = JsonParser.parseString(output);
            JsonObject outputKey = jelement.getAsJsonObject().get("output").getAsJsonObject();
            JsonObject granule = outputKey.getAsJsonArray("granules").get(0).getAsJsonObject().getAsJsonObject();
    
            assert granule.has("version");
            assert granule.get("version").getAsString().equals(inputJson.getAsJsonObject("config")
                    .getAsJsonObject("collection")
                    .get("version")
                    .getAsString());
    
            assert granule.getAsJsonObject().has("dataType");
            assert granule.get("dataType").getAsString().equals(inputJson.getAsJsonObject("config")
                    .getAsJsonObject("collection")
                    .get("name")
                    .getAsString());
							
            assertEquals("product_0001-of-0019", granule.get("granuleId").getAsString());
            JsonArray files = granule.get("files").getAsJsonArray();
            assertEquals(2, files.size());
            for (JsonElement file: files) {
                JsonObject fileObj = file.getAsJsonObject();
                if (fileObj.get("fileName").getAsString().equals("product_0001-of-0019.h5")) {
                    assertEquals("data", fileObj.get("type").getAsString());
                    assertEquals("md5", fileObj.get("checksumType").getAsString());
                    assertEquals("123454321abc", fileObj.get("checksum").getAsString());
                    assertEquals(96772640, fileObj.get("size").getAsInt());
                    assertEquals("L2_HR_LAKE_AVG/product_0001-of-0019.h5", fileObj.get("key").getAsString());
                } else if(fileObj.get("fileName").getAsString().equals("product_0001-of-0019.h5.iso.xml")) {
                    assertEquals("metadata", fileObj.get("type").getAsString());
                    assertFalse(fileObj.has("checksumType"));
                    assertFalse(fileObj.has("checksum"));
                    assertEquals(141205, fileObj.get("size").getAsInt());
                    assertEquals("L2_HR_LAKE_AVG/product_0001-of-0019.h5.iso.xml", fileObj.get("key").getAsString());
                }
                assertEquals("podaac-dev-cumulus-test-input", fileObj.get("bucket").getAsString());
                assertEquals("podaac-dev-cumulus-test-input", fileObj.get("source_bucket").getAsString());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
	
	/**
	 * Test that PerformFunction functions as expected when provided a CNM with filegroups
	 */
	public void testPerformFunctionFilegroups() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		File inputJsonFile = new File(classLoader.getResource("inputFilegroups.json").getFile());
		File expectedJsonFile = new File(classLoader.getResource("expectedFilegroups.json").getFile());
		
		String input = new String(Files.readAllBytes(inputJsonFile.toPath()));
		String expected = new String(Files.readAllBytes(expectedJsonFile.toPath()));
		JsonObject expectedJson = JsonParser.parseString(expected).getAsJsonObject();
		
		CnmToGranuleHandler cnmToGranuleHandler = new CnmToGranuleHandler();
		String output = cnmToGranuleHandler.PerformFunction(input, null);
		JsonObject outputJson = JsonParser.parseString(output).getAsJsonObject();
		
		assert(expectedJson.equals(outputJson.getAsJsonObject("output")));
    }


    public void testBuildHttpsGranuleFile() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File inputJsonFile = new File(classLoader.getResource("https_input.json").getFile());
        File expectedJsonFile = new File(classLoader.getResource("https_output.json").getFile());

        String input = new String(Files.readAllBytes(inputJsonFile.toPath()));
        String expected = new String(Files.readAllBytes(expectedJsonFile.toPath()));
        JsonObject expectedJson =  JsonParser.parseString(expected).getAsJsonObject();

        CnmToGranuleHandler cnmToGranuleHandler = new CnmToGranuleHandler();
        String output = cnmToGranuleHandler.PerformFunction(input, null);
        JsonObject outputJson =  JsonParser.parseString(output).getAsJsonObject();

        assert(expectedJson.equals(outputJson.getAsJsonObject("output")));
    }

    public void testBuildSftpGranuleFile() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        File inputJsonFile = new File(classLoader.getResource("sftp_input.json").getFile());
        File expectedJsonFile = new File(classLoader.getResource("sftp_output.json").getFile());

        String input = new String(Files.readAllBytes(inputJsonFile.toPath()));
        String expected = new String(Files.readAllBytes(expectedJsonFile.toPath()));
        JsonObject expectedJson = JsonParser.parseString(expected).getAsJsonObject();

        CnmToGranuleHandler cnmToGranuleHandler = new CnmToGranuleHandler();
        String output = cnmToGranuleHandler.PerformFunction(input, null);

        JsonObject outputJson = JsonParser.parseString(output).getAsJsonObject();


        JsonArray filesJson = outputJson.get("output").getAsJsonObject().get("granules").getAsJsonArray().get(0)
                .getAsJsonObject().get("files").getAsJsonArray();
        /**
         * Output validation
         * Use  com.networknt   jsonschemavalidator library
         * Need to pass in the files array as string to newly added validation function.
         * If the returned ValidationMessage has error related to source_bucket then let it pass since this field is necessary for S6 ingest 
         */
        Set<ValidationMessage> errors = validateJsonSchema(filesJson.toString());
        for (Iterator<ValidationMessage> it = errors.iterator(); it.hasNext(); ) {
            ValidationMessage message = it.next();
            String[] arguments = message.getArguments();
            if(!StringUtils.startsWithIgnoreCase(arguments[0], "source_bucket")) {
                fail("Unexpected output json validation error");
            }
        }
        assert(expectedJson.equals(outputJson.getAsJsonObject("output")));
    }


    public Set<ValidationMessage> validateJsonSchema(String jsonString) throws  Exception {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(CUMULUS_SCHEMA_FILE_NAME);
        JsonSchema schema =  factory.getSchema(is);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(jsonString);
        Set<ValidationMessage> errors = schema.validate(node);
        return errors;
    }
}
