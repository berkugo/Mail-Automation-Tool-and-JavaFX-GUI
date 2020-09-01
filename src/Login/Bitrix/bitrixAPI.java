package Login.Bitrix;


import Login.Main;
import Login.secondPage.Company;
import com.google.gson.*;
import javafx.concurrent.Task;
import Login.restRequest.request;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class bitrixAPI extends Task<Integer> {


    private ArrayList<JsonArray> company_list;
    private ArrayList<JsonObject> company_details;

    private final int limit = 2; // 1000 companies
    private final int speed = 300; // dont change !!!!!!!!!!!!!! IMPORTANT !!!!!!!! DONT REDUCE IT, IT BREAKS THE API.
    private Main stageHolder;
    private int iteration;
    private final String AUTH = "https://mlcomponents.bitrix24.com/rest/12/b6pt3a9mlu6prvpl";
    private String firstMethod = "/crm.company.list?select[]=ID&start=";
    private String secondMethod ="/crm.company.get?id=";
    private String thirdMethod = "/user.get?id=";

    public bitrixAPI(Main stageHolder) throws InterruptedException, ExecutionException, SQLException {
       this.company_list = new ArrayList<>();
       this.company_details = new ArrayList<>();
       this.stageHolder = stageHolder;
       Database.databaseConnection db = new Database.databaseConnection();
       db.openConnection();
       this.iteration = db.getIteration();



    }

    @Override
    protected Integer call() throws Exception {

        if(!this.isCancelled() && !this.isDone()) {


            stageHolder.progressBarUpdate(0, "Checking company ID's ..");
            Database.databaseConnection db = new Database.databaseConnection();
            db.openConnection();
            Company.setCountries();
            ArrayList<String[]> data = db.getSalesTeam();
            for(int i = 0; i<limit; i++)
            {
                JsonObject obj = (JsonObject) new JsonParser().parse(firstRequest(iteration));
                JsonArray array = (JsonArray) obj.get("result");
                this.company_list.add(array);
                this.iteration += 50;
            }
            for(int i = 0; i<this.company_list.size(); i++) {

                for (JsonElement element : this.company_list.get(i)) {
                    String result = secondRequest(element.getAsJsonObject().get("ID").getAsInt());
                    if (!result.equals(null)) {
                        JsonObject object = (JsonObject) new JsonParser().parse(result);
                        this.company_details.add(object.get("result").getAsJsonObject());
                        String phone_number = "00";
                        JsonArray emails = null;
                        if (object.get("result").getAsJsonObject().has("PHONE") && object.get("result").getAsJsonObject().has("EMAIL")) {
                            phone_number = object.get("result").getAsJsonObject().get("PHONE").getAsJsonArray().get(0).getAsJsonObject().get("VALUE").getAsString();
                            emails = object.get("result").getAsJsonObject().get("EMAIL").getAsJsonArray();
                            Date dt = new Date();
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

                            Company comp = new Company(object.get("result").getAsJsonObject().get("ID").
                                    getAsInt(), object.get("result").getAsJsonObject().get("TITLE").getAsString(), object.get("result").getAsJsonObject().get("ASSIGNED_BY_ID").getAsInt(), phone_number, emails, data, "Presale", "Waiting", true, "Not yet");
                            Thread.sleep(speed);

                        }


                    } else continue;
                }
                stageHolder.progressBarUpdate(Double.parseDouble(String.valueOf(i + 1)) / Double.parseDouble(String.valueOf(this.company_list.size())), "Fetching companies information .." + Double.parseDouble(String.valueOf(i + 1)) / Double.parseDouble(String.valueOf(this.company_list.size() * 1).toString()));
            }

            this.succeeded();
        }
        return this.iteration;
    }
    private String firstRequest(int iteration)
    {
        request requestObject = null;
        try {
            requestObject = new request(this.AUTH + firstMethod + iteration);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return requestObject.getResult();
    }
    private String secondRequest(int companyId)
    {
        request requestObject = null;
        try {
            requestObject = new request(this.AUTH + secondMethod + companyId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return requestObject.getResult();
    }
}
