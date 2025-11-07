package com.gearfirst.warehouse.api.summary;

import com.gearfirst.warehouse.api.dto.NoteCountsByDateResponse;
import com.gearfirst.warehouse.api.receiving.persistence.ReceivingQueryRepository;
import com.gearfirst.warehouse.api.shipping.persistence.ShippingQueryRepository;
import com.gearfirst.warehouse.common.exception.BadRequestException;
import com.gearfirst.warehouse.common.response.ErrorStatus;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SummaryServiceImpl implements SummaryService {

    private final ReceivingQueryRepository receivingQueryRepository;
    private final ShippingQueryRepository shippingQueryRepository;

    @Override
    public NoteCountsByDateResponse countNotesByDate(String requestDate) {
        LocalDate day;
        try {
            day = (requestDate == null || requestDate.isBlank()) ? null : LocalDate.parse(requestDate);
        } catch (Exception e) {
            throw new BadRequestException(ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION);
        }
        if (day == null) {
            throw new BadRequestException(ErrorStatus.VALIDATION_REQUEST_MISSING_EXCEPTION);
        }
        long receiving = receivingQueryRepository.countByRequestedAtDateKst(day);
        long shipping = shippingQueryRepository.countByRequestedAtDateKst(day);
        return new NoteCountsByDateResponse(day.toString(), receiving, shipping);
    }
}
