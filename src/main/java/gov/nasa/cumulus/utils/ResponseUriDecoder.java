package gov.nasa.cumulus.utils;

import cumulus_message_adapter.message_parser.AdapterLogger;
import gov.nasa.cumulus.bo.ExtraFileFields;
import org.apache.commons.lang3.StringUtils;

public class ResponseUriDecoder {
    String className = this.getClass().getName();

    public ExtraFileFields process(String uri, String distribution_endpoint) {
        uri = StringUtils.trim(uri);
        distribution_endpoint =  StringUtils.trim(distribution_endpoint);
        AdapterLogger.LogInfo(this.className + " uri: " + uri + " distribution_endpoint: " + distribution_endpoint);
        ExtraFileFields extra = new ExtraFileFields();
        // Make sure distribution_endpoint ends with slash, the next step removeIgnoreCase will output
        //  bucket_name/folder_name/xxxx.nc
        if(!StringUtils.endsWith(distribution_endpoint, "/")) {
            distribution_endpoint = distribution_endpoint + "/";
        }
        String bucketFolderFilename = StringUtils.removeIgnoreCase(uri, distribution_endpoint);
        extra.setFilename("s3://" + bucketFolderFilename);
        if(StringUtils.startsWith(bucketFolderFilename, "/"))
            bucketFolderFilename = StringUtils.substring(bucketFolderFilename, 1); // remove first character
        String[] array = StringUtils.splitByWholeSeparator(bucketFolderFilename, "/");
        extra.setBucket(array[0]);
        if(array.length >= 3) { // uri includes bucket, folder and filename at least
            extra.setFilepath(bucketFolderFilename.substring(bucketFolderFilename.indexOf("/") + 1, bucketFolderFilename.lastIndexOf("/")));
        } else {
            extra.setFilepath("");
        }
        return extra;
    }
}
