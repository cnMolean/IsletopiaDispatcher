package com.molean.isletopiabungeetweaker;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUtils {
    private static MysqlDataSource dataSource;

    static {
        dataSource = new MysqlDataSource();
        String url = "jdbc:mysql://localhost/minecraft?useSSL=false";
        String username = "molean";
        String password = "123asd";
        dataSource.setUrl(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);
    }

    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return connection;
    }

    public static String get(String playerName, String column) {
        String result = null;
        if (!exist(playerName)) {
            return null;
        }
        try (Connection connection = getConnection()) {
            String sql = "select " + column + " from parameter where player_name=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, playerName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getString(1);
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result;
    }

    public static boolean set(String playerName, String column, String value) {
        if (!exist(playerName)) insert(playerName);

        boolean execute = false;
        try (Connection connection = getConnection()) {
            String sql = "update parameter set " + column + "=? where player_name=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, value);
            preparedStatement.setString(2, playerName);
            execute = preparedStatement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return execute;
    }

    public static boolean exist(String playerName) {
        String string = null;
        try (Connection connection = getConnection()) {
            String sql = "select player_name from parameter where player_name=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, playerName);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                string = resultSet.getString(1);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return playerName.equals(string);
    }

    public static boolean insert(String playerName) {
        boolean execute = false;
        try (Connection connection = getConnection()) {
            String sql = "insert into parameter(player_name) value('" + playerName + "') ";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            execute = preparedStatement.execute(sql);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return execute;
    }
}
