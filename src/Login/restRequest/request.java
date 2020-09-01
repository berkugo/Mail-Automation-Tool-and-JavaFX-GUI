package Login.restRequest;

import com.goebl.david.Request;
import com.goebl.david.Response;
import com.goebl.david.Webb;

import java.io.IOException;

public class request
{
    private final String USER_AGENT = "Mozilla/5.0";
    private String URL;
    private Webb webb;
    /*private URL REQUEST_ADDRESS;
    private HttpURLConnection connection;
    */
    private String result;
    public request(String URL) throws IOException
    {
        this.URL = URL;
        webb = Webb.create();
        this.sendGETrequest();
        /*
        this.REQUEST_ADDRESS = new URL(this.URL);
        result = new StringBuffer();
        this.sendGETrequest();
        Webb.create();

         */


    }
   /* private boolean sendGETrequest() throws IOException {
        connection = (HttpURLConnection) this.REQUEST_ADDRESS.openConnection();
        this.connection.setRequestMethod("GET");
        this.connection.setRequestProperty("USER-AGENT", USER_AGENT);
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        while((line = in.readLine()) != null)
            this.result.append(line);
        in.close();
        if(connection.getResponseCode() != 400)
        return true;
        else return false;

    } */
    private boolean sendGETrequest()
    {
        Request response = webb.get(this.URL);
        Response<String> rest = response.asString();
        this.result = rest.getBody();
        return true;

    }
    public String getResult()

    {
        if(this.result != null)
            return this.result;
        else return null;
    }


}