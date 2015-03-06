//Lexer.lex
//02/03/2015 - Rohan Kopparapu, Sam Fallahi, David Lipowicz

import java_cup.runtime.*;

%%

%class Lexer
%unicode
%cup
%line
%column

%eofval{
    return sym(sym.EOF);
%eofval}

%{
  StringBuffer string = new StringBuffer();
  private Symbol sym(int type) { 
    return new Symbol(type, yyline, yycolumn);
  } 
  private Symbol sym(int type, Object value) {
    return new Symbol(type, yyline, yycolumn, value);
  }
%}  

//Declared but never used 

WHITESPACE  = [ \t\r\n\f]
LETTER      = [a-zA-Z]
DIGIT       = [0-9]
PUNCTUATION = [\.,-\/#!$%\^&\*;:{}=\-_`~()]
NUM_NO_ZERO = [1-9][0-9]* // Cannot start with 0.

//Literal cfgs
IDENTIFIER  = {LETTER}({LETTER}|{DIGIT}|"_")*
CHAR_LIT    = "'"({LETTER}|{DIGIT}|{PUNCTUATION})"'"
BOOL_LIT    = ("T"|"F")
INT_LIT     = 0|{NUM_NO_ZERO}
FLOAT_LIT   = {DIGIT}+"."{DIGIT}+
RAT_LIT     = (INT_LIT"_")?INT_LIT"/"INT_LIT|INT_LIT
STR_LIT     = "\"" ~"\""

%state STRING 
%%

<YYINITIAL> {
//To be ignored
{WHITESPACE}        {} /* Ignore extra whitespace */
"#".*[\r|\n|\r\n]   {} /* Single-line comments */
"/#"~ "#/"          {} /* Multi-line comments */

//Primitives
"char"      { return sym(sym.CHAR); }
"bool"      { return sym(sym.BOOL); }
"int"       { return sym(sym.INT); }
"rat"       { return sym(sym.RAT); }
"float"     { return sym(sym.FLOAT); }
"top"       { return sym(sym.TOP); }

//Aggregates
"dict"      { return sym(sym.DICT); }
"seq"       { return sym(sym.SEQ); }

//Aggregate operators
"in"        { return sym(sym.IN); }
"len"       { return sym(sym.LEN); }
"::"        { return sym(sym.CONCAT); }

//Definitions/Declarations
"tdef"      { return sym(sym.TDEF); }
"fdef"      { return sym(sym.FDEF); }

//Input operator
"read"      { return sym(sym.READ); }

//Output operator
"print"     { return sym(sym.PRINT); }

//Control flow operators
"if"        { return sym(sym.IF); }
"then"      { return sym(sym.THEN); }
"else"      { return sym(sym.ELSE); }
"while"     { return sym(sym.WHILE); }
"do"        { return sym(sym.DO); }
"forall"    { return sym(sym.FORALL); }
"return"    { return sym(sym.RET); }

//Logical operators
"!"         { return sym(sym.NOT); }
"&&"        { return sym(sym.AND); }
"||"        { return sym(sym.OR); }
"=>"        { return sym(sym.IMPLY); }

//Relational operators
"<"         { return sym(sym.LTHAN); }
"<="        { return sym(sym.LTHANEQ); }
"="         { return sym(sym.EQUALS); }
"!="        { return sym(sym.NEQUALS); }

//Arithmetic operators
"+"         { return sym(sym.PLUS); }
"-"         { return sym(sym.MINUS); }
"*"         { return sym(sym.TIMES); }
"/"         { return sym(sym.DIV); }
"^"         { return sym(sym.POW); }

//Expression operators
"."         { return sym(sym.DOT); }
":="        { return sym(sym.ASSIGN); }

//Other
">"         { return sym(sym.RANGBR); }
"("         { return sym(sym.LPAREN); }
")"         { return sym(sym.RPAREN); }
"{"         { return sym(sym.LBRACE); }
"}"         { return sym(sym.RBRACE); }
"["         { return sym(sym.LBRACK); }
"]"         { return sym(sym.RBRACK); }
","         { return sym(sym.COMMA); }
":"         { return sym(sym.COL); }
";"         { return sym(sym.SEMICOL); }
"main"      { return sym(sym.MAIN); }
"alias"     { return sym(sym.ALIAS); }

//Loop terminators
"fi"        { return sym(sym.ENDIF); }
"od"        { return sym(sym.ENDDO); }

//Literals
{CHAR_LIT}  { return sym(sym.CHAR_LIT); }
{BOOL_LIT}  { return sym(sym.BOOL_LIT); }
{FLOAT_LIT} { return sym(sym.FLOAT_LIT); }
{INT_LIT}   { return sym(sym.INT_LIT); }
{STR_LIT}   { return sym(sym.STR_LIT); }
{IDENTIFIER} { return sym(sym.ID); }
{RAT_LIT}   { return sym(sym.RAT_LIT); }
}

[^]         { throw new Error("Line " + yyline+1 + ", Column " + yycolumn); }