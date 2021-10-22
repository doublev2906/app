package sample.DB;

import sample.model.Word;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseManager {
    private  Connection connection;

    public DatabaseManager() {
        connect();
    }

    private void connect(){
        String url = "jdbc:sqlite:C:\\Users\\VanVu\\Documents\\DictionaryAppJavaFx\\words.db";
        try {
             connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public ArrayList<Word> getListWord(){
        ArrayList<Word> words = new ArrayList<>();
        String sql = "SELECT en_word,vn_word FROM words";
        try {
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(sql);
            while (result.next()){
                String en_word = result.getString("en_word");
                String vn_word = result.getString("vn_word");
                words.add(new Word(en_word,vn_word));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return words;
    }
    public void insert(String en_word,String vn_word){
        String sql = "INSERT INTO words(en_word,vn_word) VALUES(?,?)";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1,en_word);
            statement.setString(2,vn_word);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void delete(int id){
        String sql = "DELETE FROM words WHERE id = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1,id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateId(id);
    }

    public void update(int id,String en_word,String vn_word){
        String sql = "UPDATE words SET en_word = ? , "
                +"vn_word = ?"
                +"WHERE id = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1,en_word);
            statement.setString(2,vn_word);
            statement.setInt(3,id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void updateId(int id){
        String sql = "UPDATE words SET id = id - 1 WHERE id > ?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1,id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
