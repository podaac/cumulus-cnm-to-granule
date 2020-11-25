package gov.nasa.cumulus;

import gov.nasa.cumulus.gov.nasa.cumulus.bo.ExtraFileFileds;
import gov.nasa.cumulus.utils.ResponseUriDecoder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResponseUriDecoderTest {

    @Test
    public void processTest() {
        ResponseUriDecoder reponseUriDecoder = new ResponseUriDecoder();
        ExtraFileFileds extra = reponseUriDecoder.process("https://jh72u371y2.execute-api.us-west-2.amazonaws.com:9000/DEV/dyen-cumulus-protected/JASON-1_L2_OST_GPN_E/JA1_GPN_2PeP001_003_20020115_070317_20020115_075921.nc",
                "https://jh72u371y2.execute-api.us-west-2.amazonaws.com:9000/DEV/");
        assertEquals(extra.getFilename(),"s3://dyen-cumulus-protected/JASON-1_L2_OST_GPN_E/JA1_GPN_2PeP001_003_20020115_070317_20020115_075921.nc");
        assertEquals(extra.getBucket(),"dyen-cumulus-protected");

        // test case when distribution_endpoint is NOT ending with slash
        ExtraFileFileds extra2 = reponseUriDecoder.process("https://jh72u371y2.execute-api.us-west-2.amazonaws.com:9000/DEV/dyen-cumulus-protected/JASON-1_L2_OST_GPN_E/JA1_GPN_2PeP001_003_20020115_070317_20020115_075921.nc",
                "https://jh72u371y2.execute-api.us-west-2.amazonaws.com:9000/DEV");
        assertEquals(extra.getFilename(),"s3://dyen-cumulus-protected/JASON-1_L2_OST_GPN_E/JA1_GPN_2PeP001_003_20020115_070317_20020115_075921.nc");
        assertEquals(extra2.getBucket(),"dyen-cumulus-protected");
    }
}
