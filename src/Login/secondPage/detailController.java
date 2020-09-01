package Login.secondPage;

import Database.databaseConnection;
import Login.Interfaces.DialogConnection;
import Login.companyDialog;
import Login.Mail.Mail;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class detailController {


       @FXML
       private Label companyLabel;

       @FXML
       private CheckBox presale;

       @FXML
       private Button sendEmail;

       @FXML
       private CheckBox sale;

       @FXML
       private ComboBox<String> email;

       @FXML
       private ComboBox<String> countries;
       ;

       @FXML
       private TextArea content;

       @FXML
       private TextField title;

       @FXML
       private ComboBox<String> templates;

       @FXML
       private Button editbutton;

       private companyDialog parentDialog;
       private int respid;
       private int compid;
       private TableView table;

       private String[] cnts= {"Germany", "United Kingdom", "Italy", "Spain", "Finland", "Hungary", "China", "Turkey", "Austria", "France", "Portugal",
               "Malta", "Sweden", "Norway", "Denmark", "Chezch", "Poland", "Bulgaria", "Maleysia", "Greece", "Canada"};


        @FXML
        public void initialize()
        {

              this.presale.setOnMouseClicked(new EventHandler<MouseEvent>() {
                  @Override
                  public void handle(MouseEvent mouseEvent) {

                      if(presale.isSelected()) {
                          sale.setSelected(false);
                          DialogConnection dg = parentDialog;
                          dg.changeState("Presale");
                      }
                      else {
                          sale.setSelected(true);
                          DialogConnection dg = parentDialog;
                          dg.changeState("Sale");
                      }
                  }
              });
            this.sale.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {


                    if(sale.isSelected()) {
                        presale.setSelected(false);
                        DialogConnection dg = parentDialog;
                        dg.changeState("Sale");
                    }
                    else {
                        presale.setSelected(true);
                        DialogConnection dg = parentDialog;
                        dg.changeState("Presale");
                    }
                }
            });
            email.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    DialogConnection dg = parentDialog;
                    dg.changeEmail(email.getSelectionModel().getSelectedItem().toLowerCase());
                }
            });
            countries.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                     DialogConnection dg = parentDialog;
                     dg.changeCountry(countries.getSelectionModel().getSelectedItem().trim());
                }
            });
            this.content.setDisable(true);
            this.title.setDisable(true);
            this.editbutton.setOnMouseClicked(this::editbutton);



        }

    private void editbutton(MouseEvent mouseEvent) {

          if(this.editbutton.getText().equals("Edit")) {
              this.editbutton.setText("Save");
              this.content.setVisible(false);
              this.content.setVisible(true);
              this.title.setVisible(false);
              this.title.setVisible(true);
              this.content.setDisable(false);
              this.title.setDisable(false);
          }

    }

    public void setLabel(ArrayList<String[]> templates, String companyName, String responsiblePerson, String email, String country, ArrayList<String> mails, companyDialog companyDialog, int rasped, int compid, TableView<Company> table)
        {
            ArrayList<String> list_country = new ArrayList<>();
            ArrayList<String> list_of_titles = new ArrayList<>();
            for(String[] iter : templates) {
                list_of_titles.add(iter[4]);
            }
            list_country.add(country);
            list_country.addAll(Arrays.asList(cnts));
            this.companyLabel.setText(companyName + " | " + responsiblePerson);
            this.email.setItems(FXCollections.observableList(mails));
            this.countries.setItems(FXCollections.observableList(list_country));
            if(FXCollections.observableList(list_of_titles).size() != 0)
            this.templates.setItems(FXCollections.observableList(list_of_titles));
            else
            {
                ArrayList<String> ars = new ArrayList<>();
                ars.add("Default English to All");
                this.templates.setItems(FXCollections.observableList(ars));
            }
            this.parentDialog = companyDialog;
            this.email.getSelectionModel().select(email);
            this.countries.getSelectionModel().selectFirst();
            this.templates.getSelectionModel().selectFirst();
            this.respid = rasped;
            System.out.println(rasped);
            this.compid = compid;
            this.table = table;
            sendEmail.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {

                    Mail obj = null;
                    try {
                        Mail.Templates.putTeam();
                        Mail.process = null;
                        obj = new Mail(detailController.this.respid, detailController.this.email.getSelectionModel().getSelectedItem(), detailController.this.countries.getSelectionModel().getSelectedItem(), 0 , -1, null, null);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    obj.setOnSucceeded(workerStateEvent -> {
                        Company temp = Company.find(compid);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Information");
                        alert.setContentText("Login.Mail sent!");
                        alert.show();
                        temp.setStatus("Sent");
                        table.refresh();
                        Date dt = new Date();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                        temp.setDate(formatter.format(dt));
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {

                                databaseConnection db = new databaseConnection();
                                db.openConnection();
                                try {
                                    db.updateStatus(Integer.parseInt(temp.getID()), formatter.format(dt));
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    });
                    obj.setOnFailed(workerStateEvent -> {
                        Company temp = Company.find(compid);
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Login.Mail sent!");
                        alert.setContentText("There was an error while sending the mail.\nIt may be caused because of Unusual Sending Activity\nerror, contact the ZOHO Support.");
                        temp.setStatus("Failed");
                        table.refresh();
                        alert.show();

                    });
                    new Thread(obj).start();

                }
            });

        }


}


