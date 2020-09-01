package Login;


import Login.Interfaces.LoginConnection;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;


public class Controller implements EventHandler<ActionEvent> {


    private Main MAIN_STAGE;
    private Node NODE;
    private LoginConnection connection;

    @FXML // fx:id="loginbutton"
    protected Button loginbutton; // Value injected by FXMLLoader

    @FXML
    protected TextField USER_NAME;

    @FXML
    protected PasswordField USER_PASS;

    @FXML
    protected ProgressIndicator info;

    @FXML
    protected Label label_info;


    public void setStage(Login.Main main)
    {

        MAIN_STAGE = main;
        this.connection = this.MAIN_STAGE;

    }

    @FXML
    void initialize() {
          this.loginbutton.setOnAction(this);
          this.info.setDisable(true);
          this.label_info.toBack();

    }


    @Override
    public void handle(ActionEvent actionEvent) {

        this.NODE = (Node) actionEvent.getSource();
        if(this.NODE.getId().contentEquals("loginbutton"))
        {
            if(!USER_NAME.getText().isEmpty()  && !USER_PASS.getText().isEmpty())
                connection.loginClicked(true, USER_NAME.getText().trim(), USER_PASS.getText().trim());
            else
                connection.loginClicked(false, USER_NAME.getText(), USER_PASS.getText());

        }
    }


}

