package com.soko.minifirfin.ui;

import com.soko.minifirfin.application.TransferService;
import com.soko.minifirfin.domain.Money;
import com.soko.minifirfin.ui.request.TransferRequest;
import com.soko.minifirfin.ui.response.TransferHistoriesResponse;
import com.soko.minifirfin.ui.response.TransferResponse;
import jakarta.annotation.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfer")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> transfer(
            @RequestHeader(value = "Bearer") Long senderId,
            @RequestBody TransferRequest transferRequest
    ) {

        TransferResponse transferResponse = transferService.transfer(
                senderId,
                transferRequest.receiverId(),
                new Money(transferRequest.amount())
        );
        return ResponseEntity.ok().body(transferResponse);
    }
    @GetMapping
    @RequestMapping("/histories")
    public ResponseEntity<TransferHistoriesResponse> transferHistories(
        @RequestHeader(value = "Bearer") Long senderId,
        @RequestParam @Nullable Long cursor
    ) {
        TransferHistoriesResponse transferHistoriesResponse = transferService.transferHistories(
            senderId,
            cursor
        );
        return ResponseEntity.ok().body(transferHistoriesResponse);
    }
}
