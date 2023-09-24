package com.soko.minifirfin.application;

import com.soko.minifirfin.domain.Member;
import com.soko.minifirfin.domain.Money;
import com.soko.minifirfin.repository.MemberRepository;
import com.soko.minifirfin.repository.TransferHistoryRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TransferServiceTest {
    @Autowired
    private TransferService transferService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TransferHistoryRepository transferHistoryRepository;

    /* Biz */
    @DisplayName("송금 성공 : 1.송금자의 잔액이 요청한 만큼 줄고 2.수금자의 잔액이 그만큼 추가되며 3.송금 내역이 저장된다")
    @Test
    void transfer_happyCase() {
        int moneyAmount = 10000;
        Member sender = memberRepository.save(new Member("sender", 50000, moneyAmount));
        Member receiver = memberRepository.save(new Member("receiver", 50000, 0));
        int historiesSize = transferHistoryRepository.findBySenderId(sender.getId()).size();

        transferService.transfer(sender.getId(), receiver.getId(), new Money(moneyAmount));

        Member newSender = memberRepository.findById(sender.getId()).orElseThrow(IllegalAccessError::new);
        Member newReceiver = memberRepository.findById(receiver.getId()).orElseThrow(IllegalArgumentException::new);
        int newHistoriesSize = transferHistoryRepository.findBySenderId(newSender.getId()).size();

        assertThat(newSender.getMoneyAmountAsBigDecimal()).isEqualTo(0);
        assertThat(newReceiver.getMoneyAmountAsBigDecimal()).isEqualTo(BigDecimal.valueOf(moneyAmount));
        assertThat(historiesSize).isEqualTo(newHistoriesSize);
    }

    @DisplayName("송금 실패 : 송금자의 잔액이 송금 요청 금액을 넘으면 요청이 거절된다")
    @Test
    void transfer_reject_senderMoneyAmountUnderZero() {
        int moneyAmount = 10000;
        Member sender = memberRepository.save(new Member("sender", 50000, moneyAmount - 5000));
        Member receiver = memberRepository.save(new Member("receiver", 50000, 0));

        Assertions.assertThatThrownBy(() -> transferService.transfer(sender.getId(), receiver.getId(), new Money(moneyAmount)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("잔액이 부족합니다. 요청한 금액 : " + moneyAmount);
    }

    @DisplayName("송금 실패 : 수금자의 잔액과 송금 요청 금액의 합이 수금자의 한도를 넘으면 요청이 거절된다")
    @Test
    void transfer_reject_receiverMoneyAmountOverLimitation() {
        int moneyAmount = 10000;
        int limitation = 50000;
        Member sender = memberRepository.save(new Member("sender", limitation, moneyAmount));
        Member receiver = memberRepository.save(new Member("receiver", limitation, limitation - moneyAmount + 1));

        Assertions.assertThatThrownBy(() -> transferService.transfer(sender.getId(), receiver.getId(), new Money(moneyAmount)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("한도가 초과되었습니다. 요청한 금액 : " + moneyAmount);
    }

    /* Tech */
    @DisplayName("송금 기술 : 송금 요청이 동시에 수행되어도 동시쓰기 현상이 발생되어선 안 된다")
    @Test
    void transfer_tech_concurrency() throws InterruptedException {
        int moneyAmount = 100;
        int numberOfThreads = 50;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        Member sender = memberRepository.save(new Member("sender", 50000, numberOfThreads * moneyAmount));
        Member receiver = memberRepository.save(new Member("receiver", 50000, 0));

        for (int i = 0; i < numberOfThreads; i++) {
            service.submit(() -> {
                try {
                    transferService.transfer(sender.getId(), receiver.getId(), new Money(moneyAmount));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Member newSender = memberRepository.findById(sender.getId()).orElseThrow(IllegalAccessError::new);
        Member newReceiver = memberRepository.findById(receiver.getId()).orElseThrow(IllegalArgumentException::new);

        assertThat(newSender.getMoneyAmountAsBigDecimal()).isEqualTo(BigDecimal.valueOf(0));
        assertThat(newReceiver.getMoneyAmountAsBigDecimal()).isEqualTo(BigDecimal.valueOf(numberOfThreads * moneyAmount));
        service.shutdown();
    }
}