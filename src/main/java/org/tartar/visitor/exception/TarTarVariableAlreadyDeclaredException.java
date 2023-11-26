package org.tartar.visitor.exception;

public class TarTarVariableAlreadyDeclaredException extends TarTarException {
    public TarTarVariableAlreadyDeclaredException(String variableNme) {
        super(String.format("Variable '%s' is already declared.", variableNme));
    }
}