package Login;

import Database.databaseConnection;
import Login.Interfaces.DialogConnection;
import Login.secondPage.Company;
import Login.secondPage.detailController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.IOException;
import java.lang.reflect.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class companyDialog extends Dialog<HashMap<String, String>> implements DialogConnection {


    private String name, responsiblePerson, email, country, state;
    private int id;
    private ArrayList<String> mails = new ArrayList<>();
    private DialogConnection dg;
    private Boolean changes;
    public companyDialog(String id, String name, String responsiblePerson, String email, String country, String state, ArrayList<String> mails, int respid, TableView<Company> table) {


                try {
                    this.name = name;
                    this.responsiblePerson = responsiblePerson;
                    this.email = email;
                    this.country = country;
                    this.mails = mails;
                    this.state = state;
                    this.changes = false;
                    this.id = Integer.parseInt(id);
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("Views/companysDetail.fxml"));
                    Parent parent = loader.load();
                    getDialogPane().setContentText("MLC Europe | Edit");
                    getDialogPane().setHeaderText("Edit Company");
                    getDialogPane().setContent(parent);
                    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CLOSE);


                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<String[]> templates = new ArrayList<>();
                            detailController controller = loader.getController();
                            databaseConnection db = new databaseConnection();
                            db.openConnection();
                            try {
                                templates = db.getTemplates(respid, country);
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            controller.setLabel(templates, name, responsiblePerson, email, country, mails, companyDialog.this, respid, Integer.parseInt(id), table);

                        }
                    });
                    setResultConverter(new Callback<ButtonType, HashMap<String, String>>() {
                        @Override
                        public HashMap<String, String> call(ButtonType buttonType) {

                            System.out.println(changes);
                            if((buttonType.getButtonData().getTypeCode().trim().equals("O")) && changes)
                            {
                                HashMap<String, String> changed = new HashMap<>();
                                changed.put("ID", String.valueOf(companyDialog.this.id));
                                changed.put("Email", companyDialog.this.email);
                                changed.put("State", companyDialog.this.state);
                                changed.put("Country", companyDialog.this.country);
                                return changed;

                            }
                            else
                                return new HashMap<>();

                        }
                    });
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
        }


    @Override
    public void changeEmail(String address) {

        if(!this.email.equals(address.trim())) {
            this.email = address;
            System.out.println(address);
            this.changes = true;
        }
    }
    @Override
    public void changeState(String state) {
        if(!this.state.equals(state)) {
            this.state = state;
            this.changes = true;
        }

    }
    @Override
    public void changeCountry(String country) {
        if(!this.country.equals(country.trim())) {
            this.country = country;
            this.changes = true;
        }

    }

}
