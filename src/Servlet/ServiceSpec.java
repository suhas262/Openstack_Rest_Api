package Servlet;

public class ServiceSpec {
    private String name;
    private String flavorId;
    private String imageId;
    private String networkId;

    public ServiceSpec(String name, String flavorId, String imageId, String networkId) {
        this.name = name;
        this.flavorId = flavorId;
        this.imageId = imageId;
        this.networkId = networkId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFlavorId() {
        return flavorId;
    }

    public void setFlavorId(String flavorId) {
        this.flavorId = flavorId;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }
}
