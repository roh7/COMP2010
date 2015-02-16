//Lexer.lex
//16/02/2014 - Rohan Kopparapu, Sam Fallahi, David Lipowicz

import java_cup.runtime.*;
import java.io.IOException;


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

//MISC
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

//Definitions/Declarations
"tdef"      { return sym(Sym.TDEF); }
"fdef"       { return sym(Sym.FDEF); }
