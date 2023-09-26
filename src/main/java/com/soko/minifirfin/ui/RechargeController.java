package com.soko.minifirfin.ui;

import com.soko.minifirfin.application.RechargeService;
import com.soko.minifirfin.domain.Money;
import com.soko.minifirfin.ui.request.RechargeRequest;
import com.soko.minifirfin.ui.response.RechargeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recharge")
public class RechargeController {

    private final RechargeService rechargeService;

    public RechargeController(final RechargeService rechargeService) {
        this.rechargeService = rechargeService;
    }

    @PostMapping
    public ResponseEntity<RechargeResponse> recharge(
            @RequestHeader("Bearer") Long memberId,
            // 굳이 Money Converter 만들진 않고 int로 받아서 Money로 변환
            @RequestBody RechargeRequest rechargeRequest
    ) {
        RechargeResponse rechargeResponse = rechargeService.recharge(memberId, new Money(rechargeRequest.rechargeAmount()));

        return ResponseEntity.ok().body(rechargeResponse);
    }
}
