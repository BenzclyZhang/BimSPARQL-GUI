package nl.tue.ddss.bimsparql.client;

import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import nl.tue.ddss.bimsparql.shared.InstanceGeometry;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("myService")
public interface ModelService extends RemoteService {
public HashMap<Integer,InstanceGeometry> getGeometries();
	
	public String uploadModel();
	
	public String uploadQuery();
	
	List<String[]> getResults(String query);
	
	InstanceGeometry showTempGeometry(String wkt);
	
  String modelServer(String name) throws IllegalArgumentException;
}
