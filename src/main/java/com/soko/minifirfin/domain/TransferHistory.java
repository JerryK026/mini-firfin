package com.soko.minifirfin.domain;

import com.soko.minifirfin.common.AuditingEntity;
import com.soko.minifirfin.MoneyConverter;
import jakarta.persistence.*;

@Entity
/*
    TODO: index 후보군 : (created_date_time, sender_id, id), (created_date_time, sender_id), (created_date_time, id)
    판단 기준 및 경고사항
    - 우선 정렬해야 하므로 created_date_time은 첫번째 column으로 들어가야 하고, DESC로 인덱스 설정해야 함
    - InnoDB를 사용하므로 id column을 추가해서 커버링 인덱스를 사용할까?
    - created_date_time도 unique 걸면 index 성능이 좋아질 것 같은데...혹시나 unique하지 못하다면?
    - 어차피 스케줄러 짜서 캐싱할 거면 인덱스를 걸지 않아도 되는 것 아닐까?
    - 만약 sender_id와 id를 함께 인덱스에 넣을 거면 sender_id를 먼저 넣어야 함.
      id를 cursor로 사용할 것이기 때문에 id < cursor 절이 있어 sender_id가 뒤로 올 경우 sender_id는 인덱스를 못탐

    결론 : 시간 되면 테스트해보고 결정하자
 */
// TODO: unique column들에 unique 넣기 (index 성능을 올리기 위해)
// TODO: auditing class에 있는 필드 index 못잡는 현상 해결
// @Table(name = "transfer_history", indexes = @Index(name = "idx__created_date_time__sender_id", columnList = "created_date_time, sender_id DESC"))
public class TransferHistory extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private Member sender;
    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private Member receiver;
    @Convert(converter = MoneyConverter.class)
    private Money sendAmount;
    @Convert(converter = MoneyConverter.class)
    private Money senderRemainAmount;
    @Convert(converter = MoneyConverter.class)
    private Money receiverRemainAmount;

    public TransferHistory() {
    }

    public TransferHistory(
            final Long id,
            final Member sender,
            final Member receiver,
            final Money sendAmount,
            final Money senderRemainAmount,
            final Money receiverRemainAmount
    ) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.sendAmount = sendAmount;
        this.senderRemainAmount = senderRemainAmount;
        this.receiverRemainAmount = receiverRemainAmount;
    }

    public TransferHistory(
            final Member sender,
            final Member receiver,
            final Money sendAmount,
            final Money senderRemainAmount,
            final Money receiverRemainAmount
    ) {
        this(null, sender, receiver, sendAmount, senderRemainAmount, receiverRemainAmount);
    }

    public Long getId() {
        return id;
    }

    public Member getSender() {
        return sender;
    }

    public Member getReceiver() {
        return receiver;
    }

    public Money getSendAmount() {
        return sendAmount;
    }

    public Money getSenderRemainAmount() {
        return senderRemainAmount;
    }

    public Money getReceiverRemainAmount() {
        return receiverRemainAmount;
    }
}
