package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - ConnectionParam
 * 커넥션 유지가 필요한 findById, update 메서드 추가
 * 커넥션 유지가 필요갛기 때문에 두 메서드에서 커넥션을 닫으면 안된다. 이후에도 커넥션을 계속 이어서 사용하기 때문이다. 서비스 로직이 끝날 때 트랜잭션을 종료하고 닫아야 한다.
 */
@Slf4j
public class MemberRepositoryV2 {

    private final DataSource dataSource; // dataSouce 주입을 받음

    public MemberRepositoryV2(DataSource dataSource) {
        this.dataSource = dataSource;
    }

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

    public Member findById(Connection con, String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{
//            con = getConnection(); 새로운 커넥션을 쓰는것이기 때문에 XXXX, 파라미터로 넘어온 커넥션을 써야한다.
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
            //connection은 여기서 닫지 않는다.
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(pstmt);
//            JdbcUtils.closeConnection(con); 커넥션을 여기서 닫아버리면 커넥션은 그대로 종료되기 때문에
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


    public void update(Connection con,String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";
        PreparedStatement pstmt = null;

        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            //connection은 여기서 닫지 않는다.
            JdbcUtils.closeStatement(pstmt);
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


    private void close(Connection con, Statement stmt, ResultSet rs){ // 사용한 자원들을 모두 닫아줘야 한다. 사용 역순으로 닫아줘야함

        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeConnection(con);

    }



    private Connection getConnection() throws SQLException {

        Connection con = dataSource.getConnection();
        log.info("getConnection={}, class={}",con, con.getClass());

        return con;
    }


}
