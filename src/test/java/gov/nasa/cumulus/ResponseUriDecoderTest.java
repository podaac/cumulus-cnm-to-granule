package gov.nasa.cumulus;

import gov.nasa.cumulus.bo.ExtraFileFields;
import gov.nasa.cumulus.utils.ResponseUriDecoder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResponseUriDecoderTest {

    @Test
    public void processTest() {
        ResponseUriDecoder reponseUriDecoder = new ResponseUriDecoder();
        ExtraFileFields extra = reponseUriDecoder.process("https://jh72u371y2.execute-api.us-west-2.amazonaws.com:9000/DEV/dyen-cumulus-protected/JASON-1_L2_OST_GPN_E/JA1_GPN_2PeP001_003_20020115_070317_20020115_075921.nc",
                "https://jh72u371y2.execute-api.us-west-2.amazonaws.com:9000/DEV/");
        assertEquals("s3://dyen-cumulus-protected/JASON-1_L2_OST_GPN_E/JA1_GPN_2PeP001_003_20020115_070317_20020115_075921.nc", extra.getFilename());
        assertEquals("dyen-cumulus-protected", extra.getBucket());
        assertEquals("JASON-1_L2_OST_GPN_E", extra.getFilepath());

        // test case when distribution_endpoint is NOT ending with slash
        ExtraFileFields extra2 = reponseUriDecoder.process("https://jh72u371y2.execute-api.us-west-2.amazonaws.com:9000/DEV/dyen-cumulus-protected/JASON-1_L2_OST_GPN_E/JA1_GPN_2PeP001_003_20020115_070317_20020115_075921.nc",
                "https://jh72u371y2.execute-api.us-west-2.amazonaws.com:9000/DEV");
        assertEquals("s3://dyen-cumulus-protected/JASON-1_L2_OST_GPN_E/JA1_GPN_2PeP001_003_20020115_070317_20020115_075921.nc", extra2.getFilename());
        assertEquals("dyen-cumulus-protected", extra2.getBucket());
        assertEquals("JASON-1_L2_OST_GPN_E", extra2.getFilepath());
    }
}
