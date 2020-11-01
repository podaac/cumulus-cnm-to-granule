package gov.nasa.cumulus;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Unit test for CnmToGranuleHandler.
 */
public class CnmToGranuleHandlerTest
        extends TestCase {
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

        CnmToGranuleHandler c = new CnmToGranuleHandler();
        try {
            String output = c.PerformFunction(input, null);
            System.out.println(output);

            JsonElement jelement = new JsonParser().parse(output);
            JsonObject outputKey = jelement.getAsJsonObject().get("output").getAsJsonObject();
            JsonObject granule = outputKey.getAsJsonArray("granules").get(0).getAsJsonObject().getAsJsonObject();
            assertEquals("product_0001-of-0019", granule.get("granuleId").getAsString());
            JsonArray files = granule.get("files").getAsJsonArray();
            assertEquals(2, files.size());
            for (JsonElement file: files) {
                JsonObject fileObj = file.getAsJsonObject();
                if (fileObj.get("name").getAsString().equals("product_0001-of-0019.h5")) {
                    assertEquals("data", fileObj.get("type").getAsString());
                    assertEquals("md5", fileObj.get("checksumType").getAsString());
                    assertEquals("123454321abc", fileObj.get("checksum").getAsString());
                    assertEquals(96772640, fileObj.get("size").getAsInt());
                    assertEquals("s3://podaac-dev-cumulus-test-input/L2_HR_LAKE_AVG/product_0001-of-0019.h5", fileObj.get("url_path").getAsString());
                } else if(fileObj.get("name").getAsString().equals("product_0001-of-0019.h5.iso.xml")) {
                    assertEquals("metadata", fileObj.get("type").getAsString());
                    assertFalse(fileObj.has("checksumType"));
                    assertFalse(fileObj.has("checksum"));
                    assertEquals(141205, fileObj.get("size").getAsInt());
                    assertEquals("s3://podaac-dev-cumulus-test-input/L2_HR_LAKE_AVG/product_0001-of-0019.h5.iso.xml", fileObj.get("url_path").getAsString());
                }
                assertEquals("L2_HR_LAKE_AVG", fileObj.get("path").getAsString());
                assertEquals("podaac-dev-cumulus-test-input", fileObj.get("bucket").getAsString());
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
		JsonObject expectedJson = new JsonParser().parse(expected).getAsJsonObject();
		
		CnmToGranuleHandler cnmToGranuleHandler = new CnmToGranuleHandler();
		String output = cnmToGranuleHandler.PerformFunction(input, null);
		JsonObject outputJson = new JsonParser().parse(output).getAsJsonObject();
		
		assert(expectedJson.equals(outputJson.getAsJsonObject("output")));
    }
}
