package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - DriverManager 사용해서 저장
 */
@Slf4j
public class MemberRepositoryV0 { // ctrl + shift + t 누르면 test를 만들어준다.

    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values(?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        // Connection 가져오는 코드
        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            int i = pstmt.executeUpdate(); // 실행, 반환값이 숫자다 = 1건을 insert했다면 1을 반환.
            return member;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally { // finally에서 close를 하는 이유는 try에서 예외가 터지면, catch로 넘어가버리기 때문에 close가 호출 않는 문제가 발생한다. 때문에 close 호출이 보장되도록 finally에서 실행.
            close(con, pstmt, null);
        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs){ // 사용한 자원들을 모두 닫아줘야 한다. 사용 역순으로 닫아줘야함

        if (rs != null){
            try{
                rs.close();
            } catch (SQLException e){
                log.info("error", e);
            }
        }

        if (stmt != null){
            try{
                stmt.close(); // SQLException이 터진다 해도 catch로 잡기 때문에 con을 닫는데 영향을 끼치지 않기 때문에 try-catch를 사용
            } catch (SQLException e){
                log.info("error", e);
            }
        }
        if (con != null){
            try{
                con.close();
            } catch (SQLException e){
                log.info("error", e);
            }
        }
    }

    public Member findById(String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            rs = pstmt.executeQuery();
            if (rs.next()){ // 첫 번째 데이터가 있으면 true 없으면 false
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            }else{
                throw new NoSuchElementException("member not found memberId=" + memberId);
            }
        } catch (SQLException e) {
            log.error("db error",e);
            throw e;
        } finally {
            close(con,pstmt,rs);
        }
    }

    public void update(String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }

    public void delete(String member_id) throws SQLException {
        String sql = "delete from member where member_id=?";
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member_id);
            pstmt.executeUpdate();;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
    }


    private static Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }


}
