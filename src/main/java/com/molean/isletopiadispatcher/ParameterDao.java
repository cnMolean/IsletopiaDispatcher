package com.molean.isletopiadispatcher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ParameterDao {

    public static String get(String playerName, String key) {
        try (Connection connection = DataSourceUtils.getConnection()) {
            String sql = "select p_value from isletopia_parameters where player=? and p_key=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, playerName);
            preparedStatement.setString(2, key);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static void set(String playerName, String key, String value) {
        if (get(playerName, key) == null) {
            insert(playerName, key, value);
        } else {
            try (Connection connection = DataSourceUtils.getConnection()) {
                String sql = "update isletopia_parameters set p_value=? where player=? and p_key=?";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, value);
                preparedStatement.setString(2, playerName);
                preparedStatement.setString(3, key);
                preparedStatement.execute();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    public static void insert(String playerName, String key, String value) {
        try (Connection connection = DataSourceUtils.getConnection()) {
            String sql = "insert into isletopia_parameters(player,p_key,p_value) values(?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, playerName);
            preparedStatement.setString(2, key);
            preparedStatement.setString(3, value);
            preparedStatement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
