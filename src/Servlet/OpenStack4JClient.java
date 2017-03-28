package Servlet;

import com.google.inject.assistedinject.Assisted;

import VO.ServerDetailsVO;

import org.openstack4j.api.Builders;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.exceptions.AuthenticationException;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Server;
import org.openstack4j.model.compute.ServerCreate;
import org.openstack4j.model.compute.SimpleTenantUsage;
import org.openstack4j.model.identity.Tenant;
import org.openstack4j.model.identity.User;
import org.openstack4j.model.image.Image;
import org.openstack4j.model.network.Network;
import org.openstack4j.model.network.Subnet;
import org.openstack4j.openstack.OSFactory;
import org.openstack4j.openstack.compute.domain.NovaServer;
import org.openstack4j.model.compute.*;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class OpenStack4JClient implements OpenStackInterface{

    private static final Logger LOGGER = Logger.getLogger(OpenStack4JClient.class.getName());
    private OSClient os = null;
    private String user = null;
    private String passwd = null;
    private String tenant = null;
    
    public OpenStack4JClient() {
		super();
		// TODO Auto-generated constructor stub
	}

	public OpenStack4JClient(@Assisted("user")String user, @Assisted("passwd")String passwd, @Assisted("tenant")String tenant) {
        os = authenticateUser(user, passwd, tenant);
        this.user = user;
        this.passwd = passwd;
        this.tenant = tenant;
    }

    public static OSClient authenticateUser(String user, String passwd, String tenant) throws AuthenticationException {
    	Identifier domainIdentifier = Identifier.byId("be5650f6433a46e696e4f8425063b80a");

        return OSFactory.builderV3()
                .endpoint("http://127.0.0.1:5000/v3")
                .credentials("admin", "admin_user_secret",Identifier.byName("default"))
                .scopeToProject(Identifier.byName("admin"),Identifier.byName("default"))
                .authenticate();
    }

    /*public static void main(String[] args) {
        testOpenstack();

    }*/

    public VMDetails getOpenstackDetails() {
    	List<String> imasList = new ArrayList<String>();
    	List<String> flavors = new ArrayList<String>();
    	List<String> netList = new ArrayList<String>();
    	VMDetails vmDet = new VMDetails();
    	
        try {
            LOGGER.info("Creating service VM.... ");
            
            OpenStack4JClient client = new OpenStack4JClient("admin", "admin_user_secret", "admin");
            imasList = client.getAllImages();
            flavors = client.getAllFlavors();
            netList = client.getAllNetworkList();
            vmDet.setImasList(imasList);
            vmDet.setFlavorList(flavors);
            vmDet.setNetworkList(netList);
            
            Flavor f = client.getFlavorByName("m1.small");
            Image image = client.getImageByName("my-image");
            Network net = client.getNetworkByName("provider");
            LOGGER.info(MessageFormat.format("Creating VM with flavor {0} and image {1} and network {2}",
                    f.getName(), image.getName(), net.getName()));
           // client.startVM(new ServiceSpec("praveenVM", f.getId(), image.getId(), net.getId()));


        } catch (Exception e) {
            System.out.println("Got error... ");
            e.printStackTrace();
        }
        return vmDet;
    }
    
    @Override
    public Network getNetworkByName(String name) {
        List<? extends Network> networks = getAllNetworks();
        if (networks != null) {
            for (Network network : networks) {
                if (network.getName().equals(name)) {
                    return network;
                }
            }
        }
        return null;
    }

    @Override
    public void close() throws IOException {
    }

    /**
     * Provision a VM and start it.
     *
     * @param serviceSpec
     * @return
     */
    @Override
    public Server startVM(final ServiceSpec serviceSpec) {
        
        ServerCreate sc = Builders.server()
                .name(serviceSpec.getName())
                .flavor(serviceSpec.getFlavorId())
                .image(serviceSpec.getImageId())
                .networks(new ArrayList<String>() {{
                    add(serviceSpec.getNetworkId());
                }})
                .build();
        // Boot the Server
        Server server = os.compute().servers().boot(sc);
        return server;
    }

/*    @Override
    public void deleteServers(Service service) {
        if (service != null) {
            List<Node> nodes = service.getNodes();
            if (nodes != null) {
                for (Node node : nodes) {
                    os.compute().servers().delete(node.getNodeId());
                }
            }
        }
    }*/

    @Override
    public List<? extends Flavor> getFlavors() {
        return os.compute().flavors().list();
    }

    
    @Override
    public Flavor getFlavorByName(String name) {
        List<? extends Flavor> flavors = os.compute().flavors().list();
        for (Flavor f: flavors) {
            if (f.getName().equals(name)) {
                return f;
            }
        }
        return null;
    }
    
    
    public List<String> getAllFlavors(){
    	 List<? extends Flavor> flavors = os.compute().flavors().list();
    	List<String> flavorList = new ArrayList<String>();
    	for (Flavor fla: flavors) {
    		flavorList.add(fla.getName());
    	}
    	return flavorList;
    }
    
    public List<String> getAllNetworkList(){
    	List<? extends Network> networks = os.networking().network().list();
    	List<String> netList = new ArrayList<String>();
    	for (Network net: networks) {
    		netList.add(net.getName());
    	}
    	return netList;
    }

    public List<String> getAllImages(){
    	List<? extends Image> images = os.images().list();
    	List<String> imageList = new ArrayList<String>();
    	for (Image ima: images) {
    		imageList.add(ima.getName());
    	}
    	return imageList;
    }



    @Override
    public Tenant createTenant(String name, String description) {
        Tenant tenant = os.identity().tenants()
                .create(new Builders().tenant().name(name).description(description).build());
        return tenant;
    }

    @Override
    public List<? extends Tenant> getAllTenants() {
        return os.identity().tenants().list();
    }

    @Override
    public SimpleTenantUsage getQuotaForTenant() {
        return os.compute().quotaSets().getTenantUsage(tenant);
    }

    @Override
    public User createUser(String name, String password, String emailId) {
        Tenant t = getTenantByName();
        User user = os.identity().users()
                .create(new Builders().user()
                        .name(name)
                        .password(password)
                        .email(emailId)
                        .tenant(t).build());

        return user;

    }

    @Override
    public Tenant getTenantByName() {
        return os.identity().tenants().getByName(tenant);
    }

    @Override
    public Network createNetwork(String name) {
        Tenant tenant = getTenantByName();
        Network network = os.networking().network()
                .create(Builders.network().name(name).tenantId(tenant.getId()).build());
        return network;
    }

    @Override
    public List<? extends Network> getAllNetworks() {
        return os.networking().network().list();
    }

    @Override
    public Subnet createSubnet(String name, String networkId, String tenantId, String startIpPool, String endIpPool, String cidr) {
        return null;
    }

    @Override
    public List<? extends Subnet> getAllSubnets() {
        return null;
    }

    @Override
    public Image getImageByName(String name) {
        List<? extends Image> images = os.images().list();
        for (Image image: images) {
            if (image.getName().equals(name)) {
                return image;
            }
        }
        return null;
    }

    @Override
    public void deleteNetwork(Network network) {
        os.networking().network().delete(network.getId());
    }

    @Override
    public Server getServerByName(String vmName) {
        List<? extends Server> servers = getAllServers();
        if (servers != null) {
            for(Server s: servers) {
                if(s.getName().equalsIgnoreCase(vmName)) {
                    return s;
                }
            }
        }
        return null;
    }

    @Override
    public List<? extends Server> getAllServers() {
        List<? extends Server> servers = os.compute().servers().list();
        return servers;
    }
    
    public List<ServerDetailsVO> getServerList(){
    	List<? extends Server> servers = getAllServers();
    	List<ServerDetailsVO> serverList = new ArrayList<ServerDetailsVO>();
    	for (Server server: servers) {
    		ServerDetailsVO serverDetail = new ServerDetailsVO();
    		serverDetail.setServerId(server.getId());
    		serverDetail.setServerVMName(server.getName());
    		serverDetail.setInstanceName(server.getInstanceName());
    		serverDetail.setVmState(server.getVmState());
    		serverDetail.setCreated(server.getCreated().toString());
    		serverDetail.setUpdated(server.getUpdated().toString());
    		serverList.add(serverDetail);
    	}
    	return serverList;
    }

    /*@Override
    public Router createRouter(String name, String networkId) {
        return null;
    }*/


}
