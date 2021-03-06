
package ai.kumar.server.api.kumar;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import ai.kumar.tools.DateParser;
import org.json.JSONArray;
import org.json.JSONObject;

import ai.kumar.DAO;
import ai.kumar.json.JsonObjectWithDefault;
import ai.kumar.mind.KumarCognition;
import ai.kumar.server.APIException;
import ai.kumar.server.APIHandler;
import ai.kumar.server.AbstractAPIHandler;
import ai.kumar.server.Authorization;
import ai.kumar.server.BaseUserRole;
import ai.kumar.server.Query;
import ai.kumar.server.ServiceResponse;

import javax.servlet.http.HttpServletResponse;

/**
 * get information about the user.
 * i.e. it will return the history of conversation with two memories starting from
 * given date. Also returns number of cognitions left to return.
 * http://127.0.0.1:4000/kumar/memory.json?cognitions=2&date=2017-06-27T18:56:12.389Z
 */
public class UserService extends AbstractAPIHandler implements APIHandler {
   
    private static final long serialVersionUID = 8578478303098111L;

    @Override
    public BaseUserRole getMinimalBaseUserRole() { return BaseUserRole.ANONYMOUS; }

    @Override
    public JSONObject getDefaultPermissions(BaseUserRole baseUserRole) {
        return null;
    }

    public String getAPIPath() {
        return "/kumar/memory.json";
    }
    
    @Override
    public ServiceResponse serviceImpl(Query post, HttpServletResponse response, Authorization user, final JsonObjectWithDefault permissions) throws APIException {

        int cognitionsCount = Math.min(10, post.get("cognitions", 10));
        String date = post.get("date",null);
        boolean isValidDate = (date != null);

        String client = user.getIdentity().getClient();
        List<KumarCognition> cognitions = DAO.kumar.getMemories().getCognitions(client);

        //Stores all dates in arraylist
        List<Date> dates = new ArrayList<>();
        for(int i=0 ; i<cognitions.size() ; i++) {
            dates.add(cognitions.get(i).getQueryDate());
        }

        Date keyDate = null;
        //Checks if date is valid
        if(isValidDate) {
            try {
                keyDate = DateParser.utcFormatter.parseDateTime(date).toDate();
                isValidDate = true;
            } catch (IllegalArgumentException e) {
                isValidDate = false;
            }
        } else {
            isValidDate = false;
        }

        int index = -1;

        if(isValidDate)
            index = dates.indexOf(keyDate);

        JSONArray coga = new JSONArray();
        int cognitionsRemaining = cognitions.size();
        if(index == -1) {
            for (KumarCognition cognition : cognitions) {
                coga.put(cognition.getJSON());
                cognitionsRemaining--;
                if (--cognitionsCount <= 0) break;
            }
        } else {
            cognitionsRemaining -= index;
            for(int i=index ; i<cognitions.size() ; i++) {
                coga.put(cognitions.get(i).getJSON());
                cognitionsRemaining--;
                if (--cognitionsCount <= 0) break;
            }
        }
        JSONObject json = new JSONObject(true);
        json.put("cognitions", coga);
        json.put("cognitions_remaining", cognitionsRemaining);
        return new ServiceResponse(json);
    }
}
