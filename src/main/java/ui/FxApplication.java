package ui;

import bank.PrivateBank;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FxApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // directoryName sama kayak P4, misalnya "accounts"
        PrivateBank bank = new PrivateBank("MyBank", 0.02, 0.03, "accounts");

        //tolong ambil file fxml ini
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/mainview.fxml"));
        Scene scene = new Scene(loader.load()); // load scene nya ke window

        MainviewController controller = loader.getController();
        controller.setBank(bank); //nyambungin ui ke controller (data + logic)

        stage.setTitle("Bank - Mainview");
        stage.setScene(scene); //taro scene ke window
        stage.show(); // zeigt das Fenster ke layar
    }
}
