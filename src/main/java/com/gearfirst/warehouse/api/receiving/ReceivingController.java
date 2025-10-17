package com.gearfirst.warehouse.api.receiving;

import com.gearfirst.warehouse.api.receiving.dto.ReceivingNoteSummaryResponse;
import com.gearfirst.warehouse.common.response.ApiResponse;
import com.gearfirst.warehouse.common.response.SuccessStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/api/v1/receiving")
public class ReceivingController {
    @GetMapping("/summaries/not-done")
    public ResponseEntity<ApiResponse<List<ReceivingNoteSummaryResponse>>> getPendingNotes(@RequestParam(required = false) String date) {
        return ApiResponse.success(SuccessStatus.SEND_RECEIVING_NOTE_SUMMARY_SUCCESS, ReceivingMockStore.findNotDone(date));
    }


}
