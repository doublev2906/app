package sample.controller;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.ResourceBundle;

public class TranslateController implements Initializable {
    @FXML
    public TextArea word_translated;
    @FXML
    public TextField word_target;
    @FXML
    public ImageView img_speech;
    @FXML
    public ProgressIndicator loading;
    @FXML
    public Button btn_translate;
    @FXML
    public ImageView img_change;
    @FXML
    public Text txt_lan_target;
    @FXML
    public Text txt_lan_source;

    //Translate
    public void translate(String lan_target,String lan_source) {
        String text = word_target.getText();
        if (text.equals("")) {
            showAlert();
            return;
        }
        loading.setVisible(true);
        new Thread(() -> {
            try {
                String urlStr = "https://script.google.com/macros/s/AKfycbwBBo3bAEC5aDzqDq1gYehfcUJShDDYU6hkpB4DJWl3kXBeURuq/exec" +
                        "?q=" + URLEncoder.encode(text, StandardCharsets.UTF_8) +
                        "&target=" + lan_target +
                        "&source=" + lan_source;
                URL url = new URL(urlStr);
                StringBuilder response = new StringBuilder();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                loading.setVisible(false);
                word_translated.setText(response.toString());
            } catch (IOException e){
                e.printStackTrace();
            }
        }).start();


    }

    public void back() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../fxml/menu.fxml")));
        Scene scene = new Scene(root, 600, 400);
        Stage stage = (Stage) word_target.getScene().getWindow();
        stage.setTitle("Dictionary");
        stage.setScene(scene);

        stage.show();
    }

    public void setImg_changeClick(){
        img_change.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            if(txt_lan_source.getText().equals("English")){
                txt_lan_source.setText("Vietnamese");
                txt_lan_target.setText("English");
            }
            else {
                txt_lan_source.setText("English");
                txt_lan_target.setText("Vietnamese");
            }
            String text = word_target.getText();
            word_target.setText(word_translated.getText());
            word_translated.setText(text);
        });
    }

    public void setBtn_translateClick(){
        btn_translate.setOnAction(actionEvent -> {
            if(txt_lan_source.getText().equals("English")){
                translate("vi","en");
            }else {
                translate("en","vi");
            }

        });
    }



    public void speech() {
        img_speech.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
            Voice voice = VoiceManager.getInstance().getVoice("kevin16");
            voice.allocate();
            voice.speak(word_target.getText());
        });
    }

    private void showAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setTitle("Alert");
        alert.setContentText("Bạn chưa nhập gì mà :<");
        alert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loading.setVisible(false);
        speech();
        setImg_changeClick();
        setBtn_translateClick();
    }

}
