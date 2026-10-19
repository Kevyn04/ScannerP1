package lexer;

// every kind of token the scanner can spit out
/**
 * @author Kevyn Victor Salonga
 * @author Ryan Stencavage
 * @author Domenic Doyle
 */
public enum TokenType {
    // data types
    MAT, INT, FLOAT, BOOL, CMPLX,

    // control structures
    IF, FOR, WHILE,

    // booleans
    TRUE, FALSE,

    // built-in functions
    DET, SQRT, TRANS, INV, LEN, RAND,

    // identifiers/literals
    ID, NUM,

    // operators
    ASSIGN,   // =
    PLUS,     // +
    MINUS,    // -
    STAR,     // *
    SLASH,    // /
    LT, LE,   // <  <=
    GT, GE,   // >  >=
    EQ, NEQ,  // == !=
    RANGE,    // ..

    // punctuation
    LPAREN, RPAREN,     // ( )
    LBRACE, RBRACE,     // { }
    LBRACKET, RBRACKET, // [ ]
    COMMA, SEMICOLON,   // , ;

    // everything else
    EOF, UNKNOWN
}
