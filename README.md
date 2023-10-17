# Mini Firfin

## 실행 방법

1. docker 실행
```shell
docker-compose up -d
```

2. 애플리케이션 빌드
```shell
./gradlew build
```

3애플리케이션 실행
```shell
java -Dspring.profiles.active=prod -jar build/libs/mini-firfin-0.0.1-SNAPSHOT.jar -Djava.net.preferIPv4Stack=true -Duser.timezone=Asia/Seoul -Xms4096m -Xmx4096m
```

- http 폴더에 존재하는 Requests.http 파일을 활용하면 손쉽게 API를 테스트할 수 있습니다.
- schema.sql : index 설정이 들어있습니다.
- data.sql : 테스트를 위한 데이터가 들어있습니다.

## 내용
가입자간 머니 송금을 할 수 있는 API 를 설계/구현하시오.
- 다음 http api 로 설계하고, 파라미터에 대한 설명을 추가합니다.
    - 대상
        - 송금 API
          - 로그인한 사용자가 다른 유저에게 송금을 할 수 있다.
        - 특정 유저의 송금 목록을 볼 수 있는 API
          - 로그인한 사용자가 페이징이나 무한 스크롤 형태로 자신의 송금 목록을 본다.
        - 금액 충전 API
          - 로그인한 사용자가 자신의 금액을 충전할 수 있다.
    - 설계/구현/문서화에 발생할 수 있는 문제와 이를 어떻게 해결하는지에 대한 문서화가 필요합니다.

- 하루에 몇천건에서 몇만건 정도가 발생할 수 있다고 가정한다.
- 실제 타켓사용자는 사용자 전체이다.
- 단순한 케이스가 아니라, 실제로 심각한 문제를 일으킬 수 있는 케이스도 고민해야 한다.

## 과제기간
- 일주일 
  - 27일 00 시 까지 제출해주시면 됩니다.
- 과제 일정의 조절이 필요하면 연락주시면됩니다.

## 요구사항
- 개발언어는 Java/Kotlin 입니다.
- 각 유저는 현재 가지고 있는 금액을 저장하고 있습니다.
- 현재 유저는 가입상태거나 탈퇴일 수 있습니다.
- 유저마다 보유할 수 있는 최대 한도가 있고, 서로 다를 수 있습니다.
    - 한도가 넘을 경우 해당 송금은 실패해야 합니다.
- DB Schema 를 mysql 기준으로 첨부 부탁드립니다.
- 송금의 취소는 발생하지 않습니다.
- 사용자의 인증 부분 자체는 따로 고려하지 않고 파라미터 유저 id로 대체합니다.

## 다음과 같은 부분들을 고려해 주세요.
- 어떤 상황에서든 최대한 시스템의 송금 데이터가 일치해야 합니다.
    - 실패한다면 어느 시점에서 틀어지는지를 알 수 있어야 합니다.
- 100% 완성되지 않은 경우, 고민거리와 이에 대한 부분을 문서에 잘 남겨주시면 됩니다.
- 송금과 송금기록 부분에 집중해주시면 됩니다.

## 필요 결과물
- 본인이 해당 프로젝트에서 발생할 수 있는 위험요소, 그리고 이를 어떻게 해결할지에 대해서 정리한 문서
- 발생할 수 있는 문제와 이에 대한 해결책(비용측면 또는 성능 측면등)
- 구현 코드

