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
    @Column(name = "sender_ip_address", length = 15)
    private String senderIpAddress;
    @Column(name = "sender_email", length = 254)
    private String senderEmail;
    @Column(name = "sender_phone_number", length = 11)
    private String senderPhoneNumber;
    private String senderPaymentMethod;
    private String senderPaymentInfo;
    private String senderSerialNumber;

    public TransferHistory() {
    }

    public TransferHistory(
            final Long id,
            final Member sender,
            final Member receiver,
            final Money sendAmount,
            final Money senderRemainAmount,
            final Money receiverRemainAmount,
            final String senderIpAddress,
            final String senderEmail,
            final String senderPhoneNumber,
            final String senderPaymentMethod,
            final String senderPaymentInfo,
            final String senderSerialNumber
    ) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.sendAmount = sendAmount;
        this.senderRemainAmount = senderRemainAmount;
        this.receiverRemainAmount = receiverRemainAmount;
        this.senderIpAddress = senderIpAddress;
        this.senderEmail = senderEmail;
        this.senderPhoneNumber = senderPhoneNumber;
        this.senderPaymentMethod = senderPaymentMethod;
        this.senderPaymentInfo = senderPaymentInfo;
        this.senderSerialNumber = senderSerialNumber;
    }
    public TransferHistory(
            final Member sender,
            final Member receiver,
            final Money sendAmount,
            final Money senderRemainAmount,
            final Money receiverRemainAmount,
            final String senderIpAddress,
            final String senderEmail,
            final String senderPhoneNumber,
            final String senderPaymentMethod,
            final String senderPaymentInfo,
            final String senderSerialNumber
    ) {
        this(
                null,
                sender,
                receiver,
                sendAmount,
                senderRemainAmount,
                receiverRemainAmount,
                senderIpAddress,
                senderEmail,
                senderPhoneNumber,
                senderPaymentMethod,
                senderPaymentInfo,
                senderSerialNumber
        );
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

    public String getSenderIpAddress() {
        return senderIpAddress;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public String getSenderPhoneNumber() {
        return senderPhoneNumber;
    }

    public String getSenderPaymentMethod() {
        return senderPaymentMethod;
    }

    public String getSenderPaymentInfo() {
        return senderPaymentInfo;
    }

    public String getSenderSerialNumber() {
        return senderSerialNumber;
    }
}
