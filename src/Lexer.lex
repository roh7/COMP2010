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
IDENTIFIER  = {LETTER}({LETTER}|{DIGIT}|"_")*
CHAR_LIT    = "'"({LETTER}|{DIGIT}|{PUNCTUATION})"'"
BOOL_LIT    = ("T"|"F")
INT_LIT     = 0|-?{NUM_NO_ZERO}
FLOAT_LIT   = -?{DIGIT}+"."{DIGIT}+
RAT_LIT     = (INT_LIT"_")?INT_LIT"/"INT_LIT
STR_LIT     = "\"" ~"\""

%%

//=====================================================================================================================

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
"top"       { return sym(Sym.TOP); }

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
"||"        { return sym(Sym.OR); }
"=>"        { return sym(Sym.IMPLY); }

//Relational operators
"<"         { return sym(Sym.LTHAN); }
"<="        { return sym(Sym.LTHANEQ); }
"=="        { return sym(Sym.EQUALS); }
"!="        { return sym(Sym.NEQUALS); }
//">"         { return sym(Sym.MTHAN); } //not sure about this
//">="        { return sym(Sym.MTHANEQ); } //not sure about this

//Arithmetic operators
"+"         { return sym(Sym.PLUS); }
"-"         { return sym(Sym.MINUS); }
"*"         { return sym(Sym.TIMES); }
"/"         { return sym(Sym.DIV); }
"^"         { return sym(Sym.POW); }

//Expression operators
//"."			{ return sym(Sym.DOT); } //not sure about this
":=" 		{ return sym(Sym.ASSIGN); }

//Other
"<"			{ return sym(Sym.LANGBR); }
">"			{ return sym(Sym.RANGBR); }
"("         { return sym(Sym.LPAREN); }
")"         { return sym(Sym.RPAREN); }
"{"         { return sym(Sym.LBRACE); }
"}"         { return sym(Sym.RBRACE); }
"[|"        { return sym(Sym.LDICT); }
"|]"        { return sym(Sym.RDICT); }
"["         { return sym(Sym.LBRACK); }
"]"         { return sym(Sym.RBRACK); }
","         { return sym(Sym.COMMA); }
":"         { return sym(Sym.COL); }
";"         { return sym(Sym.SEMICOL); }
"main"      { return sym(Sym.MAIN); }

//Loop terminators
"fi"        { return syn(Sym.ENDIF); }
"od"        { return syn(Sym.ENDDO); }

//Literals
{CHAR_LIT}  { return sym(Sym.CHAR_LIT); }
{BOOL_LIT}  { return sym(Sym.BOOL_LIT); }
{FLOAT_LIT} { return sym(Sym.FLOAT_LIT); }
{INT_LIT}   { return sym(Sym.INT_LIT); }
{STR_LIT}   { return sym(Sym.STR_LIT); }
{IDENTIFIER}{ return sym(Sym.ID); }
.           { /*System.out.println ("<ERROR>");*/ error(); } // For any other symbols, print error.