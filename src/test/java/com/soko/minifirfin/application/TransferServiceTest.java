package com.soko.minifirfin.application;

import com.soko.minifirfin.common.exception.BadRequestException;
import com.soko.minifirfin.domain.Member;
import com.soko.minifirfin.domain.Money;
import com.soko.minifirfin.domain.TransferHistory;
import com.soko.minifirfin.repository.MemberRepository;
import com.soko.minifirfin.repository.TransferHistoryRepository;
import com.soko.minifirfin.ui.response.TransferHistoriesResponse;
import com.soko.minifirfin.ui.response.TransferHistoryResponse;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.soko.minifirfin.common.exception.BadRequestCode.NOT_ENOUGH_MONEY;
import static com.soko.minifirfin.common.exception.BadRequestCode.OVER_LIMITATION;
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

        Member newSender = memberRepository.findById(sender.getId())
            .orElseThrow(IllegalAccessError::new);
        Member newReceiver = memberRepository.findById(receiver.getId())
            .orElseThrow(IllegalArgumentException::new);
        int newHistoriesSize = transferHistoryRepository.findBySenderId(newSender.getId()).size();

        assertThat(newSender.getMoneyAmountAsBigDecimal()).isEqualTo(BigDecimal.valueOf(0));
        assertThat(newReceiver.getMoneyAmountAsBigDecimal()).isEqualTo(
            BigDecimal.valueOf(moneyAmount));
        assertThat(newHistoriesSize).isEqualTo(historiesSize + 1);
    }

    @DisplayName("송금 실패 : 송금자의 잔액이 송금 요청 금액보다 작으면 요청이 거절된다")
    @Test
    void transfer_reject_senderMoneyAmountUnderZero() {
        int moneyAmount = 10000;
        Member sender = memberRepository.save(new Member("sender", 50000, moneyAmount - 5000));
        Member receiver = memberRepository.save(new Member("receiver", 50000, 0));

        Assertions.assertThatThrownBy(
                () -> transferService.transfer(
                    sender.getId(),
                    receiver.getId(),
                    new Money(moneyAmount))
            )
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining(NOT_ENOUGH_MONEY.getMessage());
    }

    @DisplayName("송금 실패 : 수금자의 잔액과 송금 요청 금액의 합이 수금자의 한도를 넘으면 요청이 거절된다")
    @Test
    void transfer_reject_receiverMoneyAmountOverLimitation() {
        int moneyAmount = 10000;
        int limitation = 50000;
        Member sender = memberRepository.save(new Member("sender", limitation, moneyAmount));
        Member receiver = memberRepository.save(
            new Member("receiver", limitation, limitation - moneyAmount + 1));

        Assertions.assertThatThrownBy(
                () -> transferService.transfer(sender.getId(), receiver.getId(),
                    new Money(moneyAmount)))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining(OVER_LIMITATION.getMessage());
    }

    @DisplayName("송금 내역 확인 성공 : 송금 내역 목록 첫 페이지를 확인한다")
    @Test
    void transferHistories_happyCase_firstPage() throws InterruptedException {
        // given
        Member sender = memberRepository.save(new Member("sender", 5_000_000, 1_999_999));
        Member receiver = memberRepository.save(new Member("receiver", 5_000_000, 0));

        transferNTimesAsync(50, 10_000, sender.getId(), receiver.getId());

        // when
        TransferHistoriesResponse transferHistoriesResponse =
            transferService.transferHistories(sender.getId(), null);

        long expectedCursor = transferHistoryRepository.findBySenderId(sender.getId())
            .get(50 - 30)
            .getId();

        assertThat(transferHistoriesResponse.data().size()).isEqualTo(30);
        assertThat(transferHistoriesResponse.isEmpty()).isFalse();
        assertThat(transferHistoriesResponse.cursorId()).isEqualTo(expectedCursor);
    }

    @DisplayName("송금 내역 확인 성공 : 송금 내역 목록에서 첫페이지 이후를 확인한다")
    @Test
    void transferHistories_happyCase_afterFirstPage() throws InterruptedException {
        // given
        Member sender = memberRepository.save(new Member("sender", 5_000_000, 1_999_999));
        Member receiver = memberRepository.save(new Member("receiver", 5_000_000, 0));

        transferNTimesAsync(50, 10_000, sender.getId(), receiver.getId());

        List<TransferHistory> transferHistories = transferHistoryRepository.findBySenderId(sender.getId());
        Long cursor = transferHistories.get(50 - 30).getId();

        // when
        TransferHistoriesResponse transferHistoriesResponse =
            transferService.transferHistories(sender.getId(), cursor);

        long expectedCursor = transferHistories.get(0).getId();

        // then
        assertThat(transferHistoriesResponse.data().size()).isEqualTo(20);
        assertThat(transferHistoriesResponse.isEmpty()).isFalse();
        assertThat(transferHistoriesResponse.cursorId()).isEqualTo(expectedCursor);
    }

    @DisplayName("송금 내역 확인 성공 : 마지막 페이지의 다음 페이지를 조회하면 빈 목록을 반환한다")
    @Test
    void transferHistories_happyCase_underPageCapacity() throws InterruptedException {
        // given
        Member sender = memberRepository.save(new Member("sender", 5_000_000, 1_999_999));
        Member receiver = memberRepository.save(new Member("receiver", 5_000_000, 0));

        transferNTimesAsync(30, 10_000, sender.getId(), receiver.getId());

        List<TransferHistory> transferHistories = transferHistoryRepository.findBySenderId(sender.getId());
        Long cursor = transferHistories.get(0).getId();

        // when
        TransferHistoriesResponse transferHistoriesResponse =
            transferService.transferHistories(sender.getId(), cursor);

        // then
        assertThat(transferHistoriesResponse.data().size()).isEqualTo(0);
        assertThat(transferHistoriesResponse.isEmpty()).isTrue();
        assertThat(transferHistoriesResponse.cursorId()).isEqualTo(-1L);
    }

    @DisplayName("송금 내역 확인 성공 : 거래 내역이 없으면 빈 목록을 반환한다")
    @Test
    void transferHistories_happyCase_empty() {
        Member sender = memberRepository.save(new Member("sender", 5_000_000, 1_999_999));
        Member receiver = memberRepository.save(new Member("receiver", 5_000_000, 0));

        TransferHistoriesResponse transferHistoriesResponse =
            transferService.transferHistories(sender.getId(), null);

        assertThat(transferHistoriesResponse.data().size()).isEqualTo(0);
        assertThat(transferHistoriesResponse.isEmpty()).isTrue();
        assertThat(transferHistoriesResponse.cursorId()).isEqualTo(-1L);
    }

    /* Tech */
    @DisplayName("송금 기술 : 송금 요청이 동시에 수행되어도 동시쓰기 현상이 발생되어선 안 된다")
    @Test
    void transfer_tech_concurrency() throws InterruptedException {
        int moneyAmount = 100;
        int threadCount = 50;
        Member sender = memberRepository.save(
            new Member("sender", 50000, threadCount * moneyAmount));
        Member receiver = memberRepository.save(new Member("receiver", 50000, 0));

        transferNTimesAsync(threadCount, moneyAmount, sender.getId(), receiver.getId());

        Member newSender = memberRepository.findById(sender.getId())
            .orElseThrow(IllegalAccessError::new);
        Member newReceiver = memberRepository.findById(receiver.getId())
            .orElseThrow(IllegalArgumentException::new);

        assertThat(newSender.getMoneyAmountAsBigDecimal()).isEqualTo(BigDecimal.valueOf(0));
        assertThat(newReceiver.getMoneyAmountAsBigDecimal())
            .isEqualTo(BigDecimal.valueOf(threadCount * moneyAmount));
    }

    private void transferNTimesAsync(int n, int moneyAmount, long senderId, long receiverId)
        throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(n);
        CountDownLatch latch = new CountDownLatch(n);

        for (int i = 0; i < n; i++) {
            service.submit(() -> {
                try {
                    transferService.transfer(senderId, receiverId, new Money(moneyAmount));
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        service.shutdownNow();
    }
}