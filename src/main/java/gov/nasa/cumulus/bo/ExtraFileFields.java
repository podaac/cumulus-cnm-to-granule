package gov.nasa.cumulus.bo;

public class ExtraFileFields {
    String bucket;
    String filename;
    String filepath;

    /**
     * S3 bucket
     *
     * @return S3 bucket
     */
    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    /**
     * Full S3 URI to file
     *
     * @return full S3 URI to file
     */
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * S3 directories to file
     *
     * @return S3 directories to file excluding bucket and filename
     */
    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }
}
