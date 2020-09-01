package Login;

import Login.Bitrix.bitrixAPI;
import Login.secondPage.ControllerSecond;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class Main extends Application implements Login.Interfaces.LoginConnection {


    private Stage primaryStage;
    private Login.Controller cont;

    @Override
    public void start(Stage primaryStage) throws Exception{
        this.primaryStage = primaryStage;
        FXMLLoader loader= new FXMLLoader(getClass().getResource("Views/login.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("MLC Europe - Login Page");
        primaryStage.setScene(new Scene(root, primaryStage.getWidth(), primaryStage.getHeight()));
        primaryStage.setResizable(false);
        cont = loader.getController();
        Platform.runLater(() -> {
            cont.setStage(Main.this);
            primaryStage.show();

        });

    }

    public static void main(String[] args) {



        launch(args);



    }
    private void openSecondPage() throws IOException, InterruptedException, ExecutionException, SQLException {
      FXMLLoader secondPageLoader = new FXMLLoader(getClass().getResource("Views/secondpage.fxml"));
      Parent secondPage = secondPageLoader.load();
      Stage secondStage = new Stage();
      secondStage.setTitle("MLC Europe - CRM | Login.Mail Application");
      secondStage.setResizable(false);
      secondStage.setScene(new Scene(secondPage, secondStage.getWidth(), secondStage.getHeight()));
      ControllerSecond cont = secondPageLoader.getController();
        cont.enableTable();
        secondStage.show();



/*
        Platform.runLater(new Runnable() {
          @Override
          public void run() {
              try {



              } catch (InterruptedException e) {
                  e.printStackTrace();
              }

          }
      });*/


    }

    @Override
    public boolean loginClicked(boolean clicked, String username, String pass)
    {
        Database.databaseConnection db_class = new Database.databaseConnection();
        db_class.openConnection();
        try {
            if(db_class.checkForUsers(username, pass))
            {

                    Date dt = new Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    Database.databaseConnection db_comp = new Database.databaseConnection();
                    db_comp.openConnection();
                    Task<Boolean> task = db_comp.getCompanies(formatter.format(dt), true);
                    new Thread(task).start();
                    Main.this.cont.label_info.toFront();
                    Main.this.cont.info.setDisable(false);
                    Main.this.cont.loginbutton.setDisable(true);
                    Main.this.cont.USER_NAME.setDisable(true);
                    Main.this.cont.USER_PASS.setDisable(true);
                    task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent workerStateEvent) {
                            bitrixAPI api = null;
                            try {
                                api = new bitrixAPI(Main.this);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            Thread thread = new Thread(api);
                            thread.start();

                            bitrixAPI finalApi = api;
                            api.setOnSucceeded((WorkerStateEvent workerState) -> {
                                try {
                                    Database.databaseConnection db_two = new Database.databaseConnection();
                                    db_two.openConnection();
                                    db_two.setIteration(finalApi.get());
                                    Thread.sleep(3000);
                                    openSecondPage();
                                    Main.this.primaryStage.close();

                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    });



            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public void progressBarUpdate(double prog, String status) {
        Platform.runLater(() -> {
            //Main.this.cont.info.setProgress(prog);
            Main.this.cont.label_info.setText(status);
        });

    }

}
