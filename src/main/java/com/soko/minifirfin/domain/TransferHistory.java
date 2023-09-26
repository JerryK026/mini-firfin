package com.soko.minifirfin.domain;

import com.soko.minifirfin.common.AuditingEntity;
import com.soko.minifirfin.MoneyConverter;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

// TODO: unique column들에 unique 넣기 (index 성능을 올리기 위해)
@Entity
@SQLDelete(sql = "UPDATE transfer_history SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
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
