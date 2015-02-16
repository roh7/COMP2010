//Lexer.lex
//16/02/2014 - Rohan Kopparapu, Sam Fallahi, David Lipowicz

import java_cup.runtime.*;
import java.io.IOException;

//import something

%%

//fix these sections
%class Lex

%line
%column
%final

%cupsym Analyser.Sym
%cup

%eofval{
    return sym(Sym.EOF);
%eofval}

%{
    /* Convenience constructor for CUP Symbol object. */
    private Symbol sym (int type) {
        return sym(type, yytext());
    }

    /* Convenience constructor for CUP Symbol object. */
    private Symbol sym (int type, Object value) {
        return new Symbol(type, (yyline+1), yycolumn, value);
    }

    private void error () {
        System.out.println("Error in line " + (yyline+1) + ": Unknown input '" + yytext() + "'");
    }
%}

WHITESPACE  = [ \t\r\n\f] // \f is the form feed control character.
LETTER      = [a-zA-Z]
DIGIT       = [0-9]
PUNCTUATION = [\.,-\/#!$%\^&\*;:{}=\-_`~()]
NUM_NO_ZERO = [1-9][0-9]* // Cannot start with 0.

/* Literals macros. */
IDENTIFIER  = {LETTER}({LETTER}|{DIGIT})*
CHAR_LIT    = "'"({LETTER}|{DIGIT}|{PUNCTUATION})"'"
BOOL_LIT    = ("true"|"false")
INT_LIT     = 0|-?{NUM_NO_ZERO}
FLOAT_LIT   = -?{DIGIT}+"."{DIGIT}*
STR_LIT     = "\"" ~"\""

%%

//To be ignored
{WHITESPACE}        {} /* Ignore extra whitespace */
"#".*[\r|\n|\r\n]   {} /* Single-line comments */
"/#"~ "#/"          {} /* Multi-line comments */

//Types
"char"      { return sym(Sym.CHAR); }
"bool"      { return sym(Sym.BOOL); }
"int"       { return sym(Sym.INT); }
"rat"       { return sym(Sym.RAT); }
"float"     { return sym(Sym.FLOAT); }
"dict"      { return sym(Sym.DICT); }
"string"    { return sym(Sym.STR); }
"list"      { return sym(Sym.LIST); }
"void"      { return sym(Sym.VOID); }

//Aggregate operators
"in"        { return sym(Sym.IN); }
"len"       { return sym(Sym.LEN); }
"::"        { return sym(Sym.CONCAT); }

//Definitions/Declarations
"tdef"      { return sym(Sym.TDEF); }
"fdef"      { return sym(Sym.FDEF); }

//Input operator
"read"      { return sym(Sym.READ); }

//Output operator
"print"     { return sym(Sym.PRINT); }

//Control flow operators
"if"        { return sym(Sym.IF); }
"else"      { return sym(Sym.ELSE); }
"while"     { return sym(Sym.WHILE); }
"do"        { return sym(Sym.DO); }
"forall"    { return sym(Sym.FORALL); }
"return"    { return sym(Sym.RET); }

//Logical operators
"!"         { return sym(Sym.NOT); }
"&&"        { return sym(Sym.AND); }

//Arithmetic operators
"+"         { return sym(Sym.PLUS); }
"-"         { return sym(Sym.MINUS); }
"*"         { return sym(Sym.TIMES); }
"/"         { return sym(Sym.DIV); }
"^"         { return sym(Sym.POW); }
