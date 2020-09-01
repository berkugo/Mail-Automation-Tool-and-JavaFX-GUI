package Login.Interfaces;

public interface LoginConnection {

      boolean loginClicked(boolean clicked, String username, String pass);
      void progressBarUpdate(double prog, String status);


}
