package lexer;

// every kind of token the scanner can spit out
/**
 * @author Kevyn Victor Salonga
 * @author Ryan Stencavage
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
    IDENTIFIER, INT_LITERAL, FLOAT_LITERAL,

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
