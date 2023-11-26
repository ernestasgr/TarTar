package org.tartar.visitor;

import org.antlr.v4.runtime.misc.Pair;
import org.tartar.visitor.exception.TarTarVariableAlreadyDeclaredException;
import org.tartar.visitor.exception.TarTarVariableNotDeclaredException;

import java.util.HashMap;
import java.util.Map;
public class TarTarScope {
    String[] ValKeeper = new String[1000];
    int i = 1;
    private final TarTarScope parent;
    private final Map<String, Pair<Object, String>> symbols = new HashMap<>();

    public TarTarScope() {
        this.parent = null;
    }

    public TarTarScope(TarTarScope parent) {
        this.parent = parent;
    }

    public void declareVariable(String variableName, Object value, String dataType) {
        Pair<Object, String> pair = new Pair<>(value, dataType);
        if (isDeclared(variableName)) {
            throw new TarTarVariableAlreadyDeclaredException(variableName);
        }
        symbols.put(variableName, pair);
    }

    public boolean isVal(String variableName,String valConfirmation) {
        ValKeeper[0]="";
        if (valConfirmation=="val")
        {
            ValKeeper[i]=variableName;
            i=i+1;
            return true;
        }
        for(int j=0; j<i; j++) {
            if (ValKeeper[j].equals(variableName)) {
                return true;
            }
        }
            return false;
    }

    private boolean isDeclared(String variableName) {
        if (symbols.containsKey(variableName)) {
            return true;
        }
        return parent != null && parent.isDeclared(variableName);
    }

    public void changeVariable(String variableName, Object value, String dataType, String valConfirmation) {
        if(isVal(variableName, valConfirmation)){
            return;
        }
        else {
            Pair<Object, String> pair = new Pair<>(value, dataType);
            if (!isDeclared(variableName)) {
                throw new TarTarVariableNotDeclaredException(variableName);
            }
            if (symbols.containsKey(variableName)) {
                symbols.put(variableName, pair);
            } else {
                assert parent != null;
                parent.changeVariable(variableName, value, dataType, valConfirmation);
            }
        }
    }

    public Pair<Object, String> resolveVariable(String variableName) {
        if (!isDeclared(variableName)) {
            throw new TarTarVariableNotDeclaredException(variableName);
        }
        if (symbols.containsKey(variableName)) {
            return symbols.get(variableName);
        } else {
            assert parent != null;
            return parent.resolveVariable(variableName);
        }
    }
}
