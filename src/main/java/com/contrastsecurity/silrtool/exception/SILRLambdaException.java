package com.contrastsecurity.silrtool.exception;

import software.amazon.awssdk.services.lambda.model.LambdaException;

public class SILRLambdaException extends Exception {
    private static final long serialVersionUID = 1L;

    private String funcName;
    private LambdaException lambdaException;

    public SILRLambdaException(String funcName, LambdaException le) {
        super(le.getMessage());
        this.funcName = funcName;
        this.lambdaException = le;
    }

    public String getFuncName() {
        return funcName;
    }

    public LambdaException getLambdaException() {
        return lambdaException;
    }

}
