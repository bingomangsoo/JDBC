package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDbException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * SQLExceptionTranslator 추가
 */
@Slf4j
public class MemberRepositoryV4_2 implements MemberRepository{

    private final DataSource dataSource; // dataSouce 주입을 받음
    private final SQLExceptionTranslator exTranslaor;

    public MemberRepositoryV4_2(DataSource dataSource) {
        this.dataSource = dataSource;
        this.exTranslaor = new SQLErrorCodeSQLExceptionTranslator(dataSource); //데이터소스를 넣어주는 이유는 어떤 DB를 쓰는지 알아내기 위해
    }

    @Override
    public Member save(Member member) {
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
            throw exTranslaor.translate("save",sql,e);
        } finally { // finally에서 close를 하는 이유는 try에서 예외가 터지면, catch로 넘어가버리기 때문에 close가 호출 않는 문제가 발생한다. 때문에 close 호출이 보장되도록 finally에서 실행.
            close(con, pstmt, null);
        }
    }
    @Override
    public Member findById(String memberId) {
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
            throw exTranslaor.translate("findById",sql,e);
        } finally {
            close(con,pstmt,rs);
        }
    }


    @Override
    public void update(String memberId, int money) {
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
            throw exTranslaor.translate("update",sql,e);
        } finally {
            close(con, pstmt, null);
        }
    }



    @Override
    public void delete(String member_id) {
        String sql = "delete from member where member_id=?";
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member_id);
            pstmt.executeUpdate();;
        } catch (SQLException e) {
            throw exTranslaor.translate("delete",sql,e);
        } finally {
            close(con, pstmt, null);
        }
    }


    private void close(Connection con, Statement stmt, ResultSet rs){ // 사용한 자원들을 모두 닫아줘야 한다. 사용 역순으로 닫아줘야함

        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        //주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다.
        DataSourceUtils.releaseConnection(con,dataSource);
//        JdbcUtils.closeConnection(con);

    }



    private Connection getConnection() throws SQLException {
        //주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다.
        Connection con = DataSourceUtils.getConnection(dataSource);
        log.info("getConnection={}, class={}",con, con.getClass());

        return con;
    }


}
