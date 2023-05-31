package com.amerikano.dividendsproject.exception.impl;

import com.amerikano.dividendsproject.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class UserNotExistException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "존재하지 않는 사용자명입니다.";
    }
}
