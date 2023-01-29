package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import lombok.RequiredArgsConstructor;

import java.sql.SQLException;

@RequiredArgsConstructor
public class MemberServiceV1 {

    private final MemberRepositoryV1 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        //fromMember의 돈을 toMember에게 계좌이체
        //트랜잭션 시작 : set autocommit false;
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney()-money);
        validation(toMember); // 첫번째 검증에 성공하면 두번째 실행으로 넘어감 실패하면 넘어가지 않음
        memberRepository.update(toId, toMember.getMoney()+money);
        //커밋, 롤백 트랜잭션 종료
    }

    private static void validation(Member toMember) { // ctrl+alt+m 으로 만들 수 있음
        if(toMember.getMemberId().equals("ex")){
            throw new IllegalStateException("이체 중 예외발생");
        }
    }

}
