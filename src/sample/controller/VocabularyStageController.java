package sample.controller;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import sample.DB.DatabaseManager;
import sample.model.Word;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class VocabularyStageController implements Initializable {

    ArrayList<Word> words;
    ObservableList<String> lv_word;
    DatabaseManager manager;
    ArrayList<String> vocab_en_words;

    @FXML
    public Button btn_back;
    @FXML
    public ImageView speech;
    @FXML
    public Button btn_add_word;
    @FXML
    public TextArea en_word;
    @FXML
    public TextArea vn_word;
    @FXML
    public TextField tf_find_word;
    @FXML
    public ListView<String> lv_list_vocab;

    /**
     * get list word from database
     */
    public void initWords() {
        words = manager.getListWord();
    }

    /**
     * set up list vocabulary
     */

    private void setListView() {
        //init list string english word
        vocab_en_words = new ArrayList<>();
        for (var i : words) {
            vocab_en_words.add(i.getWordTarget());
        }
        //binding list to observablelist to listen listview change to display
        lv_word = FXCollections.observableList(vocab_en_words);

        //filter data when user find vocabulary
        FilteredList<String> filteredData = new FilteredList<>(lv_word, s -> true);
        lv_list_vocab.setItems(filteredData);
        tf_find_word.textProperty().addListener((observableValue, s, t1) -> {
            String filter = tf_find_word.getText();
            if (filter == null || filter.length() == 0) {
                filteredData.setPredicate(s1 -> true);
            } else {
                filteredData.setPredicate(s1 -> s1.toLowerCase().startsWith(filter.toLowerCase()));
            }
        });

        //init context menu to implement edit and delete vocabulary in listview
        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem();
        editItem.textProperty().bind(Bindings.format("Edit"));
        MenuItem deleteItem = new MenuItem();
        deleteItem.textProperty().bind(Bindings.format("Delete"));
        contextMenu.getItems().addAll(editItem, deleteItem);

        //set listview handler when click item
        lv_list_vocab.setOnMouseClicked(mouseEvent -> {
            int index = find(lv_list_vocab.getSelectionModel().getSelectedItem());
            String s = lv_list_vocab.getSelectionModel().getSelectedItem();
            if (mouseEvent.getButton() == MouseButton.SECONDARY) {

                //set action when edit item
                editItem.setOnAction(actionEvent -> {
                    Word editWord = getWordFromDialog(words.get(index));
                    if (editWord != null) {
                        if (!words.get(index).getWordTarget().equals(editWord.getWordTarget())) {
                            words.set(index, editWord);
                            lv_word.set(index, editWord.getWordTarget());
                            manager.update(index + 1, editWord.getWordTarget(), editWord.getWordExplain());
                            showAlert("Chỉnh sửa từ thành công");
                        }
                    }


                });
                //set action when delete item
                deleteItem.setOnAction(actionEvent -> {
                    words.remove(index);
                    lv_word.remove(s);
                    manager.delete(index + 1);
                    showAlert("Xóa từ thành công");
                });
                contextMenu.show(lv_list_vocab, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            } else {
                contextMenu.hide();
                if (index != -1) {
                    en_word.setText(words.get(index).getWordTarget());
                    vn_word.setText(words.get(index).getWordExplain());
                }
            }


        });


    }

    private int find(String text) {
        return vocab_en_words.indexOf(text);
    }

    private Word getWordFromDialog(Word word) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Dialog");

        ButtonType okBtn = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(okBtn, cancelBtn);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField vocabulary = new TextField();

        TextField mean = new TextField();

        if (word == null) {
            vocabulary.setPromptText("Vocabulary");
            mean.setPromptText("Mean");
        } else {
            vocabulary.setText(word.getWordTarget());
            mean.setText(word.getWordExplain());
        }

        grid.add(new Label("Vocabulary:"), 0, 0);
        grid.add(vocabulary, 1, 0);
        grid.add(new Label("Mean:"), 0, 1);
        grid.add(mean, 1, 1);
        dialog.getDialogPane().setContent(grid);

        AtomicBoolean check = new AtomicBoolean(false);

        Optional<ButtonType> result = dialog.showAndWait();

        result.ifPresent(buttonType -> {
            if (buttonType == okBtn) {
                check.set(true);
            }
        });
        if (check.get()) return new Word(vocabulary.getText(), mean.getText());
        else return null;
    }

    private void setFindHandler() {
        tf_find_word.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                int index = find(tf_find_word.getText());
                if (index != -1) {
                    en_word.setText(words.get(index).getWordTarget());
                    vn_word.setText(words.get(index).getWordExplain());
                } else showAlert("Từ này hơi cao siêu nên mình chưa thêm vào từ điển :<<");
            }
        });
    }

    public void addWord() {
        Word add_word = getWordFromDialog(null);
        if (add_word != null) {
            words.add(add_word);
            lv_word.add(add_word.getWordTarget());
            manager.insert(add_word.getWordTarget(), add_word.getWordExplain());
            showAlert("Thêm từ thành công!");
        }


//        notify();
    }

    private void showAlert(String txt) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setTitle("Alert");
        alert.setContentText(txt);
        alert.showAndWait();
    }

    public void speechEn() {
        speech.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
            Voice voice = VoiceManager.getInstance().getVoice("kevin16");
            voice.allocate();
            voice.speak(en_word.getText());
        });
    }

    public void back() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("../fxml/menu.fxml")));
        Scene scene = new Scene(root, 600, 400);
        Stage stage = (Stage) btn_back.getScene().getWindow();
        stage.setTitle("Dictionary");
        stage.setScene(scene);

        stage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        manager = new DatabaseManager();
        initWords();
        setListView();
        setFindHandler();
        speechEn();
    }
}
