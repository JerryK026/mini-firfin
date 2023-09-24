package com.soko.minifirfin.domain;

import com.soko.minifirfin.common.AuditingEntity;
import com.soko.minifirfin.MoneyConverter;
import jakarta.persistence.*;

@Entity
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

    public TransferHistory() {}

    public TransferHistory(Long id, Member sender, Member receiver, Money sendAmount, Money senderRemainAmount, Money receiverRemainAmount) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.sendAmount = sendAmount;
        this.senderRemainAmount = senderRemainAmount;
        this.receiverRemainAmount = receiverRemainAmount;
    }
}
