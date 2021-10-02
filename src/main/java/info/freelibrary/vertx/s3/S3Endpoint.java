
package info.freelibrary.vertx.s3;

import info.freelibrary.util.StringUtils;

/**
 * An AWS S3 endpoint.
 */
public enum S3Endpoint implements Endpoint {

    US_EAST_2("us-east-2", "US East (Ohio)"), //
    US_EAST_1("us-east-1", "US East (N. Virginia)"), //
    US_WEST_1("us-west-1", "US West (N. California)"), //
    US_WEST_2("us-west-2", "US West (Oregon)"), //
    AF_SOUTH_1("af-south-1", "Africa (Cape Town)"), //
    AP_EAST_1("ap-east-1", "Asia Pacific (Hong Kong)"), //
    AP_SOUTH_1("ap-south-1", "Asia Pacific (Mumbai)"), //
    AP_NORTHEAST_3("ap-northeast-3", "Asia Pacific (Osaka-Local)"), //
    AP_NORTHEAST_2("ap-northeast-2", "Asia Pacific (Seoul)"), //
    AP_SOUTHEAST_1("ap-southeast-1", "Asia Pacific (Singapore)"), //
    AP_SOUTHEAST_2("ap-southeast-2", "Asia Pacific (Sydney)"), //
    AP_NORTHEAST_1("ap-northeast-1", "Asia Pacific (Tokyo)"), //
    CA_CENTRAL_1("ca-central-1", "Canada (Central)"), //
    CN_NORTH_1("cn-north-1", "China (Beijing)"), //
    CN_NORTHWEST_1("cn-northwest-1", "China (Ningxia)"), //
    EU_CENTRAL_1("eu-central-1", "Europe (Frankfurt)"), //
    EU_WEST_1("eu-west-1", "Europe (Ireland)"), //
    EU_WEST_2("eu-west-2", "Europe (London)"), //
    EU_SOUTH_1("eu-south-1", "Europe (Milan)"), //
    EU_WEST_3("eu-west-3", "Europe (Paris)"), //
    EU_NORTH_1("eu-north-1", "Europe (Stockholm)"), //
    SA_EAST_1("sa-east-1", "South America (São Paulo)"), //
    ME_SOUTH_1("me-south-1", "Middle East (Bahrain)"), //
    US_GOV_EAST_1("us-gov-east-1", "AWS GovCloud (US-East)"), //
    US_GOV_WEST_1("us-gov-west-1", "AWS GovCloud (US)");

    /**
     * The S3 host pattern.
     */
    private static final String HOST_PATTERN = "s3.{}.amazonaws.com";

    /**
     * The S3 endpoint pattern.
     */
    private static final String ENDPOINT_PATTERN = "https://" + HOST_PATTERN;

    /**
     * The S3 dual stack endpoint pattern.
     */
    private static final String DUALSTACK_ENDPOINT_PATTERN = "https://s3.dualstack.{}.amazonaws.com";

    /**
     * An S3 region.
     */
    private final String myRegion;

    /**
     * An S3 region name.
     */
    private final String myRegionName;

    /**
     * Creates a new S3 endpoint.
     *
     * @param aRegion An endpoint region
     * @param aRegionName A human-friendly endpoint name.
     */
    S3Endpoint(final String aRegion, final String aRegionName) {
        myRegion = aRegion;
        myRegionName = aRegionName;
    }

    @Override
    public String toString() {
        return StringUtils.format(ENDPOINT_PATTERN, myRegion);
    }

    @Override
    public String getDualStack() {
        return StringUtils.format(DUALSTACK_ENDPOINT_PATTERN, myRegion);
    }

    @Override
    public String getRegion() {
        return myRegion;
    }

    @Override
    public String getHost() {
        return StringUtils.format(HOST_PATTERN, myRegion);
    }

    @Override
    public int getPort() {
        return 443;
    }

    @Override
    public String getLabel() {
        return myRegionName;
    }
}
