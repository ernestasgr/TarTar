grammar TarTar;

program
 : line* main+ EOF
 ;

main
 : 'def void Main()' functionBody
 ;

line
 : functionDeclaration
 | statement
 ;

statement
 : variableDeclaration
 | variableDeclarationWithoutSemicolon
 | variableDeclarationWithSemicolon
 | immutableVariableDeclaration
 | arrayDeclaration
 | dictionaryDeclaration
 | assignment
 | functionCall
 | systemFunctionCall
 | ifElseStatement
 | forAssignmentStatement
 | forVariableDeclarationStatement
 | whileStatement
 | foreachStatement
 | foreachStatementForHashMap
 | returnStatement
 | patternMatchStatement
 | add
 | addRange
 | remove
 | removeAt
 | removeAll
 | clear
 | put
 ;

functionDeclaration
 : 'def' dataType IDENTIFIER '(' paramList? ')' functionBody
 ;

paramList : dataType IDENTIFIER (',' dataType IDENTIFIER)* ;

functionBody : '{' statement* '}' ;

variableDeclaration
 : 'var' dataType IDENTIFIER '=' expression ';'
 ;

variableDeclarationWithoutSemicolon
 : 'var' dataType IDENTIFIER
 ;

variableDeclarationWithSemicolon
 : 'var' dataType IDENTIFIER ';'
 ;

immutableVariableDeclaration
 : 'val' dataType IDENTIFIER '=' expression ';'
 ;

arrayDeclaration
 : 'var' 'List' '<' dataType '>' IDENTIFIER ';'
 ;

dictionaryDeclaration
 : 'var' 'Dictionary' '<' dataType ',' dataType '>' IDENTIFIER ';'
 ;

size
 : IDENTIFIER '.' SIZE '(' ')'
 ;

add
 : IDENTIFIER '.' ADD '(' expression ')' ';'
 ;

addRange
 : IDENTIFIER '.' ADDRANGE '(' expression ')' ';'
 ;

remove
 : IDENTIFIER '.' REMOVE '(' expression ')' ';'
 ;

removeAt
 : IDENTIFIER '.' REMOVEAT '(' expression ')' ';'
 ;

removeAll
 : IDENTIFIER '.' REMOVEALL '(' expression ')' ';'
 ;

clear
 : IDENTIFIER '.' CLEAR '(' ')' ';'
 ;

contains
 : IDENTIFIER '.' CONTAINS '(' expression ')'
 ;

get
 : IDENTIFIER '[' expression ']'
 ;

put
 : IDENTIFIER '.' PUT '(' expression ',' expression ')' ';'
 ;

assignment
 : IDENTIFIER assignmentOp expression ';'
 ;

functionCall
 : IDENTIFIER '(' expressionList? ')'
 ;

functionReturnCall
 : IDENTIFIER '(' expressionList? ')'
 ;

systemFunctionCall
 : PRINT '(' expression ')' ';'                                                    #printFunctionCall
 | FILEPRINT '(' expression ',' expression ')' ';'                                 #filePrintFunctionCall
 ;

ifElseStatement : 'if' '(' expression ')' block ('else' block)? ;

forAssignmentStatement: 'for' '(' assignment expression compareOp expression ';' assignment ')' block;

forVariableDeclarationStatement: 'for' '(' variableDeclaration expression compareOp expression ';' assignment ')' block;

whileStatement: 'while' '(' expression ')' block;

foreachStatement: 'foreach' '(' variableDeclarationWithoutSemicolon 'in' IDENTIFIER ')' block;

foreachStatementForHashMap: 'foreach' '(' variableDeclarationWithSemicolon variableDeclarationWithoutSemicolon 'in' IDENTIFIER ')' block;

returnStatement: 'return' expression? ';' ;

patternMatchStatement: 'match' '(' expressionList? ')' patternMatchBlock;

patternMatchBlock: '{' patternMatchCase* patternMatchDefault '}';

patternMatchCase: 'case' '(' expressionList? ')' block ;

patternMatchDefault: 'default' block ;

block : '{' statement* '}' ;

constant: INT | REAL | BOOL | STRING ;
dataType: INT_TYPE | REAL_TYPE | BOOL_TYPE | STRING_TYPE;

expressionList
 : expression (',' expression)*
 ;

expression
 : constant                                                 #constantExpression
 | IDENTIFIER                                               #identifierExpression
 | '(' expression ')'                                       #parenthesesExpression
 | booleanUnaryOp expression                                #booleanUnaryOpExpression
 | expression booleanBinaryOp expression                    #booleanBinaryOpExpression
 | expression numericMultiOp expression                     #numericMultiOpExpression
 | expression addOp expression                              #addOpExpression
 | expression numericMinusOp expression                     #numericMinusOpExpression
 | expression compareOp expression                          #compareOpExpression
 | functionCall                                             #functionCallExpression
 | stringify                                                #stringifyExpression
 | FILEREAD '(' expression ')'                              #fileReadFunctionCall
 | READ '(' ')'                                             #readFunctionCall
 | size                                                     #containerSizeCall
 | contains                                                 #containerContainsCall
 | get                                                      #containerGetCall
 ;

stringify
 : STRINGIFY '(' expression ')'                             #identifierStringify
 ;

PRINT : 'Print';
READ: 'Read';
STRINGIFY: 'ToString';
FILEREAD: 'FileRead';
FILEPRINT: 'FilePrint';

SIZE: 'Size';
ADD: 'Add';
ADDRANGE: 'AddRange';
REMOVE: 'Remove';
REMOVEAT: 'RemoveAt';
REMOVEALL: 'RemoveAll';
CLEAR: 'Clear';
CONTAINS: 'Contains';
PUT: 'Put';

INT : [-]?[0-9]+ ;
REAL : [-]?[0-9]+ '.' [0-9]+ ;
BOOL : 'True' | 'False' ;
STRING : ["] ( ~["\r\n\\] | '\\' ~[\r\n] )* ["] ;

INT_TYPE: 'int';
REAL_TYPE: 'real';
BOOL_TYPE: 'bool';
STRING_TYPE: 'string';

IDENTIFIER : [a-zA-Z_][a-zA-Z_0-9]* ;

COMMENT : ( '//' ~[\r\n]* | '/*' .*? '*/' ) -> skip ;

WS : [ \t\f\r\n]+ -> skip ;

booleanUnaryOp : 'not' ;

booleanBinaryOp : 'or' | 'and' ;

numericMultiOp : '*' | '/' | '%' ;

addOp : '+';

numericMinusOp: '-';

compareOp: '<' | '<=' | '>' | '>=' | '==' | '!=';

assignmentOp: '=' | '+=' | '-=' | '*=' | '/=' | '%=';