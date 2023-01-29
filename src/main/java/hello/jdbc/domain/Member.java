package hello.jdbc.domain;

import lombok.Data;

@Data
public class Member {
    private String memberId;
    private int money;

    public Member(){
    }
    public Member(String memberId, int money) { // 생성자 단축키 alt + insert
        this.memberId = memberId;
        this.money = money;
    }
}
