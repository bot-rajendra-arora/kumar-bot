
package ai.kumar.server.api.cms;

import ai.kumar.DAO;
import ai.kumar.json.JsonObjectWithDefault;
import ai.kumar.server.*;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.ArrayList;

/**
 * 
 * Adds API Endpoint to get all languages for a group.
 * BASE ROLE Required is ANONYMOUS
 * Accepts 2 GET parameters, Model Name and Group Name
 * http://127.0.0.1:4000/cms/getAllLanguages.json?group=assistants
 */
public class GetAllLanguages  extends AbstractAPIHandler implements APIHandler {


    private static final long serialVersionUID = -7872551914189898030L;

    @Override
    public BaseUserRole getMinimalBaseUserRole() { return BaseUserRole.ANONYMOUS; }

    @Override
    public JSONObject getDefaultPermissions(BaseUserRole baseUserRole) {
        return null;
    }

    @Override
    public String getAPIPath() {
        return "/cms/getAllLanguages.json";
    }

    @Override
    public ServiceResponse serviceImpl(Query call, HttpServletResponse response, Authorization rights, final JsonObjectWithDefault permissions) {

        String model_name = call.get("model", "general");
        File model = new File(DAO.model_watch_dir, model_name);
        String group_name = call.get("group", "knowledge");
        File group = new File(model, group_name);
        JSONObject json = new JSONObject(true);
        json.put("accepted", false);
        String[] languages = group.list((current, name) -> new File(current, name).isDirectory());
        JSONArray languagesArray = new JSONArray(languages);
        json.put("languagesArray", languagesArray);
        json.put("accepted", true);
        return new ServiceResponse(json);
    }
}
