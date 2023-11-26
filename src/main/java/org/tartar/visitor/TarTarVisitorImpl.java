package org.tartar.visitor;

import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.tree.RuleNode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class TarTarVisitorImpl extends TarTarBaseVisitor<Object> {

    private final StringBuilder SYSTEM_OUT = new StringBuilder();

    private final Stack<TarTarScope> scopeStack = new Stack<>();

    private TarTarScope currentScope = new TarTarScope();

    private final Map<String, TarTarParser.FunctionDeclarationContext> functions = new HashMap<>();

    @Override
    public Object visitProgram(TarTarParser.ProgramContext ctx) {
        super.visitProgram(ctx);
        return SYSTEM_OUT.toString();
    }

    @Override
    public Object visitPrintFunctionCall(TarTarParser.PrintFunctionCallContext ctx) {
        Object obj = visit(ctx.expression());
        String text = obj.toString();
        if(text.startsWith("\"") && text.endsWith("\"")) {
            text = text.substring(1, text.length() - 1);
        }
        System.out.println(text);
        SYSTEM_OUT.append(text).append("\n");
        //return null;
        return text;
    }

    @Override
    public Object visitConstantExpression(TarTarParser.ConstantExpressionContext ctx) {
        return visit(ctx.constant());
    }

    @Override
    public Object visitConstant(TarTarParser.ConstantContext ctx) {
        if (ctx.INT() != null) {
            return Integer.parseInt(ctx.INT().getText());
        }
        if (ctx.BOOL() != null) {
            return Boolean.parseBoolean(ctx.BOOL().getText());
        }
        if (ctx.STRING() != null) {
            return ctx.STRING().getText();
        }
        if (ctx.REAL() != null) {
            return Double.parseDouble(ctx.REAL().getText());
        }
        return null;
    }

    @Override
    public Object visitVariableDeclaration(TarTarParser.VariableDeclarationContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        String dataType = ctx.dataType().getText();
        Object value = visit(ctx.expression());
        if(value instanceof String && ((String) value).startsWith("\"") && ((String) value).endsWith("\"")) {
            value = ((String) value).substring(1, ((String) value).length() - 1);
        }
        this.currentScope.declareVariable(varName, value, dataType);
        return new Pair<>(value, dataType);
    }

    @Override
    public Object visitImmutableVariableDeclaration(TarTarParser.ImmutableVariableDeclarationContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        String dataType = ctx.dataType().getText();
        Object value = visit(ctx.expression());
        String valConfirmation = "val";
        if(value instanceof String && ((String) value).startsWith("\"") && ((String) value).endsWith("\"")) {
            value = ((String) value).substring(1, ((String) value).length() - 1);
        }
        this.currentScope.declareVariable(varName, value, dataType);
        this.currentScope.isVal(varName, valConfirmation);
        return new Pair<>(value, dataType);
    }

    @Override
    public Object visitVariableDeclarationWithoutSemicolon(TarTarParser.VariableDeclarationWithoutSemicolonContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        String dataType = ctx.dataType().getText();
        Object value = new Object();
        switch (dataType) {
            case "INT" -> value = 0;
            case "REAL" -> value = 0;
            case "BOOL" -> value = 0;
            case "STRING" -> value = "";
            case "_" -> {
            }
        }
        this.currentScope.declareVariable(varName, value, dataType);
        return new Pair<>(value, dataType);
    }

    @Override
    public Object visitVariableDeclarationWithSemicolon(TarTarParser.VariableDeclarationWithSemicolonContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        String dataType = ctx.dataType().getText();
        Object value = new Object();
        switch (dataType) {
            case "INT" -> value = 0;
            case "REAL" -> value = 0;
            case "BOOL" -> value = 0;
            case "STRING" -> value = "";
            case "_" -> {
            }
        }
        this.currentScope.declareVariable(varName, value, dataType);
        return new Pair<>(value, dataType);
    }

    @Override
    public Object visitAssignment(TarTarParser.AssignmentContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        Object value = visit(ctx.expression());
        if(value instanceof String && ((String) value).startsWith("\"") && ((String) value).endsWith("\"")) {
            value = ((String) value).substring(1, ((String) value).length() - 1);
        }
        Object variable = this.currentScope.resolveVariable(varName).a;
        String dataType = this.currentScope.resolveVariable(varName).b;
        boolean fIsInt = variable instanceof Integer;
        boolean sIsInt = value instanceof Integer;
        boolean fIsReal = variable instanceof Double;
        boolean sIsReal = value instanceof Double;
        if((fIsInt || fIsReal) && (sIsInt || sIsReal)) {
            switch (ctx.assignmentOp().getText()) {
                case "=" -> {
                    if(fIsInt) {
                        if(sIsInt) {
                            this.currentScope.changeVariable(varName, value, "int", "");
                        } else {
                            this.currentScope.changeVariable(varName, (int)value, "int", "");
                        }
                    } else {
                        if(sIsInt) {
                            this.currentScope.changeVariable(varName, (double)value, "real", "");
                        } else {
                            this.currentScope.changeVariable(varName, value, "real", "");
                        }
                    }
                }
                case "+=" -> {
                    if(fIsInt) {
                        this.currentScope.changeVariable(varName, (int)variable + (int)value, "int", "");
                    } else {
                        this.currentScope.changeVariable(varName, (double)variable + Double.parseDouble(value.toString()), "real", "");
                    }
                }
                case "-=" -> {
                    if(fIsInt) {
                        this.currentScope.changeVariable(varName, (int)variable - (int)value, "int", "");
                    } else {
                        this.currentScope.changeVariable(varName, (double)variable - Double.parseDouble(value.toString()), "real", "");
                    }
                }
                case "*=" -> {
                    if(fIsInt) {
                        this.currentScope.changeVariable(varName, (int)((int)variable * Double.parseDouble(value.toString())), "int", "");
                    } else {
                        this.currentScope.changeVariable(varName, (double)variable * Double.parseDouble(value.toString()), "real", "");
                    }
                }
                case "/=" -> {
                    if(fIsInt) {
                        this.currentScope.changeVariable(varName, (int)((int)variable / Double.parseDouble(value.toString())), "int", "");
                    } else {
                        this.currentScope.changeVariable(varName, (double)variable / Double.parseDouble(value.toString()), "real", "");
                    }
                }
                case "%=" -> {
                    if(fIsInt) {
                        this.currentScope.changeVariable(varName, (int)((int)variable % Double.parseDouble(value.toString())), "int", "");
                    } else {
                        this.currentScope.changeVariable(varName, (double)variable % Double.parseDouble(value.toString()), "real", "");
                    }
                }
                default -> {
                }
            }
        }
        else if (variable instanceof String && value instanceof String) {
            switch (ctx.assignmentOp().getText()) {
                case "=":
                    this.currentScope.changeVariable(varName, value, "string", "");
                case "+=":
                    this.currentScope.changeVariable(varName,
                            variable.toString().concat(value.toString()), "string", "");
            }
        }
        else if(variable instanceof Boolean && value instanceof Boolean) {
            switch (ctx.assignmentOp().getText()) {
                case "=":
                    this.currentScope.changeVariable(varName, value, "bool", "");
            }
        }
        return null;
    }

    @Override
    public Object visitIdentifierExpression(TarTarParser.IdentifierExpressionContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        return this.currentScope.resolveVariable(varName).a;
    }

    @Override
    public Object visitAddOpExpression(TarTarParser.AddOpExpressionContext ctx) {
        Object val1 = visit(ctx.expression(0));
        Object val2 = visit(ctx.expression(1));
        boolean val1S = val1 instanceof String;
        boolean val2S = val2 instanceof String;
        if(val1S && val2S) {
            return switch (ctx.addOp().getText()) {
                case "+" -> {
                    var val1s = val1.toString();
                    var val2s = val2.toString();
                    if(val1s.startsWith("\"") && val1s.endsWith("\"")) {
                        val1s = val1s.substring(1, val1s.length() - 1);
                    }
                    if(val2s.startsWith("\"") && val2s.endsWith("\"")) {
                        val2s = val2s.substring(1, val2s.length() - 1);
                    }
                    yield val1s.concat(val2s);
                }
                default -> null;
            };
        }
        boolean val1D = val1 instanceof Double;
        boolean val2D = val2 instanceof Double;
        return switch (ctx.addOp().getText()) {
            case "+" -> {
                if(val1D) {
                    if(val2D) {
                        yield (double)val1 + (double) val2;
                    }
                    yield (double)val1 + (int)val2;
                } else {
                    if(val2D) {
                        yield (int)val1 + (double)val2;
                    }
                    yield (int )val1 + (int) val2;
                }
            }
            default -> null;
        };
    }

    @Override
    public Object visitNumericMinusOpExpression(TarTarParser.NumericMinusOpExpressionContext ctx) {
        Object val1 = visit(ctx.expression(0));
        Object val2 = visit(ctx.expression(1));
        boolean val1D = val1 instanceof Double;
        boolean val2D = val2 instanceof Double;
        return switch (ctx.numericMinusOp().getText()) {
            case "-" -> {
                if(val1D) {
                    if(val2D) {
                        yield (double)val1 - (double) val2;
                    }
                    yield (double)val1 - (int)val2;
                } else {
                    if(val2D) {
                        yield (int)val1 - (double)val2;
                    }
                    yield (int )val1 - (int) val2;
                }
            }
            default -> null;
        };
    }

    @Override
    public Object visitNumericMultiOpExpression(TarTarParser.NumericMultiOpExpressionContext ctx) {
        Object val1 = visit(ctx.expression(0));
        Object val2 = visit(ctx.expression(1));
        boolean val1D = val1 instanceof Double;
        boolean val2D = val2 instanceof Double;
        return switch (ctx.numericMultiOp().getText()) {
            case "*" -> {
                if(val1D) {
                    if(val2D) {
                        yield (double)val1 * (double) val2;
                    }
                    yield (double)val1 * (int)val2;
                } else {
                    if(val2D) {
                        yield (int)val1 * (double)val2;
                    }
                    yield (int )val1 * (int) val2;
                }
            }
            case "/" -> {
                if(val1D) {
                    if(val2D) {
                        yield (double)val1 / (double) val2;
                    }
                    yield (double)val1 / (int)val2;
                } else {
                    if(val2D) {
                        yield (int)val1 / (double)val2;
                    }
                    yield (int )val1 / (int) val2;
                }
            }
            case "%" -> {
                if(val1D) {
                    if(val2D) {
                        yield (double)val1 % (double) val2;
                    }
                    yield (double)val1 % (int)val2;
                } else {
                    if(val2D) {
                        yield (int)val1 % (double)val2;
                    }
                    yield (int )val1 % (int) val2;
                }
            }
            default -> null;
        };
    }

    @Override
    public Object visitCompareOpExpression(TarTarParser.CompareOpExpressionContext ctx) {
        Object val1 = visit(ctx.expression(0));
        Object val2 = visit(ctx.expression(1));
        boolean val1S = val1 instanceof String;
        boolean val2S = val2 instanceof String;
        boolean val1B = val1 instanceof Boolean;
        boolean val2B = val2 instanceof Boolean;
        if(val1S && val2S) {
            if(((String) val1).startsWith("\"") && ((String) val1).endsWith("\"")) {
                val1 = ((String) val1).substring(1, ((String) val1).length() - 1);
            }
            if(((String) val2).startsWith("\"") && ((String) val2).endsWith("\"")) {
                val2 = ((String) val2).substring(1, ((String) val2).length() - 1);
            }
            return switch (ctx.compareOp().getText()) {
                case "<" -> val1.toString().compareTo(val2.toString()) < 0;
                case "<=" -> val1.toString().compareTo(val2.toString()) <= 0;
                case ">" -> val1.toString().compareTo(val2.toString()) > 0;
                case ">=" -> val1.toString().compareTo(val2.toString()) >= 0;
                case "==" -> val1.toString().compareTo(val2.toString()) == 0;
                case "!=" -> val1.toString().compareTo(val2.toString()) != 0;
                default -> null;
            };
        } else if(val1B && val2B) {
            return switch (ctx.compareOp().getText()) {
                case "==" -> val1.toString().compareTo(val2.toString()) == 0;
                case "!=" -> val1.toString().compareTo(val2.toString()) != 0;
                default -> null;
            };
        } else{
            double val1N = Double.parseDouble(val1.toString());
            double val2N = Double.parseDouble(val2.toString());
            double epsilon = 0.00001;
            return switch (ctx.compareOp().getText()) {
                case "<" -> val1N < val2N;
                case "<=" -> val1N < val2N || Math.abs(val1N - val2N) < epsilon;
                case ">" -> val1N > val2N;
                case ">=" -> val1N > val2N || Math.abs(val1N - val2N) < epsilon;
                case "==" -> Math.abs(val1N - val2N) < epsilon;
                case "!=" -> Math.abs(val1N - val2N) >= epsilon;
                default -> null;
            };
        }
    }

    public boolean visitCompareOpExpression(TarTarParser.ExpressionContext ec1, String coc, TarTarParser.ExpressionContext ec2) {
        Object val1 = visit(ec1);
        Object val2 = visit(ec2);
        boolean val1S = val1 instanceof String;
        boolean val2S = val2 instanceof String;
        if(val1S || val2S) {
            return switch (coc) {
                case "<" -> val1.toString().compareTo(val2.toString()) < 0;
                case "<=" -> val1.toString().compareTo(val2.toString()) <= 0;
                case ">" -> val1.toString().compareTo(val2.toString()) > 0;
                case ">=" -> val1.toString().compareTo(val2.toString()) >= 0;
                case "==" -> val1.toString().compareTo(val2.toString()) == 0;
                case "!=" -> val1.toString().compareTo(val2.toString()) != 0;
                default -> false;
            };
        } else{
            double val1N = Double.parseDouble(val1.toString());
            double val2N = Double.parseDouble(val2.toString());
            double epsilon = 0.00001;
            return switch (coc) {
                case "<" -> val1N < val2N;
                case "<=" -> val1N < val2N || Math.abs(val1N - val2N) < epsilon;
                case ">" -> val1N > val2N;
                case ">=" -> val1N > val2N || Math.abs(val1N - val2N) < epsilon;
                case "==" -> Math.abs(val1N - val2N) < epsilon;
                case "!=" -> Math.abs(val1N - val2N) >= epsilon;
                default -> false;
            };
        }
    }

    @Override
    public Object visitBooleanBinaryOpExpression(TarTarParser.BooleanBinaryOpExpressionContext ctx) {
        Object val1 = visit(ctx.expression(0));
        Object val2 = visit(ctx.expression(1));
        return switch (ctx.booleanBinaryOp().getText()) {
            case "and" -> (Boolean)val1 && (Boolean)val2;
            case "or" -> (Boolean)val1 || (Boolean)val2;
            default -> null;
        };
    }

    @Override
    public Object visitBooleanUnaryOpExpression(TarTarParser.BooleanUnaryOpExpressionContext ctx) {
        Object val1 = visit(ctx.expression());
        return switch (ctx.booleanUnaryOp().getText()) {
            case "not" -> !((Boolean)val1);
            default -> null;
        };
    }

    @Override
    public Object visitIdentifierStringify(TarTarParser.IdentifierStringifyContext ctx) {
        Object val1 = visit(ctx.expression());
        return val1.toString();
    }


    @Override
    public Object visitIfElseStatement(TarTarParser.IfElseStatementContext ctx) {
        boolean value = (Boolean) visit(ctx.expression());
        if (value) {
            return visit(ctx.block(0));
        } else if (ctx.block().size() == 2){
            return visit(ctx.block(1));
        }
        return null;
    }

    @Override
    public Object visitForAssignmentStatement(TarTarParser.ForAssignmentStatementContext ctx) {
        visit(ctx.assignment(0));
        while(visitCompareOpExpression(ctx.expression(0), ctx.compareOp().getText(), ctx.expression(1))) {
            visit(ctx.block());
            visit(ctx.assignment(1));
        }
        return null;
    }

    @Override
    public Object visitForVariableDeclarationStatement(TarTarParser.ForVariableDeclarationStatementContext ctx) {
        visit(ctx.variableDeclaration());
        while(visitCompareOpExpression(ctx.expression(0), ctx.compareOp().getText(), ctx.expression(1))) {
            visit(ctx.block());
            visit(ctx.assignment());
        }
        return null;
    }

    @Override
    public Object visitWhileStatement(TarTarParser.WhileStatementContext ctx) {
        Object val = visit(ctx.expression());
        boolean condition = val instanceof Boolean;
        if(condition) {
            while ((boolean)val) {
                visit(ctx.block());
                val = visit(ctx.expression());
            }
        }
        return null;
    }

    @Override
    public Object visitForeachStatement(TarTarParser.ForeachStatementContext ctx) {
        visit(ctx.variableDeclarationWithoutSemicolon());
        String varName = ctx.IDENTIFIER().getText();
        Pair<Object, String > pair = this.currentScope.resolveVariable(varName);
        Object variable = pair.a;
        String index = ctx.children.get(2).getChild(2).toString();
        String indexType = ctx.children.get(2).getChild(1).getChild(0).toString();
        if (variable instanceof ArrayList) {
            for (var i : ((ArrayList<?>)variable)) {
                this.currentScope.changeVariable(index, i, indexType, "");
                visit(ctx.block());
            }
        }
        return null;
    }

    @Override
    public Object visitForeachStatementForHashMap(TarTarParser.ForeachStatementForHashMapContext ctx) {
        visit(ctx.variableDeclarationWithoutSemicolon());
        visit(ctx.variableDeclarationWithSemicolon());
        String varName = ctx.IDENTIFIER().getText();
        Pair<Object, String > pair = this.currentScope.resolveVariable(varName);
        Object variable = pair.a;
        String index1 = ctx.children.get(2).getChild(2).toString();
        String indexType1 = ctx.children.get(2).getChild(1).getChild(0).toString();
        String index2 = ctx.children.get(3).getChild(2).toString();
        String indexType2 = ctx.children.get(3).getChild(1).getChild(0).toString();
        if (variable instanceof HashMap) {
            for (Map.Entry i : ((HashMap<?, ?>) variable).entrySet()) {
                this.currentScope.changeVariable(index1, i.getKey(), indexType1, "");
                this.currentScope.changeVariable(index2, i.getValue(), indexType2, "");
                visit(ctx.block());
            }
        }
        return null;
    }

    @Override
    public Object visitBlock(TarTarParser.BlockContext ctx) {
        scopeStack.push(currentScope);
        currentScope = new TarTarScope(currentScope);
        Object value = super.visitBlock(ctx);
        currentScope = scopeStack.pop();
        return value;
    }

    @Override
    public Object visitParenthesesExpression(TarTarParser.ParenthesesExpressionContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public Object visitReturnStatement(TarTarParser.ReturnStatementContext ctx) {
        if (ctx.expression() == null) {
            return new ReturnValue(null);
        } else {
            return new ReturnValue(this.visit(ctx.expression()));
        }
    }

    @Override
    protected boolean shouldVisitNextChild(RuleNode node, Object currentResult) {
        return !(currentResult instanceof ReturnValue);
        //return true;
    }

    @Override
    public Object visitFunctionDeclaration(TarTarParser.FunctionDeclarationContext ctx) {
        String functionName = ctx.IDENTIFIER().getText();

        //TODO create Function class that has constructor(FunctionDeclarationContext), invoke method
        //TODO validate if does not exist
        //TODO probably something else
        this.functions.put(functionName, ctx);
        return null;
    }

    @Override
    public Object visitFunctionCall(TarTarParser.FunctionCallContext ctx) {
        String functionName = ctx.IDENTIFIER().getText();
        TarTarParser.FunctionDeclarationContext function = this.functions.get(functionName);
        if(function != null) {
            List<Object> arguments = new ArrayList<>();
            if (ctx.expressionList() != null) {
                for (var expr : ctx.expressionList().expression()) {
                    arguments.add(this.visit(expr));
                }
            }

            TarTarScope functionScope = new TarTarScope();

            if (function.paramList() != null) {
                for (int i = 0; i < function.paramList().IDENTIFIER().size(); i++) {
                    String paramName = function.paramList().IDENTIFIER(i).getText();
                    String dataType = function.paramList().dataType(i).getText();
                    functionScope.declareVariable(paramName, arguments.get(i), dataType);
                }
            }

            scopeStack.push(currentScope);
            currentScope = functionScope;
            ReturnValue value = (ReturnValue) this.visitFunctionBody(function.functionBody());
            currentScope = scopeStack.pop();

            return value.getValue();
        }
        return null;
    }

    @Override
    public Object visitFunctionBody(TarTarParser.FunctionBodyContext ctx) {
        Object value = super.visitFunctionBody(ctx);
        if (value instanceof ReturnValue) {
            return value;
        }
        return new ReturnValue(null);
    }

    @Override
    public Object visitPatternMatchStatement(TarTarParser.PatternMatchStatementContext ctx) {
        List<Object> arguments = new ArrayList<>();
        if (ctx.expressionList() != null) {
            for (var expr : ctx.expressionList().expression()) {
                arguments.add(this.visit(expr));
            }
        }

        int index = 0;
        if(ctx.patternMatchBlock() != null) {
            for(var patternCase : ctx.patternMatchBlock().patternMatchCase()) {
                boolean caseValid = true;
                for(var argument : patternCase.expressionList().expression()) {
                    if(argument instanceof TarTarParser.IdentifierExpressionContext) {
                        String type = argument.children.get(0).getText();
                        switch (type) {
                            case "INT" -> caseValid = arguments.get(index) instanceof Integer;
                            case "REAL" -> caseValid = arguments.get(index) instanceof Double;
                            case "BOOL" -> caseValid = arguments.get(index) instanceof Boolean;
                            case "STRING" -> caseValid = arguments.get(index) instanceof String;
                            case "_" -> {

                            }
                        }
                    } else if (argument instanceof TarTarParser.ConstantExpressionContext) {
                        Object obj = arguments.get(index);
                        if(obj instanceof Integer) {
                            caseValid = obj.equals(Integer.parseInt(argument.children.get(0).getText()));
                        } else if(obj instanceof Double) {
                            caseValid = obj.equals(Double.parseDouble(argument.children.get(0).getText()));
                        } else if(obj instanceof Boolean) {
                            caseValid = obj.equals(Boolean.parseBoolean(argument.children.get(0).getText()));
                        } else if(obj instanceof String) {
                            var str = argument.children.get(0).getText();
                            if(str.startsWith("\"") && str.endsWith("\"")) {
                                str = str.substring(1, str.length() - 1);
                            }
                            caseValid = obj.equals(str);
                        }
                    } else {
                        caseValid = (boolean) this.visit(argument);
                    }
                    if(!caseValid) {
                        break;
                    }
                    index++;
                }
                if(caseValid) {
                    return this.visitBlock(patternCase.block());
                } else {
                    index = 0;
                }
            }
        }
        return this.visitBlock(ctx.patternMatchBlock().patternMatchDefault().block());
    }

    @Override
    public Object visitFileReadFunctionCall(TarTarParser.FileReadFunctionCallContext ctx) {
        Object fileName = visit(ctx.expression());
        if(fileName instanceof String) {
            try {
                String fileNameS = ((String) fileName);
                if(fileNameS.startsWith("\"") && fileNameS.endsWith("\"")) {
                    fileNameS = fileNameS.substring(1, fileNameS.length() - 1);
                }
                return Files.readString(Paths.get(fileNameS), StandardCharsets.UTF_8);
            } catch (IOException exception) {
                System.out.println("Failed to read file " + fileName);
            }
        }
        return null;
    }

    @Override
    public Object visitFilePrintFunctionCall(TarTarParser.FilePrintFunctionCallContext ctx) {
        Object fileName = visit(ctx.expression(0));
        Object contents = visit(ctx.expression(1));
        if(fileName instanceof String && contents instanceof String) {
            try {
                String fileNameS = ((String) fileName);
                if(fileNameS.startsWith("\"") && fileNameS.endsWith("\"")) {
                    fileNameS = fileNameS.substring(1, fileNameS.length() - 1);
                }
                String contentsS = contents.toString().replace("\\n", System.lineSeparator());
                FileWriter writer = new FileWriter(fileNameS, false);
                writer.write(contentsS);
                writer.close();
            } catch (IOException exception) {
                System.out.println("Failed to write to file " + fileName);
            }
        }
        return null;
    }

    @Override
    public Object visitReadFunctionCall(TarTarParser.ReadFunctionCallContext ctx) {
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));
        try {
            var contents = reader.readLine();
            var dataType = ((TarTarParser.DataTypeContext) ((TarTarParser.VariableDeclarationContext) ctx.parent).children.get(1)).children.get(0).getText();
            switch (dataType) {
                case "int" -> {
                    return Integer.parseInt(contents);
                }
                case "real" -> {
                    return Double.parseDouble(contents);
                }
                case "bool" -> {
                    return Boolean.parseBoolean(contents);
                }
                case "string" -> {
                    return contents;
                }
            }
        } catch (IOException exception) {
            System.out.println("Failed to read from console");
        }
        return null;
    }

    @Override
    public Object visitArrayDeclaration(TarTarParser.ArrayDeclarationContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        String dataType = ctx.dataType().getText();
        switch (dataType) {
            case "int" -> {
                ArrayList<Integer> list = new ArrayList<>();
                this.currentScope.declareVariable(varName, list, dataType);
                return list;
            }
            case "real" -> {
                ArrayList<Double> list = new ArrayList<>();
                this.currentScope.declareVariable(varName, list, dataType);
                return list;
            }
            case "string" -> {
                ArrayList<String> list = new ArrayList<>();
                this.currentScope.declareVariable(varName, list, dataType);
                return list;
            }
            case "bool" -> {
                ArrayList<Boolean> list = new ArrayList<>();
                this.currentScope.declareVariable(varName, list, dataType);
                return list;
            }
        }
        return null;
    }

    @Override
    public Object visitDictionaryDeclaration(TarTarParser.DictionaryDeclarationContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        String dataTypeA = ctx.dataType(0).getText();
        String dataTypeB = ctx.dataType(1).getText();
        switch (dataTypeA) {
            case "int" -> {
                switch (dataTypeB) {
                    case "int" -> {
                        HashMap<Integer, Integer> map = new HashMap<>();
                        this.currentScope.declareVariable(varName, map, "int,int");
                        return map;
                    }
                    case "real" -> {
                        HashMap<Integer, Double> map = new HashMap<>();
                        this.currentScope.declareVariable(varName, map, "int,real");
                        return map;
                    }
                    case "string" -> {
                        HashMap<Integer, String> map = new HashMap<>();
                        this.currentScope.declareVariable(varName, map, "int,string");
                        return map;
                    }
                    case "bool" -> {
                        HashMap<Integer, Boolean> map = new HashMap<>();
                        this.currentScope.declareVariable(varName, map, "int,bool");
                        return map;
                    }
                }
                return null;
            }
            case "real" -> {
                switch (dataTypeB) {
                    case "int" -> {
                        HashMap<Double, Integer> map = new HashMap<>();
                        this.currentScope.declareVariable(varName, map, "real,int");
                        return map;
                    }
                    case "real" -> {
                        HashMap<Double, Double> map = new HashMap<>();
                        this.currentScope.declareVariable(varName, map, "real,real");
                        return map;
                    }
                    case "string" -> {
                        HashMap<Double, String> map = new HashMap<>();
                        this.currentScope.declareVariable(varName, map, "real,string");
                        return map;
                    }
                    case "bool" -> {
                        HashMap<Double, Boolean> map = new HashMap<>();
                        this.currentScope.declareVariable(varName, map, "real,bool");
                        return map;
                    }
                }
                return null;
            }
            case "string" -> {
                switch (dataTypeB) {
                    case "int" -> {
                        HashMap<String, Integer> map = new HashMap<>();
                        this.currentScope.declareVariable(varName, map, "string,int");
                        return map;
                    }
                    case "real" -> {
                        HashMap<String, Double> map = new HashMap<>();
                        this.currentScope.declareVariable(varName, map, "string,real");
                        return map;
                    }
                    case "string" -> {
                        HashMap<String, String> map = new HashMap<>();
                        this.currentScope.declareVariable(varName, map, "string,string");
                        return map;
                    }
                    case "bool" -> {
                        HashMap<String, Boolean> map = new HashMap<>();
                        this.currentScope.declareVariable(varName, map, "string,bool");
                        return map;
                    }
                }
                return null;
            }
            case "bool" -> {
                switch (dataTypeB) {
                    case "int" -> {
                        HashMap<Boolean, Integer> map = new HashMap<>();
                        this.currentScope.declareVariable(varName, map, "bool,int");
                        return map;
                    }
                    case "real" -> {
                        HashMap<Boolean, Double> map = new HashMap<>();
                        this.currentScope.declareVariable(varName, map, "bool,real");
                        return map;
                    }
                    case "string" -> {
                        HashMap<Boolean, String> map = new HashMap<>();
                        this.currentScope.declareVariable(varName, map, "bool,string");
                        return map;
                    }
                    case "bool" -> {
                        HashMap<Boolean, Boolean> map = new HashMap<>();
                        this.currentScope.declareVariable(varName, map, "bool,bool");
                        return map;
                    }
                }
                return null;
            }
        }
        return null;
    }

    @Override
    public Object visitSize(TarTarParser.SizeContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        Object variable = this.currentScope.resolveVariable(varName).a;
        if(variable instanceof ArrayList) {
            return ((ArrayList<?>)variable).size();
        }
        if(variable instanceof HashMap) {
            return ((HashMap<?, ?>)variable).size();
        }
        if(variable instanceof String) {
            return ((String)variable).length();
        }
        return null;
    }

    @Override
    public Object visitAdd(TarTarParser.AddContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        Pair<Object, String > pair = this.currentScope.resolveVariable(varName);
        Object variable = pair.a;
        Object value = visit(ctx.expression());
        String dataTypeA = pair.b;
        if(value instanceof Integer && !Objects.equals(dataTypeA, "int") ||
                value instanceof Double && !Objects.equals(dataTypeA, "real") ||
                value instanceof String && !Objects.equals(dataTypeA, "string") ||
                value instanceof Boolean && !Objects.equals(dataTypeA, "bool")) {
            return null;
        }
        if(!(value instanceof Integer) && !(value instanceof Double) && !(value instanceof String) && !(value instanceof Boolean)) {
            return null;
        }

        if(variable instanceof ArrayList) {
            switch (dataTypeA) {
                case "int" -> ((ArrayList<Integer>)variable).add((Integer) value);
                case "real" -> ((ArrayList<Double>)variable).add((Double) value);
                case "string" -> ((ArrayList<String>)variable).add((String) value);
                case "bool" -> ((ArrayList<Boolean>)variable).add((Boolean) value);
            }
        }
        return null;
    }

    @Override
    public Object visitAddRange(TarTarParser.AddRangeContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        Pair<Object, String > pair = this.currentScope.resolveVariable(varName);
        Object variable = pair.a;
        Object value = visit(ctx.expression());
        String dataTypeA = pair.b;
        String dataTypeB = this.currentScope.resolveVariable(ctx.expression().children.get(0).getText()).b;
        if(variable instanceof ArrayList && value instanceof ArrayList && dataTypeA.equals(dataTypeB)) {
            switch (dataTypeA) {
                case "int" -> ((ArrayList<Integer>)variable).addAll((Collection<? extends Integer>) value);
                case "real" -> ((ArrayList<Double>)variable).addAll((Collection<? extends Double>) value);
                case "string" -> ((ArrayList<String>)variable).addAll((Collection<? extends String>) value);
                case "bool" -> ((ArrayList<Boolean>)variable).addAll((Collection<? extends Boolean>) value);
            }
        }
        return null;
    }

    @Override
    public Object visitRemove(TarTarParser.RemoveContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        Pair<Object, String > pair = this.currentScope.resolveVariable(varName);
        Object variable = pair.a;
        Object value = visit(ctx.expression());
        String dataTypeA = pair.b;
        if(value instanceof Integer && !Objects.equals(dataTypeA, "int") ||
                value instanceof Double && !Objects.equals(dataTypeA, "real") ||
                value instanceof String && !Objects.equals(dataTypeA, "string") ||
                value instanceof Boolean && !Objects.equals(dataTypeA, "bool")) {
            return null;
        }
        if(!(value instanceof Integer) && !(value instanceof Double) && !(value instanceof String) && !(value instanceof Boolean)) {
            return null;
        }

        if(variable instanceof ArrayList) {
            switch (dataTypeA) {
                case "int" -> ((ArrayList<Integer>)variable).remove((Integer) value);
                case "real" -> ((ArrayList<Double>)variable).remove((Double) value);
                case "string" -> ((ArrayList<String>)variable).remove((String) value);
                case "bool" -> ((ArrayList<Boolean>)variable).remove((Boolean) value);
            }
        }
        if(variable instanceof HashMap) {
            switch (dataTypeA) {
                case "int" -> ((HashMap<Integer, ?>)variable).remove((Integer) value);
                case "real" -> ((HashMap<Double, ?>)variable).remove((Double) value);
                case "string" -> ((HashMap<String, ?>)variable).remove((String) value);
                case "bool" -> ((HashMap<Boolean, ?>)variable).remove((Boolean) value);
            }
        }
        return null;
    }

    @Override
    public Object visitRemoveAt(TarTarParser.RemoveAtContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        Pair<Object, String > pair = this.currentScope.resolveVariable(varName);
        Object variable = pair.a;
        Object value = visit(ctx.expression());
        if(!(value instanceof Integer)) {
            return null;
        }
        int index = (Integer)value;
        if(variable instanceof ArrayList) {
            ((ArrayList<?>)variable).remove(index);
        }
        return null;
    }

    @Override
    public Object visitRemoveAll(TarTarParser.RemoveAllContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        Pair<Object, String > pair = this.currentScope.resolveVariable(varName);
        Object variable = pair.a;
        Object value = visit(ctx.expression());
        String dataTypeA = pair.b;
        if(value instanceof Integer && !Objects.equals(dataTypeA, "int") ||
                value instanceof Double && !Objects.equals(dataTypeA, "real") ||
                value instanceof String && !Objects.equals(dataTypeA, "string") ||
                value instanceof Boolean && !Objects.equals(dataTypeA, "bool")) {
            return null;
        }
        if(!(value instanceof Integer) && !(value instanceof Double) && !(value instanceof String) && !(value instanceof Boolean)) {
            return null;
        }

        if(variable instanceof ArrayList) {
            switch (dataTypeA) {
                case "int" -> ((ArrayList<Integer>)variable).removeIf(n -> Objects.equals(n, (Integer)value));
                case "real" -> ((ArrayList<Double>)variable).removeIf(n -> Objects.equals(n, (Double)value));
                case "string" -> ((ArrayList<String>)variable).removeIf(n -> Objects.equals(n, (String)value));
                case "bool" -> ((ArrayList<Boolean>)variable).removeIf(n -> Objects.equals(n, (Boolean)value));
            }
        }
        return null;
    }

    @Override
    public Object visitClear(TarTarParser.ClearContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        Pair<Object, String > pair = this.currentScope.resolveVariable(varName);
        Object variable = pair.a;

        if(variable instanceof ArrayList) {
            ((ArrayList<?>)variable).clear();
        }
        return null;
    }

    @Override
    public Object visitContains(TarTarParser.ContainsContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        Pair<Object, String > pair = this.currentScope.resolveVariable(varName);
        Object variable = pair.a;
        Object value = visit(ctx.expression());
        String dataTypeA = pair.b;
        if(value instanceof Integer && !Objects.equals(dataTypeA, "int") ||
                value instanceof Double && !Objects.equals(dataTypeA, "real") ||
                value instanceof String && !Objects.equals(dataTypeA, "string") ||
                value instanceof Boolean && !Objects.equals(dataTypeA, "bool")) {
            return null;
        }
        if(!(value instanceof Integer) && !(value instanceof Double) && !(value instanceof String) && !(value instanceof Boolean)) {
            return null;
        }

        if(variable instanceof ArrayList) {
            switch (dataTypeA) {
                case "int" -> {
                    return ((ArrayList<Integer>)variable).contains((Integer) value);
                }
                case "real" -> {
                    return ((ArrayList<Double>)variable).contains((Double) value);
                }
                case "string" -> {
                    return ((ArrayList<String>)variable).contains((String) value);
                }
                case "bool" -> {
                    return ((ArrayList<Boolean>)variable).contains((Boolean) value);
                }
            }
        }
        return null;
    }

    @Override
    public Object visitGet(TarTarParser.GetContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        Pair<Object, String > pair = this.currentScope.resolveVariable(varName);
        Object variable = pair.a;
        Object value = visit(ctx.expression());
        if(value instanceof String && ((String) value).startsWith("\"") && ((String) value).endsWith("\"")) {
            value = ((String) value).substring(1, ((String) value).length() - 1);
        }
        if(variable instanceof ArrayList) {
            int index = (Integer)value;
            return ((ArrayList<?>)variable).get(index);
        }
        if(variable instanceof String) {
            int index = (Integer)value;
            return ((String)variable).charAt(index);
        }
        if(variable instanceof HashMap) {
            return ((HashMap<?, ?>)variable).get(value);
        }
        return null;
    }

    @Override
    public Object visitPut(TarTarParser.PutContext ctx) {
        String varName = ctx.IDENTIFIER().getText();
        Pair<Object, String > pair = this.currentScope.resolveVariable(varName);
        Object variable = pair.a;
        String dataType = pair.b;
        Object key = visit(ctx.expression(0));
        Object value = visit(ctx.expression(1));
        if(key instanceof String && ((String) key).startsWith("\"") && ((String) key).endsWith("\"")) {
            key = ((String) key).substring(1, ((String) key).length() - 1);
        }
        switch (dataType) {
            case "int,int" -> {
                if(key instanceof Integer && value instanceof Integer) {
                    ((HashMap<Integer, Integer>)variable).put((Integer) key, (Integer)value);
                }
            }
            case "int,real" -> {
                if(key instanceof Integer && value instanceof Double) {
                    ((HashMap<Integer, Double>)variable).put((Integer) key, (Double) value);
                }
            }
            case "int,string" -> {
                if(key instanceof Integer && value instanceof String) {
                    ((HashMap<Integer, String>)variable).put((Integer) key, (String)value);
                }
            }
            case "int,bool" -> {
                if(key instanceof Integer && value instanceof Boolean) {
                    ((HashMap<Integer, Boolean>)variable).put((Integer) key, (Boolean) value);
                }
            }
            case "real,int" -> {
                if(key instanceof Integer && value instanceof Integer) {
                    ((HashMap<Double, Integer>)variable).put((Double) key, (Integer)value);
                }
            }
            case "real,real" -> {
                if(key instanceof Integer && value instanceof Double) {
                    ((HashMap<Double, Double>)variable).put((Double) key, (Double) value);
                }
            }
            case "real,string" -> {
                if(key instanceof Double && value instanceof String) {
                    ((HashMap<Double, String>)variable).put((Double) key, (String)value);
                }
            }
            case "real,bool" -> {
                if(key instanceof Double && value instanceof Boolean) {
                    ((HashMap<Double, Boolean>)variable).put((Double) key, (Boolean) value);
                }
            }
            case "string,int" -> {
                if(key instanceof String && value instanceof Integer) {
                    ((HashMap<String, Integer>)variable).put((String) key, (Integer)value);
                }
            }
            case "string,real" -> {
                if(key instanceof String && value instanceof Double) {
                    ((HashMap<String, Double>)variable).put((String) key, (Double) value);
                }
            }
            case "string,string" -> {
                if(key instanceof String && value instanceof String) {
                    ((HashMap<String, String>)variable).put((String) key, (String)value);
                }
            }
            case "string,bool" -> {
                if(key instanceof String && value instanceof Boolean) {
                    ((HashMap<String, Boolean>)variable).put((String) key, (Boolean) value);
                }
            }
            case "bool,int" -> {
                if(key instanceof Boolean && value instanceof Integer) {
                    ((HashMap<Boolean, Integer>)variable).put((Boolean) key, (Integer)value);
                }
            }
            case "bool,real" -> {
                if(key instanceof Boolean && value instanceof Double) {
                    ((HashMap<Boolean, Double>)variable).put((Boolean) key, (Double) value);
                }
            }
            case "bool,string" -> {
                if(key instanceof Boolean && value instanceof String) {
                    ((HashMap<Boolean, String>)variable).put((Boolean) key, (String)value);
                }
            }
            case "bool,bool" -> {
                if(key instanceof Boolean && value instanceof Boolean) {
                    ((HashMap<Boolean, Boolean>)variable).put((Boolean) key, (Boolean) value);
                }
            }
        }
        return null;
    }
}
