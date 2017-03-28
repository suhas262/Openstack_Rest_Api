package Servlet;

import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.SimpleTenantUsage;
import org.openstack4j.model.identity.Tenant;
import org.openstack4j.model.identity.User;
import org.openstack4j.model.image.Image;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.Subnet;

import java.util.List;

public interface OpenStackInterface extends AutoCloseable {

    Network getNetworkByName(String net1);

    //server
    public Server startVM(ServiceSpec serviceSpec);

    /*void deleteServers(Service service);*/

    public List<? extends Flavor> getFlavors();
    public Flavor getFlavorByName(String name);
    Server getServerByName(String vmName);
    List<? extends Server> getAllServers();


    //tenant
    public Tenant createTenant(String name, String description);
    public List<? extends Tenant> getAllTenants();
    public SimpleTenantUsage getQuotaForTenant();
    public User createUser(String name, String password, String emailId);
    public Tenant getTenantByName();

    //network
    public Network createNetwork(String name);
    public List<? extends Network> getAllNetworks();
    public Subnet createSubnet(String name, String networkId, String tenantId, String startIpPool, String endIpPool, String cidr);
    public List<? extends Subnet> getAllSubnets();
    //public Router createRouter(String name, String networkId, )

    // Image
    public Image getImageByName(String name);

    void deleteNetwork(Network network);


    //void getAllImages();
}
