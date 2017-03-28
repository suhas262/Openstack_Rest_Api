package Servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import VO.ServerDetailsVO;
import database.DatabaseAccess;
import database.UserDAO;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.output.*;
import org.openstack4j.model.compute.Server;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

/**
 * Servlet implementation class CreateSensor
 */
public class StartStop extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public StartStop() {
        super();
        // TODO Auto-generated constructor stub
    }

    private static final String DATA_DIRECTORY = "data";
    private static final int MAX_MEMORY_SIZE = 1024 * 1024 * 2;
    private static final int MAX_REQUEST_SIZE = 1024 * 1024;
    private String USERNAME ="root"; // username for remote host
    private String PASSWORD ="password"; // password of the remote host
    private String host = null; // remote host address
    private String filePath = null;
    private String fileName = null;
    private static int port=22;

    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//response.getWriter().append("Served at: ").append(request.getContextPath());
	
		HttpSession ses = request.getSession();
		
		String webProjectName = null;
		String webProjectDesc = null;
		String instanceName = null;
		String imageId = null;
		String flavorId = null;
		String networkId = null;
		String webTech = null;
		int noOfInstances = 0;
		
		File file = null;//assuming we always send file
		
        DiskFileItemFactory factory = new DiskFileItemFactory();

        factory.setSizeThreshold(MAX_MEMORY_SIZE);

        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

        String uploadFolder = getServletContext().getRealPath("")
                + File.separator + DATA_DIRECTORY;

        ServletFileUpload upload = new ServletFileUpload(factory);

        upload.setSizeMax(MAX_REQUEST_SIZE);

        try {
            // Parse the request
            List items = upload.parseRequest(request);
            Iterator iter = items.iterator();
            while (iter.hasNext()) {
                FileItem item = (FileItem) iter.next();

                if (item.isFormField()) {
                	String fieldname = item.getFieldName();
                    String fieldvalue = item.getString();
                    
                    switch (fieldname) {
    	            case "webProjectName":   webProjectName = fieldvalue;
    	                     break;
    	            case "webProjectDesc":  webProjectDesc = fieldvalue;
    	                     break;
    	            case "instanceName":  instanceName = fieldvalue;
    	                     break;
    	            case "vmImage":    	imageId = fieldvalue;
                    		 break;
    	            case "vmFlavor":  	flavorId = fieldvalue;
   	                         break;
    	            case "vmNetwork":   networkId =fieldvalue;
    	            		 break;
    	            case "webTech":  webTech = fieldvalue;
   	                         break;
   	                default:   noOfInstances = Integer.parseInt(fieldvalue);
    	            		 break;
    	        }
                    
                    System.out.println(fieldvalue);
                }
                
                if (!item.isFormField()) {
                	file = new File(item.getName());
                    fileName = file.getName();
                    filePath = uploadFolder + File.separator + fileName;
                    File uploadedFile = new File(filePath);
                    System.out.println(filePath);
                    // saves the file to upload directory
                    item.write(uploadedFile);
                }
            }

            OpenStack4JClient client = new OpenStack4JClient("admin", "admin_user_secret", "admin");
            
           /* for(int i=0;i<noOfInstances;i++){
            	String instanceNm = instanceName+i;
            	client.startVM(new ServiceSpec(instanceNm, flavorId, imageId , networkId));
            }*/
            
           // List<String> result = executeFile(fileName, filePath);
           // System.out.println(result);
            
            List<ServerDetailsVO> servers = client.getServerList();
            
            request.setAttribute("servers", servers);
    		//request.getRequestDispatcher("/WEB-INF/persons.jsp").forward(request, response);
            RequestDispatcher dispatcher = request.getRequestDispatcher("viewServers.jsp");
            dispatcher.forward(request, response);
            

        } catch (FileUploadException ex) {
            throw new ServletException(ex);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
		
		//response.sendRedirect("login_new.jsp");
	
	}

	public List<String> executeFile(String uploadFileName, String filepath)
		 {
		     List<String> result = new ArrayList<String>();
		     try
		     {
		         JSch jsch = new JSch();
		         Session session = jsch.getSession(USERNAME, host, port);
		         session.setConfig("StrictHostKeyChecking", "no");
		         session.setPassword(PASSWORD);
		         session.connect();
		         
		         //create the execution channel over the session
		         ChannelExec channelExec = (ChannelExec)session.openChannel("exec");

		         // Gets an InputStream for this channel. All data arriving in as messages from the remote side can be read from this stream.

		         InputStream in = channelExec.getInputStream();

		         channelExec.setCommand("sh "+filepath+"install_mysql.sh"+" "+uploadFileName);

		         // Execute the command
		         channelExec.connect();

		         // Read the output from the input stream we set above
		         BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		         String line;

		         while ((line = reader.readLine()) != null)
		         {
		             result.add(line);
		         }

		         int exitStatus = channelExec.getExitStatus();
		         channelExec.disconnect();
		         session.disconnect();
		         if(exitStatus < 0){
		            // System.out.println("Done, but exit status not set!");
		         }
		         else if(exitStatus > 0){
		            // System.out.println("Done, but with error!");
		         }
		         else{
		            // System.out.println("Done!");
		         }
		     }
		     catch(Exception e)
		     {
		         System.err.println("Error: " + e);
		     }
		     return result;
		 }

	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
