/**
 * 
 */
package org.sunbird.helper;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.sunbird.common.models.util.ConfigUtil;
import org.sunbird.common.models.util.JsonKey;
import org.sunbird.common.models.util.LoggerEnum;
import org.sunbird.common.models.util.ProjectLogger;
import org.sunbird.common.models.util.ProjectUtil;

/**
 * This class will manage connection.
 * @author Manzarul
 */
public class ConnectionManager {
  
	private static TransportClient client = null;
	private static List<String> host = new ArrayList<>();
	private static List<Integer> ports = new ArrayList<>();
	private static String cluster = ConfigUtil.config.getString("es.cluster.name");
	private static String hostName = ConfigUtil.config.getString("es.host.name");
	private static String port = ConfigUtil.config.getString("es.host.port");
	static {
		initialiseConnection();
		registerShutDownHook();
	}
	
	/**
	 * This method will provide ES transport client.
	 * @return TransportClient
	 */
	public static TransportClient getClient() {
		if (client == null) {
			initialiseConnection();
		}
		return client;
	}
   
   /**
    * This method will create the client instance for elastic search.
    * @param clusterName String
    * @param host  List<String>
    * @param port List<Integer>
    * @return boolean
    * @throws Exception
    */
	private static boolean createClient(String clusterName, List<String> host, List<Integer> port) throws Exception {
		Builder builder = Settings.builder();
		if (clusterName != null && !"".equals(clusterName)) {
			builder = builder.put("cluster.name", clusterName);
		}
		builder = builder.put("client.transport.sniff", true);
		builder = builder.put("client.transport.ignore_cluster_name", true);
		client = new PreBuiltTransportClient(builder.build());
		for (int i = 0; i < host.size(); i++) {
			client.addTransportAddress(
					new InetSocketTransportAddress(InetAddress.getByName(host.get(i)), ports.get(i)));
		}
		return true;
	}
  
   /**
    * This method will read configuration data form properties file and update the list.
    * @return boolean
    */
  public static boolean initialiseConnection() {
    try {
			String cluster = ConfigUtil.config.hasPath(JsonKey.SUNBIRD_ES_CLUSTER) ? ConfigUtil.config.getString(JsonKey.SUNBIRD_ES_CLUSTER): null;
			String hostName = ConfigUtil.config.getString(JsonKey.SUNBIRD_ES_IP);
			String port = ConfigUtil.config.getString(JsonKey.SUNBIRD_ES_PORT);
			if(ProjectUtil.isStringNullOREmpty(hostName) || ProjectUtil.isStringNullOREmpty(port)) {
				return false;
			}
			String splitedHost[] = hostName.split(",");
			for (String val : splitedHost) {
				host.add(val);
			}
			String splitedPort[] = port.split(",");
			for (String val : splitedPort) {
				ports.add(Integer.parseInt(val));
			}
			boolean response = createClient(cluster, host, ports);
			ProjectLogger.log("ELASTIC SEARCH CONNECTION ESTABLISHED " + response, LoggerEnum.INFO.name());
		} catch (Exception e) {
		    ProjectLogger.log("Error while initialising connection from the Env",e);
			return false;
		}
		return true;
	}
	
	public static void closeClient() {
		client.close();
	}
	
	/**
	 * This class will be called by registerShutDownHook to 
	 * register the call inside jvm , when jvm terminate it will call
	 * the run method to clean up the resource.
	 * @author Manzarul
	 *
	 */
	public static class ResourceCleanUp extends Thread {
		  public void run() {
			  client.close(); 
		  }
	}
	
	/**
	 * Register the hook for resource clean up.
	 * this will be called when jvm shut down.
	 */
	public static void registerShutDownHook() {
		Runtime runtime = Runtime.getRuntime();
		runtime.addShutdownHook(new ResourceCleanUp());
		ProjectLogger.log("ShutDownHook registered.");
	}

}
