package Login.secondPage;

import Database.databaseConnection;
import Login.Bitrix.fetchAPI;
import Login.companyDialog;
import Login.Mail.Mail;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import Login.restRequest.request;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class ControllerSecond implements EventHandler<MouseEvent> {

    private ArrayList<TableColumn<Company, String>> columns;

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Label count, sent;

    @FXML
    private TableView<Company> table;

    @FXML
    private Button reset;

    @FXML
    private TableColumn<Company, String> company_id;
    @FXML
    private TableColumn<Company, String> company_name;

    @FXML
    private TableColumn<Company, String> country;

    @FXML
    private TableColumn<Company, String> email;

    @FXML
    private Button process;

    @FXML ProgressIndicator indicator;

    @FXML
    private TableColumn<Company, String> templates;


    @FXML
    private Button stop;

    @FXML
    private Button sb;

    @FXML
    private TextField sf;

    @FXML
    private TableColumn<Company, String> resp;

    @FXML
    private TableColumn<Company, String> phone_number;

    @FXML
    private TableColumn<Company, String> sale;

    @FXML
    private TableColumn<Company, String> status;

    @FXML
    private TableColumn<Company, String> date;

    @FXML private Button bitrixbutton;
    @FXML private TextField bitrixfield;

    @FXML private DatePicker datepicker;

    @FXML private Label cp;


    ArrayList<String> onesRemoved = new ArrayList<>();

    @FXML
    void initialize() {

        this.columns = new ArrayList<>();
        table.setEditable(false);
        columns.add(company_name);
        columns.add(company_id);
        columns.add(resp);
        columns.add(email);
        columns.add(country);
        columns.add(status);
        columns.add(sale);
        columns.add(phone_number);
        process.setOnMouseClicked(this);
        indicator.setVisible(false);
        bitrixbutton.setOnMouseClicked(this::bitrix);
            reset.setOnMouseClicked(mouseEvent -> {
                try {
                    reset(mouseEvent);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });




        datepicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();

                setDisable(date.compareTo(today) > 0 );
            }
        });
        datepicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                if(Mail.queue.size() == 0) {
                    databaseConnection db = new databaseConnection();
                    db.openConnection();
                    Date dt = new Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

                    try {
                        Task task;
                        if (formatter.format(dt).equals(datepicker.getValue().toString())) {
                            task = db.getCompanies(datepicker.getValue().toString(), true);
                            table.setDisable(true);
                            indicator.setVisible(true);
                            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                                @Override
                                public void handle(WorkerStateEvent workerStateEvent) {
                                    table.refresh();
                                    count.setText("Companies on the list: " + Company.list.size());
                                    table.setDisable(false);
                                    indicator.setVisible(false);
                                    bitrixbutton.setDisable(false);
                                    bitrixfield.setDisable(false);
                                    process.setDisable(false);
                                }
                            });
                        } else {
                            task = db.getCompanies(datepicker.getValue().toString(), false);
                            table.setDisable(true);
                            indicator.setVisible(true);
                            bitrixbutton.setDisable(true);
                            bitrixfield.setDisable(true);
                            task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                                @Override
                                public void handle(WorkerStateEvent workerStateEvent) {
                                    table.refresh();
                                    table.setDisable(false);
                                    indicator.setVisible(false);
                                    ObservableList<Company> data = FXCollections.observableList(Company.list);
                                    count.setText("Companies on the list: " + Company.list.size());
                                    if (data.size() == 0) process.setDisable(true);
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            for (int t = 0; t < data.size(); t++) {
                                                data.get(t).setStatus("Send again");
                                            }
                                        }
                                    });

                                }
                            });

                        }

                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Date Picking Error");
                    alert.setHeaderText("Error");
                    alert.setContentText("You can not change the date.\nMay the processes are running.\nThere are currently " + Mail.queue.size() + "\nprocesses running.");
                    Optional<ButtonType> result = alert.showAndWait();
                }
            }
        });
        sb.setOnMouseClicked(this::search);
        stop.setOnMouseClicked(this::stop);


    }

    private void stop(MouseEvent mouseEvent) {

       if(Mail.queue.size() != 0)
       {

           Alert alrt = new Alert(Alert.AlertType.CONFIRMATION);
           alrt.setTitle("Stop all the processes!");
           alrt.setContentText("Are you sure that you want to close\nall the processes which are running currently?\nThere are " +Mail.queue.size() +"\nrunning processes.");
           Optional<ButtonType> rst = alrt.showAndWait();
           if(rst.get() == ButtonType.OK)
           {
               for(int i = 0; i<Mail.queue.size(); i++)
               {
                   Mail.queue.forEach((integer, tasks) -> {

                       for(Task<Void> tsk : tasks)
                       {
                           tsk.cancel();
                           onesRemoved.clear();

                       }
                       cp.setText("Stopped.");
                       tasks.clear();
                   });
                   Mail.queue.clear();
               }
           }
       }
       else
       {
           Alert alert = new Alert(Alert.AlertType.INFORMATION);
           alert.setTitle("Process Information");
           alert.setHeaderText("Attention!");
           alert.setContentText("There is no process running.");

           alert.show();
       }



    }

    private void search(MouseEvent mouseEvent)
    {

        if(!sf.getText().isEmpty()) {
            boolean flag = false;
            for(Company temp : FXCollections.observableList(Company.list))
            {
                if(temp.getEmail().trim().equals(sf.getText().trim()) || temp.getCompanyName().equals(sf.getText()))
                {

                    flag = true;
                    String URL = "https://mlcomponents.bitrix24.com/rest/12/b6pt3a9mlu6prvpl/crm.company.get?id=" + temp.getID().trim();
                    JsonObject result = null;
                    ArrayList<String> mails = new ArrayList<>();
                    try {
                        request req;
                        req = new request(URL);
                        JsonParser parser = new JsonParser();
                        result = (JsonObject) parser.parse(req.getResult());
                        if (result.get("result").getAsJsonObject().has("EMAIL")) {
                            for (JsonElement object : result.get("result").getAsJsonObject().get("EMAIL").getAsJsonArray()) {
                                mails.add(object.getAsJsonObject().get("VALUE").getAsString());
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Dialog<HashMap<String, String>> d = new companyDialog(temp.getID(), temp.getCompanyName(), temp.getResponsiblePerson(), temp.getEmail(), temp.getCountry(), temp.getStater(), mails, temp.getRespPersonID(), table);
                    d.setTitle("MLC Europe | Edit");
                    d.showAndWait();
                    if (!d.getResult().isEmpty()) {
                        databaseConnection db = new databaseConnection();
                        db.openConnection();
                        try {
                            db.updateCompany(d.getResult().get("ID"), d.getResult().get("Email"), d.getResult().get("State"), d.getResult().get("Country"), d.getResult().get("phone")).setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                                @Override
                                public void handle(WorkerStateEvent workerStateEvent) {
                                    Company comp = Company.find(Integer.parseInt(d.getResult().get("ID")));
                                    comp.setEmail(d.getResult().get("Email"));
                                    comp.setStater(d.getResult().get("State"));
                                    comp.setCountry(d.getResult().get("Country"));
                                    Platform.runLater(new Runnable() {
                                        @Override
                                        public void run() {
                                            table.refresh();

                                        }
                                    });

                                }
                            });
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
            if(!flag)
            {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Seach");
                alert.setHeaderText("Error");
                alert.setContentText("There is no company in the list like that.");
                Optional<ButtonType> result = alert.showAndWait();
            }

        }

    }

    private void reset(MouseEvent mouseEvent) throws ExecutionException, InterruptedException {

        Alert alertDialog = new Alert(Alert.AlertType.CONFIRMATION);
        alertDialog.setTitle("Reset all the data!");
        alertDialog.setHeaderText("Are you sure?");
        alertDialog.setContentText("If you click 'OK', then all the data which has been stored on our database will be deleted.\nThe process could be started from the scratch by doing this.");
        Optional<ButtonType> options = alertDialog.showAndWait();
        if(options.isPresent() && (options.get() == ButtonType.OK))
        {
            databaseConnection db = new databaseConnection();
            db.openConnection();
            db.resetAllData().setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent workerStateEvent) {
                    Company.list.clear();
                    table.refresh();
                }
            });
        }

    }

    private void bitrix(MouseEvent mouseEvent) {

        try {
            int value = 0;
            try {
                value = Integer.parseInt(this.bitrixfield.getText());
                if(value < 50 || value > 200) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Bitrix Fetch API");
                    alert.setHeaderText("Error");
                    alert.setContentText("You want to fetch " + value + " companies more. \nThe maximum value is 200, minimum is 50.");
                    Optional<ButtonType> result = alert.showAndWait();
                    return;
                }
            }
            catch (NumberFormatException ex) {
                  value = 0;
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Bitrix Fetch API");
            alert.setHeaderText("Information");
            alert.setContentText("You want to fetch " + value + " companies more. \nIt may take several minutes. ");
            Optional<ButtonType> result = alert.showAndWait();
            if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
                this.table.setDisable(true);
                this.bitrixbutton.setDisable(true);
                this.bitrixfield.setDisable(true);
                this.process.setDisable(true);
                this.indicator.setVisible(true);
                fetchAPI fetch = new fetchAPI(value);
                new Thread(fetch).start();
                fetch.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent workerStateEvent) {
                        table.setDisable(false);
                        indicator.setVisible(false);
                        ControllerSecond.this.bitrixbutton.setDisable(false);
                        ControllerSecond.this.bitrixfield.setDisable(false);
                        process.setDisable(false);
                        table.refresh();
                        enableTable();
                    }
                });

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.enableTable();
               table.refresh();
    }

    public void enableTable()
    {

        ObservableList<Company> data = FXCollections.observableList(Company.list);
        for(TableColumn<Company, String> el : columns)
        {
            el.setResizable(false);
            el.setEditable(false);

        }
        company_name.setCellValueFactory(new PropertyValueFactory<Company, String>("companyName"));
        resp.setCellValueFactory(new PropertyValueFactory<Company, String>("responsiblePerson"));
        company_id.setCellValueFactory(new PropertyValueFactory<Company, String>("ID"));
        country.setCellValueFactory(new PropertyValueFactory<Company, String>("country"));
        email.setCellValueFactory(new PropertyValueFactory<Company, String>("email"));
        sale.setCellValueFactory(new PropertyValueFactory<Company, String>("stater"));
        status.setCellValueFactory(new PropertyValueFactory<Company, String>("status"));
        date.setCellValueFactory(new PropertyValueFactory<Company, String>("date"));
        phone_number.setCellValueFactory(new PropertyValueFactory<Company, String>("phone_number"));
        company_name.setSortable(false);
        resp.setSortable(false);
        company_id.setSortable(false);
        country.setSortable(false);
        email.setSortable(false);
        sale.setSortable(false);
        status.setSortable(false);
        date.setSortable(false);
        phone_number.setSortable(false);
        company_name.setSortable(false);
        company_name.setReorderable(false);
        resp.setReorderable(false);
        company_id.setReorderable(false);


        table.setItems(data);
        count.setText("Companies processed: " + Company.list.size());
        table.setOnMouseClicked(mouseEvent -> {
            if(mouseEvent.getClickCount() == 2 && table.getSelectionModel().getSelectedItem() != null)
            {

                String URL = "https://mlcomponents.bitrix24.com/rest/12/b6pt3a9mlu6prvpl/crm.company.get?id=" + table.getSelectionModel().getSelectedItem().getID().trim();
                JsonObject result = null;
                ArrayList<String> mails = new ArrayList<>();
                try {
                    request req;
                    req = new request(URL);
                    JsonParser parser = new JsonParser();
                    result = (JsonObject) parser.parse(req.getResult());
                    if(result.get("result").getAsJsonObject().has("EMAIL"))
                    {
                        for(JsonElement object : result.get("result").getAsJsonObject().get("EMAIL").getAsJsonArray())
                        {
                            mails.add(object.getAsJsonObject().get("VALUE").getAsString());
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Dialog<HashMap<String, String>> d = new companyDialog(table.getSelectionModel().getSelectedItem().getID(), table.getSelectionModel().getSelectedItem().getCompanyName(), table.getSelectionModel().getSelectedItem().getResponsiblePerson(), table.getSelectionModel().getSelectedItem().getEmail(), table.getSelectionModel().getSelectedItem().getCountry(), table.getSelectionModel().getSelectedItem().getStater(), mails, table.getSelectionModel().getSelectedItem().getRespPersonID(), table);
                d.setTitle("MLC Europe | Edit");
                d.showAndWait();
                if(!d.getResult().isEmpty()) {
                    databaseConnection db = new databaseConnection();
                    db.openConnection();
                    try {
                        db.updateCompany(d.getResult().get("ID"), d.getResult().get("Email"), d.getResult().get("State"), d.getResult().get("Country"), d.getResult().get("phone")).setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                            @Override
                            public void handle(WorkerStateEvent workerStateEvent) {
                                Company comp = Company.find(Integer.parseInt(d.getResult().get("ID")));
                                comp.setEmail(d.getResult().get("Email"));
                                comp.setStater(d.getResult().get("State"));
                                comp.setCountry(d.getResult().get("Country"));
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        table.refresh();

                                    }
                                });

                            }
                        });
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

            }
        });
    }


    @Override
    public void handle(MouseEvent mouseEvent)
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Starting Process");
        alert.setHeaderText("Send Mails");
        alert.setContentText("\nIn the list, you can only see\nthe responsible person \nwhich is not currently being processed.\n");
        Optional<ButtonType> options = alert.showAndWait();
        if(options.get() == ButtonType.OK)
        {
            ChoiceDialog<String> dialog = new ChoiceDialog<>();
            dialog.setTitle("Filter Screen");
            dialog.setHeaderText("Choose one from the list for filtering.");
            dialog.setContentText("Application will only send emails\nto the companies that the person has, you selected.    ");
            databaseConnection db = new databaseConnection();
            db.openConnection();
            try {
                ArrayList<String[]> team = db.getSalesTeam();
                for(String[] item : team) {
                    if(!onesRemoved.contains(item[1]))
                        dialog.getItems().add(item[1]);
                }
                dialog.setSelectedItem(dialog.getItems().get(0));
                Optional<String> opt = dialog.showAndWait();

                if(opt.isPresent()) {

                    System.out.println(opt.get());
                    try {
                        Mail.Templates.putTeam();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    int currentTask = Integer.parseInt(opt.get().split("-", 3)[0].trim());
                    String remove = opt.get();
                    onesRemoved.add(remove);
                    Mail.queue.put(currentTask, new ArrayList<>());
                    Task<Void> proc = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            final int ct = currentTask;
                            ObservableList<Company> data = FXCollections.observableList(Company.list);
                            int size = 0;
                            for(int t = 0; t<data.size(); t++) {
                                try {
                                    if (String.valueOf(data.get(t).getRespPersonID()).equals(opt.get().split("-", 3)[0].trim())) {
                                        if ((data.get(t).getStatus().equals("Waiting") || data.get(t).getStatus().equals("Send again") || data.get(t).getStatus().equals("Failed")) && !Objects.equals(data.get(t).getStater(), "Sale")) { // we will see
                                            size += 1;
                                            Mail obj = new Mail(data.get(t).getRespPersonID(), data.get(t).getEmail(), data.get(t).getCountry(), size, ct, onesRemoved, remove);
                                            Mail.queue.get(ct).add(obj);
                                            int finalT = t;
                                            int finalSize = size;
                                            obj.setOnSucceeded(workerStateEvent -> {
                                                // sent.setText(String.valueOf(Integer.parseInt(sent.getText().trim().split(":", 2)[1]) + 1));
                                                if(Mail.queue.get(ct) != null)
                                                cp.setText(data.get(finalT).getResponsiblePerson() + " | " + data.get(finalT).getEmail() + " | " + finalSize +" / " + Mail.queue.get(ct).size());
                                                else
                                                cp.setText("One of tasks is done. There are still " + Mail.queue.size() + " tasks running.");

                                                data.get(finalT).setStatus("Sent");
                                                Date dt = new Date();
                                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                                                data.get(finalT).setDate(formatter.format(dt));
                                                table.refresh();
                                                Platform.runLater(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        databaseConnection db = new databaseConnection();
                                                        db.openConnection();
                                                        try {
                                                            db.updateStatus(Integer.parseInt(data.get(finalT).getID()), formatter.format(dt));
                                                        } catch (ExecutionException e) {
                                                            e.printStackTrace();
                                                        } catch (InterruptedException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });

                                                if (finalT == data.size() - 1) {
                                                    ControllerSecond.this.table.setDisable(false);
                                                }
                                            });
                                            obj.setOnRunning(workerStateEvent -> {

                                                data.get(finalT).setStatus("Sending..");
                                                table.refresh();
                                            });
                                            obj.setOnFailed(workerStateEvent -> {

                                                data.get(finalT).setStatus("Failed");
                                                table.refresh();

                                            });

                                        }
                                    }
                                } catch(InterruptedException e){
                                    e.printStackTrace();
                                } catch(ExecutionException e){
                                    e.printStackTrace();
                                } catch(SQLException e){
                                    e.printStackTrace();
                                }

                            }
                            for(int thread = 0; thread<Mail.queue.get(ct).size(); thread++)
                            {
                                new Thread(Mail.queue.get(ct).get(thread)).start();
                                Thread.sleep(8000);

                            }
                            if(Mail.queue.get(currentTask).size() == 0)
                                onesRemoved.remove(remove);
                            return null;

                        }
                    };
                    new Thread(proc).start();

                }

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            // dialog.getItems().add()



         }
      }


}
