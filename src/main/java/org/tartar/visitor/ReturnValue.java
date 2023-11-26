package org.tartar.visitor;

public record ReturnValue(Object value) {

    public Object getValue() {
        return value;
    }
}
