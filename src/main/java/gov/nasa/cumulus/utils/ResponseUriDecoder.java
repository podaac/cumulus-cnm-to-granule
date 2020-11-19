package gov.nasa.cumulus.utils;

import cumulus_message_adapter.message_parser.AdapterLogger;
import gov.nasa.cumulus.gov.nasa.cumulus.bo.ExtraFileFileds;
import org.apache.commons.lang3.StringUtils;

public class ResponseUriDecoder {
    String className = this.getClass().getName();

    public ExtraFileFileds process(String uri, String distribution_endpoint) {
        uri = StringUtils.trim(uri);
        distribution_endpoint =  StringUtils.trim(distribution_endpoint);
        AdapterLogger.LogInfo(this.className + " uri: " + uri + " distribution_endpoint: " + distribution_endpoint);
        ExtraFileFileds extra = new ExtraFileFileds();
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
        // To construct the filepath, first remove the bucket name, then removing leading slash by
        // doing a substring from starting index =1
        if(array.length >= 3) { // uri at least includes bucket, folder and filename at least
            extra.setFilepath(StringUtils.substring(StringUtils.removeIgnoreCase(bucketFolderFilename, extra.getBucket()),
                    1));
        }
        return extra;
    }
}
