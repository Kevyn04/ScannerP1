package lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// the actual scanner for part 2 of the assignment
// goes char by char through the source and builds a list of tokens
// (keywords, identifiers, numbers, operators, punctuation)
// comments get skipped, they don't become tokens
//
// note: -5 gets tokenized as MINUS then 5, not as one negative number token.
// figuring out if a minus is subtraction or negation is a parser's job, not the scanner's
/**
 * @author Kevyn Victor Salonga
 * @author Ryan Stencavage
 * @author Domenic doyle
 */
public class Lexer {

    // put all the keywords in a map so identifier() can just look them up
    // instead of a huge if/else chain, way easier to add new ones later too
    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

    static {
        KEYWORDS.put("mat", TokenType.MAT);
        KEYWORDS.put("int", TokenType.INT);
        KEYWORDS.put("float", TokenType.FLOAT);
        KEYWORDS.put("bool", TokenType.BOOL);
        KEYWORDS.put("cmplx", TokenType.CMPLX);

        KEYWORDS.put("if", TokenType.IF);
        KEYWORDS.put("for", TokenType.FOR);
        KEYWORDS.put("while", TokenType.WHILE);

        KEYWORDS.put("true", TokenType.TRUE);
        KEYWORDS.put("false", TokenType.FALSE);

        KEYWORDS.put("det", TokenType.DET);
        KEYWORDS.put("sqrt", TokenType.SQRT);
        KEYWORDS.put("trans", TokenType.TRANS);
        KEYWORDS.put("inv", TokenType.INV);
        KEYWORDS.put("len", TokenType.LEN);
        KEYWORDS.put("rand", TokenType.RAND);
    }

    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int col = 1;

    // line/col where the current token starts
    private int tokenLine = 1;
    private int tokenCol = 1;

    public Lexer(String source) {
        this.source = source;
    }

    // scans everything and returns the token list, with EOF tacked on the end
    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            tokenLine = line;
            tokenCol = col;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", line, col));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case ' ':
            case '\t':
            case '\r':
            case '\n':
                break; // whitespace, no token

            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;

            case '(': addToken(TokenType.LPAREN); break;
            case ')': addToken(TokenType.RPAREN); break;
            case '{': addToken(TokenType.LBRACE); break;
            case '}': addToken(TokenType.RBRACE); break;
            case '[': addToken(TokenType.LBRACKET); break;
            case ']': addToken(TokenType.RBRACKET); break;
            case ',': addToken(TokenType.COMMA); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '+': addToken(TokenType.PLUS); break;
            case '-': addToken(TokenType.MINUS); break;
            case '*': addToken(TokenType.STAR); break;

            case '=': addToken(match('=') ? TokenType.EQ : TokenType.ASSIGN); break;
            case '<': addToken(match('=') ? TokenType.LE : TokenType.LT); break;
            case '>': addToken(match('=') ? TokenType.GE : TokenType.GT); break;
            case '!':
                if (match('=')) addToken(TokenType.NEQ);
                else addToken(TokenType.UNKNOWN); // spec doesnt have a lone ! but just in case
                break;
            case '.':
                if (match('.')) addToken(TokenType.RANGE);
                else addToken(TokenType.UNKNOWN);
                break;

            default:
                // uh oh, not an operator/punctuation char, so its either the start
                // of a number, the start of a word, or just garbage we dont recognize
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    addToken(TokenType.UNKNOWN);
                }
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        addToken(KEYWORDS.getOrDefault(text, TokenType.IDENTIFIER));
    }

    private void number() {
        while (isDigit(peek())) advance();

        boolean isFloat = false;
        // careful here: only grab the '.' if it's followed by a digit and NOT
        // another dot, otherwise "1..10" from the for loop breaks (that ".."
        // needs to stay separate so it can become a RANGE token)
        if (peek() == '.' && peekNext() != '.' && isDigit(peekNext())) {
            isFloat = true;
            advance(); // consume '.'
            while (isDigit(peek())) advance();
        }
        addToken(isFloat ? TokenType.FLOAT_LITERAL : TokenType.INT_LITERAL);
    }

    private void addToken(TokenType type) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, tokenLine, tokenCol));
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        char c = source.charAt(current++);
        if (c == '\n') {
            line++;
            col = 1;
        } else {
            col++;
        }
        return c;
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++;
        col++;
        return true;
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    // like peek() but one further ahead, needed this for telling ".5" apart
    // from "1..10" without accidentally eating into the range dots
    private char peekNext() {
        return (current + 1 >= source.length()) ? '\0' : source.charAt(current + 1);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
