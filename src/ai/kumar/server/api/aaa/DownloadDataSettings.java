package ai.kumar.server.api.aaa;

import ai.kumar.DAO;
import ai.kumar.json.JsonObjectWithDefault;
import ai.kumar.server.*;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * 
 */
public class DownloadDataSettings extends AbstractAPIHandler implements APIHandler{

    private static final int BUFSIZE = 4096;

    @Override
    public String getAPIPath() {
        return "/data/settings";
    }

    @Override
    public BaseUserRole getMinimalBaseUserRole() {
        return BaseUserRole.ADMIN;
    }

    @Override
    public JSONObject getDefaultPermissions(BaseUserRole baseUserRole) {
        return null;
    }

    @Override
    public ServiceResponse serviceImpl(Query post, HttpServletResponse response, Authorization rights, JsonObjectWithDefault permissions) throws APIException {

        File settings = new File(DAO.data_dir.getPath()+"/settings");
        String filePath = settings.getPath();
        String fileName = post.get("file","accounting");
        filePath += "/"+fileName+".json";
        System.out.println(filePath);
        int length   = 0;
        File file = new File(filePath);
        try{
            ServletOutputStream outStream = response.getOutputStream();
            ServletContext context  = getServletConfig().getServletContext();
            String mimetype = context.getMimeType(filePath);

            if (mimetype == null) {
                mimetype = "application/octet-stream";
            }
            response.setContentType(mimetype);
            response.setContentLength((int)file.length());
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName +".json");
            byte[] byteBuffer = new byte[BUFSIZE];
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            while ((in != null) && ((length = in.read(byteBuffer)) != -1))
            {
                outStream.write(byteBuffer,0,length);
            }

            in.close();
            outStream.close();
        }
        catch (Exception e){
            System.out.println(e.getCause()+"\n"+e.getMessage());
        }
        return null;
    }
}
