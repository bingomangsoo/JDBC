package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 매니저
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {

//    private final DataSource dataSource;

    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        // 트랜잭션 시작
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {

            bizLogic(fromId, toId, money);

            transactionManager.commit(status); // 성공시 커밋

        }catch (Exception e){
            transactionManager.rollback(status); // 실패시 롤백
            throw new IllegalStateException(e);
        }
//        finally { 트랜잭션이 commit되거나 rollback될 때 트랜잭션 매니져가 커넥션이 종료해주기 때문에 직접 release 할 필요 X
//            release(con);
//        }

    }

    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        //비지니스 로직 수행
        //fromMember의 돈을 toMember에게 계좌이체
        //트랜잭션 시작 : set autocommit false;
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney()- money);
        validation(toMember); // 첫번째 검증에 성공하면 두번째 실행으로 넘어감 실패하면 넘어가지 않음
        memberRepository.update(toId, toMember.getMoney()+ money);
    }

    private static void release(Connection con) {
        if(con != null){
            try{
//              con.close(); 커넥션 풀을 쓴 경우, 커넥션이 반환되는데, setAutoCommit이 false이기 때문에 그 반환된 커넥션은 setAutoCommit이 false를 가지게 된다.
                con.setAutoCommit(true); // 커넥션 풀 고려
                con.close();
            }catch (Exception e){
                log.info("error", e);
            }
        }
    }

    private static void validation(Member toMember) { // ctrl+alt+m 으로 만들 수 있음
        if(toMember.getMemberId().equals("ex")){
            throw new IllegalStateException("이체 중 예외발생");
        }
    }

}