## Schema
![mini-firfin-diagram](https://github.com/JerryK026/mini-firfin/assets/55067949/14f278e9-9624-402e-8411-5294b9cb3bbb)

### member
사용자 테이블
- birth_date : 생년월일
- deleted : 삭제 여부
- created_date_time : 생성 시간
- updated_date_time : 수정 시간
- phone_number : 전화번호
  - 프로젝트 단순화를 위해 국제 번호 등의 경우는 제외하고 11자로 고정
  - 프로젝트 단순화를 위해 통신사는 따로 저장하지 않음
- name : 이름
  - 국내에서 이름 가장 긴 사람 (한글 기준) : 30자
  - 알파벳 기준 가장 긴 사람 (알파벳 기준) : 747자
  - 애플리케이션 단순화를 위해 한글 기준으로 30자로 설정
- email : 이메일
  - rfc 2821 문서를 참고하여 254자로 설정
  - https://www.ietf.org/rfc/rfc2821.txt
- serial_number : 사용자 고유 번호


### member_money
사용자 소지금 테이블
- deleted : 삭제 여부
- created_date_time : 생성 시간
- member_id : 사용자 id
- updated_date_time : 수정 시간
- money_amount : 소지금
- money_limit : 소지금 한도
- payment_info : 결제 정보
- payment_method : 결제 수단

### transfer_history
송금 기록 테이블
- deleted : 삭제 여부
- created_date_time : 생성 시간
- receiver_id : 수신자 id
- sender_id : 송신자 id
- updated_date_time : 수정 시간
- sender_phone_number : 송신자 전화번호
  - 사용자의 전화번호가 바뀔 수도 있기 때문에 사용자 테이블에서 join하지 않음
- sender_ip_address : 송신자 ip 주소
- sender_email : 송신자 이메일
  - 사용자의 이메일 주소가 바뀔 수도 있기 때문에 사용자 테이블에서 join하지 않음
- receiver_remain_amount : 수신자 잔액
- send_amount : 송금 금액
- send_payment_info : 송금 결제 정보
- send_payment_method : 송금 결제 수단
- sender_remain_amount : 송신자 잔액
- sender_serial_number : 송신자 고유 번호

### recharge_history
충전 기록 테이블
- deleted : 삭제 여부
- created_date_time : 생성 시간
- member_id : 사용자 id
- updated_date_time : 수정 시간
- member_phone_number : 사용자 전화번호
  - 사용자의 전화번호가 바뀔 수도 있기 때문에 사용자 테이블에서 join하지 않음
- member_ip_address : 사용자 ip 주소
- member_email : 사용자 이메일
  - 사용자의 이메일주소가 바뀔 수도 있기 때문에 사용자 테이블에서 join하지 않음
- member_payment_info : 사용자 결제 정보
- member_payment_method : 사용자 결제 수단
- recharge_amount : 충전 금액
- remain_amount : 충전 후 잔액

송금 금액 / 충전 금액의 경우 1회 충전 / 송금 한도가 정해져 있기 때문에 varchar(255)가 아닌 다른 값으로 설정할까 고민했으나, 정책이 바뀔 수 있다는 점을 감안해 varchar(255)로 설정했습니다.

## API

### 송금 API

**request**  
POST http://localhost:8080/api/v1/transfer
Content-Type: application/json  
Bearer: senderId

```json
{
  "receiverId": number,       // 수신자 id
  "sendAmount": number        // 송금 금액
}
```

**response**
```json
{
  "senderName": string,               // 송신자 이름
  "receiverName": string,             // 수신자 이름
  "transferHistoryId": number,        // 송금 기록 id
  "transferredMoneyAmount": number,   // 송금 금액
  "senderRemainMoneyAmount": number,  // 송신자 잔액
  "timestamp": string,                // 거래 시각
  "status": string                    // 거래 성공 유무
}
```

### 송금 목록 API

**request**  
GET http://localhost:8080/api/v1/transfer/histories?cursorId=number
Content-Type: application/json
Bearer: memberId

**response**
```json
  "data": [{                             // 송금 목록
    "transferHistoryId": number,         // 송금 기록 id
    "senderId": number,                  // 송신자 id
    "receiverId": number,                // 수신자 id
    "senderName": string,                // 송신자 이름
    "receiverName": string,              // 수신자 이름        
    "sendAmount": number,                // 송금 금액
    "senderRemainMoneyAmount": number,   // 송신자 잔액
    "createdDateTime": string,           // 송금 시각
  }],
  "isEmpty": boolean,                    // 현재 페이지가 비었는지에 대한 여부
  "cursorId": number,                    // 다음 페이지를 위한 커서

}
```

### 충전 API

**request**  
POST http://localhost:8080/api/v1/recharge
Content-Type: application/json
Bearer: memberId

```json
{
  "rechargeAmount": number        // 충전 금액
}
```

**response**
```json
{
  "rechargeHistoryId": number,             // 충전 기록 id
  "memberId": number,                      // 사용자 id
  "memberName": string,                    // 사용자 이름
  "currentMoneyAmount": number,            // 충전 후 잔액
  "rechargedMoneyAmount": number,          // 충전한 금액
  "timestamp": string,                     // 충전 시각
  "status": string                         // 충전 성공 유무
}
```

## 상황 구체화
### 애플리케이션에 반영된 내용
- 송금 / 충전시 수수료는 없다고 가정한다.
- 한도가 50,000원이라면 50,000원까지 소지할 수 있다.
- 1회 최대 송금 가능 금액과 1일 최대 송금 한도는 199만원이다.
  - 직불전자지급수단이라고 가정한다
  - 카카오페이, 전자금융감독규정 해설서(2017.5, 10.전자지급수단의 이용한도-다.직불전자지급수단 이용한도) 참고
- 1회 최대 충전 가능 금액은 599만원 / 1일 한도는 2999만원이다
  - 전자자금이체한도라고 가정한다
  - 전자금융감독규정 해설서(2017.5, 10.전자지급수단의 이용한도-나.전자자금이체한도(지급이체의 경우))
- 본인 식별 레코드는 애플리케이션 단순화를 위해 생략한다
- 마이너스 금액 송금 / 충전이 되는 경우는 없다고 가정한다
- default 잔액 한도는 200만원으로 한다 (카카오페이 참조)
- 거래시 다음과 같은 정보를 수집해야 한다
    - 이용자의 고유식별번호
    - 이용자의 신용카드 정보 또는 지불하고자 하는 금융기관 계좌 정보
    - 이용자의 휴대폰 또는 유선 전화 번호 및 가입 통신사
        - 프로젝트 단순화를 위해 통신사 생략
    - 이용자의 상품권 번호 및 상품권 회원 아이디, 비밀번호 등
        - 상품권이 없다고 가정한다
    - 이용자의 결제하고자 하는 포인트 카드 정보
        - 포인트 카드가 없다고 가정한다
    - 이용자의 전자지갑 이용자번호 등 결제 정보
    - 이용자의 접속 IP
    - 이용자의 이메일
    - 이용자의 상품 또는 용역 거래 정보
    - 참조 : KG이니시스 개인정보 취급방침

### 애플리케이션 확장에 추가될 수 있는 내용
- role에는 보호자, 청소년, 관리자가 있을 수 있다.
  - 보호자는 청소년에게 송금할 수 있다.
  - 청소년은 보호자에게 송금할 수 없다.
  - 청소년 회원은 가입시 법정대리인 정보, 가입인증정보를 추가적으로 필요로 한다.
  - 외국인의 경우 외국인등록번호를 필요로 한다.
- 잔액이 남아있으면 회원 탈퇴가 불가능하다.
  - 잔액을 먼저 환급받아야 한다. (카카오페이 참조)
- 한도는 변경될 수 있으며, 현재 잔고보다 적어질 수 없다.
- 건당 1만원 이하 거래 기록은 1년 이상 저장해야 한다(전자금융거래법)
- 건당 1만원 초과 거래 기록은 5년 이상 저장해야 한다(전자금융거래법)

## 참고한 문서
현재 도메인에 대한 이해도가 부족하기 때문에 여러 문서들을 참고했고, 나름대로 이해한 내용을 바탕으로 자세한 내용은 다른 팀원들에게 확인받았다고 가정받은 상황에서 프로젝트를 진행했습니다.

- [전자금융감독규정 해설서 2017.5](!https://wiki.wikisecurity.net/_media/%EC%A0%84%EC%9E%90%EA%B8%88%EC%9C%B5%EA%B0%90%EB%8F%85%EA%B7%9C%EC%A0%95_%ED%95%B4%EC%84%A4_fn.pdf)
- [KG이니시스 개인정보 취급방침](!https://www.inicis.com/policy03.html)
- [토스(금융)/논란 및 사건 사고 나무위키](!https://namu.wiki/w/%ED%86%A0%EC%8A%A4(%EA%B8%88%EC%9C%B5)/%EB%85%BC%EB%9E%80%20%EB%B0%8F%20%EC%82%AC%EA%B1%B4%20%EC%82%AC%EA%B3%A0)
- [카카오페이 나무위키](https://namu.wiki/w/%EC%B9%B4%EC%B9%B4%EC%98%A4%ED%8E%98%EC%9D%B4)

## 고민한 내용 및 위험 요소

### 동시 쓰기 문제를 해결하기 위해 어떤 락을 사용해야 할까?
이 애플리케이션에서 가장 중요한 사항은 **송금 기능을 수행했을 때 얼마나 정확하게 요청이 전달되는지**라고 생각했습니다.

따라서 기본적으로 **동시 쓰기 현상을 막는 것이 가장 중요**하다고 생각해 금액을 계산하기 전에 락을 걸어 동시 쓰기 현상을 막아야 한다고 생각했고, 어떠한 방식으로 막을지 고민해 보았습니다.

대상은 `낙관적 락`과 `비관적 락`이었습니다. 현재 요청이 하루 수천 ~ 수만 건이고, 비즈니스 특성 상 송금하는 주체는 부모님 혹은 보호자들이고 수금 주체에 대해 잦은 요청이 발생하진 않을 것 같아, DB 단에 경합이 크게 발생하지 않을 것이라 판단해 `분산 락`은 대상에 넣지 않았습니다.

또한 분산 DB 환경으로 가지 않아도 견딜 수 있다는 판단이 들은 것도 그 이유였습니다.

그중 비관적 락을 선택했습니다. 이유는 낙관적 락을 선택하면 현재 정합성을 이유로 `REPEATABLE_READ` 격리 수준을 사용하고 있어 롤백 등이 발생해 버전이 꼬이는 일에서 안전할 것 같지만,  트랜잭션이 동시에 시작되어 version column이 변경되기 전에 동시에 읽는다면 혹여라도 동시성 문제가 발생할 수 있을 것이라 생각했습니다.

금전 관련한 도메인에서 정합성이 굉장히 중요하여 동시 접근 빈도를 보았을 때 성능을 손해 보더라도 정합성을 보장하는 편이 낫다고 생각했습니다.

### 로그인은 어떤 방식이라고 가정할까?
로그인 과정은 생략하라고 하셨지만, id 값을 가져오기 때문에 어떤 방식으로 로그인 할지 가정하고 설계하는 것이 필요하다고 생각했습니다.

`토큰` 방식과 `세션` 방식 중 어떤 것으로 구현할지 고민했고, jwt 토큰 방식이라고 가정했습니다.

그 이유는 현재 퍼핀 앱이 모바일 only 네이티브 애플리케이션이라고 가정해 브라우저에서 접근할 일이 없다고 생각했기 때문입니다.

따라서 세션 사용시 쿠키를 사용할 수 없으므로 세션 사용성이 떨어지고, 세션의 보안적 약점인 local storage 탈취 위험이 없기 때문에 보안적 단점이 적다고 생각했습니다.

또한, stateful하기 때문에 추후 확장에 유리할 것이고 생산성도 뛰어나 토큰 방식을 선택했습니다.

### 송금 내역 목록은 `페이지네이션`과 `무한 스크롤` 중 어떤 걸로 구현할까?
토스 앱을 참고하여 무한 스크롤 방식을 선택했습니다. 스마트폰 애플리케이션의 경우 무한 스크롤을 사용하는 편이 사용자 경험에서 더 좋다고 생각했습니다.

또한 무한 스크롤 방식을 사용하면 커서 기반 페이지네이션으로 구현하게 되니, 송금 내역 목록을 캐싱할 때도 더 효율적으로 활용이 가능할 것이라 생각했습니다.

### 로그 적재는 비동기로 옮길까?

만약 송금 혹은 충전 후 로그 적재 과정에서 문제가 생겨 롤백하게 되면 사용자 편의성에 문제가 생길 것이라고 생각했습니다.

거기에다 로그 적재 과정에 대한 트랜잭션을 분리하면, 락을 잡는 트랜잭션이 짧아지기 때문에 데드락의 위험이나 락 경합이 줄어들어 사용자 사용성 측면에서 도움이 되지 않을까 생각했습니다.

그러다가 혹시라도 거래는 이루어졌으나, 서버 혼잡으로 인해 혹여나 로그 적재 과정 중 요청이 수행되지 않게 된다면 사용자가 환불을 신청하거나, 로그가 없으므로 금융 감독 등 여러 곳에서 문제가 발생할 수 있겠다는 생각이 들어 분리하지 않기로 했습니다.

만약 정말 분리하고 싶다면 MQ 등으로 요청을 저장하고 비동기로 처리하는 방식을 선택할 수도 있을 것 같습니다.

### 데드락 문제 어떻게 해결할 수 있을까?

송금 기능과 충전 기능을 구현할 때, lock을 잡는 로직을 최대한 뒤에 두어 lock 경합을 줄이려고 노력했습니다.

그러다가 데드락이 발생하여 트랜잭션에 발생하는 SQL들을 직접 따라가 보며 잡히는 lock들을 확인했습니다.

1일 한도를 체크하는 기능 후에 member_money를 찾는 과정에 lock을 걸었습니다. 이때 member에 대해 외래키를 갖기 때문에, member로 lock이 확장되었기 때문입니다.

따라서 lock을 잡는 로직을 member_id를 활용해서 transfer_history 테이블을 조회하는 과정 이후로 배치했더니 데드락을 해결할 수 있었습니다.

### 송금 내역 목록 캐싱은 어떻게 구현할까? (시간 부족)
송금 내역 목록은 변하지 않는 데이터 정보이기 때문에 캐싱하기 좋다고 생각했습니다.

또한, Redis의 경우 SortedSet을 지원하기 때문에 날짜 최신 순으로 데이터를 넣으면 캐싱과 동시에 정렬을 수행할 수 있습니다.

따라서 송금 내역 목록에 존재하는 정렬을 위한 index를 뺄 수도 있을 거란 생각이 들어 이득을 많이 볼 수 있을 거라는 생각이 들었습니다.

송금 내역 목록은 송금마다 적재되기 때문에 쓰기 성능이 좋으면 이득이 많을 거라는 생각이 들었기 때문입니다.

캐싱 설정 시, 데이터는 어떻게 넣을 것이며 TTL은 어떻게 걸 것인지에 대해 고민했습니다.

송금 목록을 확인하는 사용자의 경우 대부분 최근 목록을 확인하는 경우가 잦을 것으로 판단해, 30일 간의 송금 내역을 캐싱하면 충분할 것이라 판단했습니다.

현재 TransferHistoryResponse의 경우 3개의 bigInt, 4개의 varchar, 1개의 datetime으로 이루어져 있습니다.

단순 계산했을 때 이 크기는 (8 * 3) + (30 + 30 + 255 + 255) + 8 = 570 + 32 = 602 byte입니다. 

다른 데이터가 추가되었을 때 레코드 하나의 크기를 약 1kb 정도라고 가정하고, 매일 3만 건의 송금이 발생한다고 가정한다면 30일치를 적재했을 때 용량은 다음과 같습니다.

30 * 30000 * 1kb = 900000kb = 900mb

900mb면 redis가 충분히 감당할 수 있는 선이라고 생각해 30일치를 캐싱하기로 했습니다.

TTL을 30일로 설정하고, 매일 00시에 스케줄러를 돌려 30일이 지난 데이터를 삭제하도록 설정하면 될 것이라 생각했습니다.

추가적인 송금이 발생하면 캐시에 업데이트하고 db에 업데이트하는 방식으로 구현하면 될 것이라 생각했습니다. (cache에서 update하는 게 아니라 WAS에서 업데이트 하므로 write through는 아닙니다.)

write through를 선택하지 않은 이유는 사용자에게 필요한 정보와 history column이 다르기 때문입니다.

history 내용을 모두 캐싱하면 필요하지 않은 데이터를 모두 올리기 때문에 메모리를 너무 많이 차지할 수 있을 거라는 생각이 들었습니다.

이때 발생할 수 있는 문제점은 커서 기반 캐싱이기 때문에 캐싱되지 않은 경우 여러 번 질의하는 문제점이 생길 것 같습니다.

예를 들어, 30일 간 기록이 하나만 있다면 캐시에 존재하는 첫 번째 요청에서는 1개만 읽어오기 때문에 client측에서는 무조건 2번 이상 요청을 해야하는 문제가 발생할 수 있습니다.

그러나, 캐싱을 했을 떄의 이점이 더 크다고 생각해 캐싱을 선택했습니다.

발생할 수 있는 또다른 문제점은, 추가적인 송금 발생시 현재 보고 있는 화면에는 그 발생한 내용이 반영되지는 않을 것이라는 점입니다.

그러나 사용자가 화면을 보면서 다른 기기로 송금하는 경우가 많지는 않을 것이고, 송금 내역 목록 페이지에 다시 접속하면 그때는 캐싱된 내용이 반영될 것이므로 괜찮을 것이라고 생각합니다.

### 동일한 요청이 여러 번 전송되는 경우 어떻게 막을 수 있을까? (통칭 : 따닥)
현재 이 애플리케이션의 경우 따닥에 대한 처리가 되어있지 않기 떄문에 모종의 문제가 발생해서 같은 요청이 여러 번 발생하면 문제가 될 수 있다고 생각합니다.

예를 들어 사용자는 송금 요청을 1번만 보내고 싶거나 충전 요청을 1번만 보내고 싶었음에도 불구하고 모종의 이유로 여러 번 발생하거나, 누군가 악의적으로 요청을 한다면 이는 서비스 신뢰성에 큰 문제가 생겨 사용자 경험에 큰 영향을 미칠 수 있다고 생각합니다.

이를 대비하기 위해 LB 단에서 이를 막을 수 있다고 생각합니다.

만약 AWS를 사용하면 ALB에서 이러한 설정을 할 수 있을 것입니다.

### 송금 내역 목록에 대해 인덱스는 어떻게 걸까?
송금 내역 목록의 경우 정렬이 들어가므로 인덱스가 필수로 필요하다고 생각했습니다.

따라서 이를 위해 index를 걸어야 하는데, 어떤 index를 걸어야 할지 고민했고 후보군은 다음과 같았습니다.

(created_date_time, sender_id, id, deleted), (created_date_time, sender_id), (created_date_time, id)

그 중 첫번째를 택했습니다. 그 이유는 커버링 인덱스를 태울 수 있어 성능이 훨씬 좋을 것이라 판단했기 때문입니다.

그러나, 다른 대상들을 후보에 올렸던 이유는 인덱스에 컬럼이 많아질 수록 인덱스 크기가 늘어나 쓰기 성능에 좋지 않은 영향을 미칠 수 있겠다는 생각이 들었기 때문입니다.

설계 상으로는 송금 내역 목록에 대해 이미 캐싱을 할 예정이었기 때문에 created_date_time를 포함해 카디널리티가 높은 sender_id 혹은 id만 걸어도 충분하지 않을까 하는 생각도 들었습니다.

+) 현재 소프트 딜리트를 사용하고 있기 떄문에, 모든 테이블은 (pk, deleted) 형태로 인덱스가 걸려 있습니다.

=== 과제 제출 이후 추가된 내용 ===
### score는 pk vs date
redis를 활용해서 cache를 적용할 때, sorted set을 사용했기 때문에 정렬 기준이 필요했습니다.

이때 고민했던 키는 pk와 created_date 중 어떤 것을 잡아야 최신 순으로 정렬할 수 있을까? 였습니다.

created_date의 경우 "최신"임을 보장할 수 있으나, 경우에 따라 중복이 발생할 수도 있을 거라는 생각이 들었습니다.

만에 하나 중복이 발생해 기록이 누락되어 저장될 경우 사용자 입장에서 중요한 송금 기록에 대해 확인할 수 없으므로 애플리케이션에 신뢰할 수 없는 문제가 발생할 수 있을 거라는 생각이 들었습니다.

pk의 경우 sequence를 사용하거나, UUID와 같이 PK가 순서대로 생성되지 않을 경우로 이전할 경우 문제가 발생할 수 있습니다.

하지만, 캐시의 경우 문제 발생시 언제든지 제거할 수 있을 것이라 판단하여 pk를 score key로 두었습니다.

### 트랜잭션은 session callback? @Transactional?
만약 송금 로직이 실패하게 되면 송금 내역에도 추가되지 않아야 하므로 cache에 올리는 로직에도 transaction을 적용하였습니다.

lettuce의 경우 transaction을 사용하기 위해 session callback과 @Transactional을 활용한 제어가 가능합니다.

session callback의 경우 구현이 간단하나, Spring의 트랜잭션과 범위가 다르다는 점이 문제가 될 수 있을 것 같았습니다.

session callback 로직을 메서드 맨 끝에 보내더라도, 만에 하나 RDBMS timeout 등의 문제가 발생하여 cache에는 commit이 되나 db에 저장하는 로직에서는 rollback이 수행되는 상황이 발생할 수도 있을 것 같아 문제가 될 수 있다고 판단했습니다.

따라서 트랜잭션 생명주기를 Spring과 맞추기 위하여 @Transactional을 사용하는 편이 낫다고 판단했습니다.

### Connection Pool을 만들지 않더라도 커넥션 공유로 인한 트랜잭션 간섭 문제가 발생하진 않는지?
lettuce는 connection pool의 default connection 수가 1개입니다.

또한, spring-data-redis를 보면 "Singleton-connection sharing for non-blocking commands"라고 표현하고 있습니다.

트랜잭션을 사용할 때 가장 주의해야 할 점 중 하나는 트랜잭션은 커넥션 당 수행되어야 한다는 것입니다.

만약 Single-connection sharing 방식이라면 송금 요청 A가 수행되는 도중에 송금 요청 B가 수행될 경우 A와 B가 커넥션을 공유하기 때문에 트랜잭션 과정에서 문제가 발생할 수 있을 것이라 판단했습니다.

몇가지의 학습테스트와, 공식문서를 참고하여 setEnableTransactionSupport 옵션을 활용하면 커넥션에 트랜잭션이 바인딩될 수 있음을 확인했습니다.

connection pool의 connection이 1개라면 송금 요청이 여럿 발생했을 때 redis connection이 block되기 때문에 성능에 문제가 될 수 있을거라 판단해 connection pool을 임의의 값 4로 설정했습니다.

### 로그 저장 / 로그 Cache에 쓰기 작업을 실패했다고 해서 rollback 시키는게 맞는지?
현재 TransactionService에서는 로그를 cache에 쓰기 작업에 실패하거나 로그 저장 작업이 실패하면 rollback을 수행하고 있습니다.

하지만 이 두 과정은 실제 송금에 대한 주요 관심사가 아니라고 볼 수도 있습니다.

그러한 주요 관심사가 아닌 로직이 실패했다고 해서 성공한 주요 관심사 로직에 영향을 주면 사용자 사용성에 영향을 주는 건 아닐까 하는 생각이 들었습니다.

그러나, 그대로 두기로 결정했는데 그 이유는 다음과 같습니다.

1. 만일 송금과 동시에 송금 목록에 대해 조회 요청했을 경우, 실제로는 송금했음에도 불구하고 송금 목록을 조회하지 못할 수도 있다.

2. 만약 송금은 처리됐음에도 모종의 이유로 송금 로그 저장에 실패한다면, 거래 기록을 저장해야 한다는 법을 위반할 수도 있다.

위 두 문제에서 발생할 수 있는 사용자 관점에서의 사용성의 애플리케이션 신뢰도가 더 클 것이라는 판단이 들어 한 트랜잭션으로 묶기로 결정했습니다.

## 애플리케이션 작성 환경
- OS : Windows 10 Education
    - CPU : Intel(R) Core(TM) i5-10400F CPU @ 2.90GHz   2.90 GHz
        - 6 코어, 12 스레드
        - RAM : 16.0GB

이 환경을 참고해서 min heap / max heap size를 4GB, MySQL container를 4GB로 설정했습니다.
- 서버용 컴퓨터가 아니기 때문에 나머지 절반은 다른 프로세스에 사용되어 CPU와 RAM 크기를 절반이라고 가정했습니다.