package com.gearfirst.warehouse.api.summary;

import com.gearfirst.warehouse.api.dto.NoteCountsByDateResponse;

public interface SummaryService {
    NoteCountsByDateResponse countNotesByDate(String requestDate);
}
