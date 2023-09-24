package com.soko.minifirfin.ui;

import com.soko.minifirfin.application.TransferService;
import com.soko.minifirfin.domain.Money;
import com.soko.minifirfin.ui.request.TransferRequest;
import com.soko.minifirfin.ui.response.TransferResponse;
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
    @RequestMapping
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
}
