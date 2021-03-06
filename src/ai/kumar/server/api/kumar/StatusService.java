
package ai.kumar.server.api.kumar;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import ai.kumar.Caretaker;
import ai.kumar.KumarServer;
import ai.kumar.json.JsonObjectWithDefault;
import ai.kumar.server.APIException;
import ai.kumar.server.APIHandler;
import ai.kumar.server.AbstractAPIHandler;
import ai.kumar.server.Authorization;
import ai.kumar.server.BaseUserRole;
import ai.kumar.server.ClientConnection;
import ai.kumar.server.Query;
import ai.kumar.server.ServiceResponse;
import ai.kumar.tools.OS;
import ai.kumar.tools.UTF8;

public class StatusService extends AbstractAPIHandler implements APIHandler {
   
    private static final long serialVersionUID = 8578478303032749879L;

    @Override
    public String getAPIPath() {
        return "/kumar/status.json";
    }

    @Override
    public BaseUserRole getMinimalBaseUserRole() {
        return BaseUserRole.ANONYMOUS;
    }

    @Override
    public JSONObject getDefaultPermissions(BaseUserRole baseUserRole) {
        return null;
    }


    public static JSONObject status(final String protocolhostportstub) throws IOException {
        final String urlstring = protocolhostportstub + "/kumar/status.json";
        byte[] response = ClientConnection.downloadPeer(urlstring);
        if (response == null || response.length == 0) return new JSONObject();
        JSONObject json = new JSONObject(UTF8.String(response));
        return json;
    }

    @Override
    public ServiceResponse serviceImpl(Query post, HttpServletResponse response, Authorization rights, JsonObjectWithDefault permissions) throws APIException {

        post.setResponse(response, "application/javascript");
        
        // generate json
        Runtime runtime = Runtime.getRuntime();
        JSONObject json = new JSONObject(true);
        JSONObject system = new JSONObject(true);
        system.put("assigned_memory", runtime.maxMemory());
        system.put("used_memory", runtime.totalMemory() - runtime.freeMemory());
        system.put("available_memory", runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory());
        system.put("cores", runtime.availableProcessors());
        system.put("threads", Thread.activeCount());
        system.put("runtime", System.currentTimeMillis() - Caretaker.startupTime);
        system.put("load_system_average", OS.getSystemLoadAverage());
        system.put("load_system_cpu", OS.getSystemCpuLoad());
        system.put("load_process_cpu", OS.getProcessCpuLoad());
        system.put("server_threads", KumarServer.getServerThreads());
        system.put("server_uri", KumarServer.getServerURI());

        JSONObject index = new JSONObject(true);
        JSONObject messages = new JSONObject(true);
        JSONObject queue = new JSONObject(true);
        messages.put("queue", queue);
        JSONObject users = new JSONObject(true);
        JSONObject queries = new JSONObject(true);
        JSONObject accounts = new JSONObject(true);
        JSONObject user = new JSONObject(true);
        index.put("messages", messages);
        index.put("users", users);
        index.put("queries", queries);
        index.put("accounts", accounts);
        index.put("user", user);

        
        JSONObject client_info = new JSONObject(true);
        client_info.put("RemoteHost", post.getClientHost());
        client_info.put("IsLocalhost", post.isLocalhostAccess() ? "true" : "false");

        JSONObject request_header = new JSONObject(true);
        Enumeration<String> he = post.getRequest().getHeaderNames();
        while (he.hasMoreElements()) {
            String h = he.nextElement();
            request_header.put(h, post.getRequest().getHeader(h));
        }
        client_info.put("request_header", request_header);
        
        json.put("system", system);
        json.put("index", index);
        json.put("client_info", client_info);

        return new ServiceResponse(json);
    }

    public static void main(String[] args) {
        try {
            JSONObject json = status("http://loklak.org");
            JSONObject index_sizs = (JSONObject) json.get("index_sizes");
            System.out.println(json.toString());
            System.out.println(index_sizs.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
}
