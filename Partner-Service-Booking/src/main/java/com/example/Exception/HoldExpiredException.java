package com.example.Exception;

import com.example.Enum.PartnerErrorCode;

public class HoldExpiredException extends AppException {

    public HoldExpiredException() {
        super(PartnerErrorCode.HOLD_EXPIRED);
    }
}
