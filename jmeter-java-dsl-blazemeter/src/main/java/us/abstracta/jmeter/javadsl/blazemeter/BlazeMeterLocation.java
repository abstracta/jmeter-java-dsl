package us.abstracta.jmeter.javadsl.blazemeter;

/*
list of locations from https://api.blazemeter.com/performance/#performance-locations
using as name the original cloud id, and adding as alias the physical location name
*/
public enum BlazeMeterLocation {
  AWS_AP_NORTHEAST_1("ap-northeast-1"), AWS_TOKYO(AWS_AP_NORTHEAST_1.name),
  AWS_AP_NORTHEAST_2("ap-northeast-2"), AWS_SEOUL(AWS_AP_NORTHEAST_2.name),
  AWS_AP_SOUTH_1("ap-south-1"), AWS_MUMBAI(AWS_AP_SOUTH_1.name),
  AWS_AP_SOUTHEAST_1("ap-southeast-1"), AWS_SINGAPORE(AWS_AP_SOUTHEAST_1.name),
  AWS_AP_SOUTHEAST_2("ap-southeast-2"), AWS_AUSTRALIA(AWS_AP_SOUTHEAST_2.name),
  AWS_CA_CENTRAL_1("ca-central-1"), AWS_CANADA_CENTRAL(AWS_CA_CENTRAL_1.name),
  AWS_EU_CENTRAL("eu-central-1"), AWS_FRANKFURT(AWS_EU_CENTRAL.name),
  AWS_EU_WEST_1("eu-west-1"), AWS_IRELAND(AWS_EU_WEST_1.name),
  AWS_EU_WEST_2("eu-west-2"), AWS_LONDON(AWS_EU_WEST_2.name),
  AWS_EU_WEST_3("eu-west-3"), AWS_PARIS(AWS_EU_WEST_3.name),
  AWS_SA_EAST_1("sa-east-1"), AWS_SAO_PAULO(AWS_SA_EAST_1.name),
  AWS_US_EAST_1("us-east-1"), AWS_NORTHERN_VIRGINIA(AWS_US_EAST_1.name),
  AWS_US_EAST_2("us-east-2"), AWS_OHIO(AWS_US_EAST_2.name),
  AWS_US_WEST_1("us-west-1"), AWS_NORTHERN_CALIFORNIA(AWS_US_WEST_1.name),
  AWS_US_WEST_2("us-west-2"), AWS_OREGON(AWS_US_WEST_2.name),
  AZURE_KOREA_CENTRAL("azure-ap-northeast-1"), AZURE_SEOUL(AZURE_KOREA_CENTRAL.name),
  AZURE_KOREA_SOUTH("azure-ap-northeast-2"), AZURE_BUSAN(AZURE_KOREA_SOUTH.name),
  AZURE_BRAZIL_SOUTH("azure-brazil-south-1"), AZURE_SAO_PAULO(AZURE_BRAZIL_SOUTH.name),
  AZURE_CENTRAL_INDIA("azure-central-asia-1"), AZURE_PUNE(AZURE_CENTRAL_INDIA.name),
  AZURE_WEST_INDIA("azure-central-asia-2"), AZURE_MUMBAI(AZURE_WEST_INDIA.name),
  AZURE_SOUTH_INDIA("azure-central-asia-3"), AZURE_CHENNAI(AZURE_SOUTH_INDIA.name),
  AZURE_CANADA_CENTRAL("azure-central-ca"), AZURE_TORONTO(AZURE_CANADA_CENTRAL.name),
  AZURE_CENTRAL_US("azure-central-us-1"), AZURE_IOWA(AZURE_CENTRAL_US.name),
  AZURE_EAST_ASIA("azure-east-asia-1"), AZURE_HONG_KONG(AZURE_EAST_ASIA.name),
  AZURE_AUSTRALIA_EAST("azure-east-au-1"), AZURE_NEW_SOUTH_WALES(AZURE_AUSTRALIA_EAST.name),
  AZURE_CANADA_EAST("azure-east-ca"), AZURE_QUEBEC(AZURE_CANADA_EAST.name),
  AZURE_US_EAST("azure-east-us-1"), AZURE_VIRGINIA(AZURE_US_EAST.name),
  AZURE_US_EAST_2("azure-east-us-2"), AZURE_VIRGINIA_2(AZURE_US_EAST_2.name),
  AZURE_UK_SOUTH("azure-eu-west-2"), AZURE_LONDON(AZURE_UK_SOUTH.name),
  AZURE_UK_WEST("azure-eu-west-3"), AZURE_CARDIFF(AZURE_UK_WEST.name),
  AZURE_JAPAN_EAST("azure-japan-east-1"), AZURE_TOKYO(AZURE_JAPAN_EAST.name),
  AZURE_JAPAN_WEST("azure-japan-west-1"), AZURE_OSAKA(AZURE_JAPAN_WEST.name),
  AZURE_NORTH_CENTRAL_US("azure-north-central-us-1"), AZURE_ILLINOIS(AZURE_NORTH_CENTRAL_US.name),
  AZURE_NORTH_EUROPE("azure-north-europe-1"), AZURE_IRELAND(AZURE_NORTH_EUROPE.name),
  AZURE_SOUTH_CENTRAL_US_STG("azure-south-central-us-1"),
  AZURE_TEXAS(AZURE_SOUTH_CENTRAL_US_STG.name),
  AZURE_SOUTHEAST_ASIA("azure-southeast-asia-1"), AZURE_SINGAPORE(AZURE_SOUTHEAST_ASIA.name),
  AZURE_AUSTRALIA_SOUTHEAST("azure-southeast-au-1"),
  AZURE_VICTORIA(AZURE_AUSTRALIA_SOUTHEAST.name),
  AZURE_WEST_CENTRAL_US("azure-us-west-central"), AZURE_WYOMING(AZURE_WEST_CENTRAL_US.name),
  AZURE_WEST_EUROPE("azure-west-europe-1"), AZURE_NETHERLANDS(AZURE_WEST_EUROPE.name),
  AZURE_WEST_US("azure-west-us-1"), AZURE_CALIFORNIA(AZURE_WEST_US.name),
  AZURE_WEST_US_2("azure-west-us-2"), AZURE_WASHINGTON(AZURE_WEST_US_2.name),
  GCP_ASIA_EAST_1("asia-east1-a"), GCP_TAIWAN(GCP_ASIA_EAST_1.name),
  GCP_ASIA_NORTHEAST_1("asia-northeast1-a"), GCP_TOKYO(GCP_ASIA_NORTHEAST_1.name),
  GCP_ASIA_NORTHEAST_2("asia-northeast2-a"), GCP_OSAKA(GCP_ASIA_NORTHEAST_2.name),
  GCP_ASIA_SOUTH_1("asia-south1-a"), GCP_MUMBAI(GCP_ASIA_SOUTH_1.name),
  GCP_ASIA_SOUTHEAST_1("asia-southeast1-a"), GCP_SINGAPORE(GCP_ASIA_SOUTHEAST_1.name),
  GCP_AUSTRALIA_SOUTHEAST_1("australia-southeast1-a"), GCP_SYDNEY(GCP_AUSTRALIA_SOUTHEAST_1.name),
  GCP_EUROPE_WEST_1("europe-west1-b"), GCP_BELGIUM(GCP_EUROPE_WEST_1.name),
  GCP_EUROPE_WEST_2("europe-west2-a"), GCP_LONDON(GCP_EUROPE_WEST_2.name),
  GCP_EUROPE_WEST_3("europe-west3-a"), GCP_FRANKFURT(GCP_EUROPE_WEST_3.name),
  GCP_EUROPE_WEST_4("europe-west4-b"), GCP_NETHERLANDS(GCP_EUROPE_WEST_4.name),
  GCP_NORTH_AMERICA_NORTHEAST_1("northamerica-northeast1-a"),
  GCP_MONTREAL(GCP_NORTH_AMERICA_NORTHEAST_1.name),
  GCP_SOUTH_AMERICA_EAST_1("southamerica-east1-a"), GCP_SAO_PAULO(GCP_SOUTH_AMERICA_EAST_1.name),
  GCP_US_CENTRAL_1("us-central1-a"), GCP_IOWA(GCP_US_CENTRAL_1.name),
  GCP_US_EAST_1("us-east1-b"), GCP_SOUTH_CAROLINA(GCP_US_EAST_1.name),
  GCP_US_EAST_4("us-east4-a"), GCP_NORTHERN_VIRGINIA(GCP_US_EAST_4.name),
  GCP_US_WEST_1("us-west1-a"), GCP_OREGON(GCP_US_WEST_1.name),
  GCP_US_WEST_2("us-west2-a"), GCP_LOS_ANGELES(GCP_US_WEST_2.name);

  private final String name;

  BlazeMeterLocation(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

}
