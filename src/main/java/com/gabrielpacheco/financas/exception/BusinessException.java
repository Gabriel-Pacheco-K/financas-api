package com.gabrielpacheco.financas.exception;

/**
 * Violação de uma regra de negócio, como e-mail duplicado ou categoria em uso.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
