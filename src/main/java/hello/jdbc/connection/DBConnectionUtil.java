package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j; // log를 위한??..뭐지

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class DBConnectionUtil {
    public static Connection getConnection() { // Connection이 JDBC 표준 인터페이스가 제공하는 Connnection
        try {
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            log.info("get connection = {}, class= {}", connection, connection.getClass());
            return connection;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }


}
