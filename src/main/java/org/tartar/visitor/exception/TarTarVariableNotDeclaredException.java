package org.tartar.visitor.exception;

public class TarTarVariableNotDeclaredException extends TarTarException {
    public TarTarVariableNotDeclaredException(String variableNme) {
        super(String.format("Variable '%s' is not declared.", variableNme));
    }
}
