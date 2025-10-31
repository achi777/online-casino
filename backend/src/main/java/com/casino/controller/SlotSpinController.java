package com.casino.controller;

import com.casino.dto.SpinRequest;
import com.casino.dto.SpinResponse;
import com.casino.service.SlotSpinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/games")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SlotSpinController {

    private final SlotSpinService slotSpinService;

    @PostMapping("/spin")
    public ResponseEntity<SpinResponse> spin(@RequestBody SpinRequest request) {
        SpinResponse response = slotSpinService.processSpin(request);
        return ResponseEntity.ok(response);
    }
}
