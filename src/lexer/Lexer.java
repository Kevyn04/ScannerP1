package lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// the actual scanner for part 2 of the assignment
// goes char by char through the source and builds a list of tokens
// (keywords, identifiers, numbers, operators, punctuation)
// spaces, tabs, newlines and comments get skipped, they don't become tokens
// anything we don't recognize gets put in the errors list instead of becoming a token
//
// note: negative numbers like -5 come out as ONE NUM token so the CFG doesn't have
// to deal with negation. but "i - 1" still has to be a subtraction, see startsNegativeNumber()
/**
 * @author Kevyn Victor Salonga
 * @author Ryan Stencavage
 * @author Domenic Doyle
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
    private final List<String> errors = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;
    private int col = 1;

    // line/col where the current token starts
    private int tokenLine = 1;
    private int tokenCol = 1;

    // how many [ ] we're inside of, matrices treat whitespace as an element separator
    private int bracketDepth = 0;

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

    // stuff the scanner couldn't recognize, call this after scanTokens()
    public List<String> getErrors() {
        return errors;
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
            case '[': bracketDepth++; addToken(TokenType.LBRACKET); break;
            case ']': if (bracketDepth > 0) bracketDepth--; addToken(TokenType.RBRACKET); break;
            case ',': addToken(TokenType.COMMA); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '+': addToken(TokenType.PLUS); break;
            case '-':
                if (isDigit(peek()) && startsNegativeNumber()) number();
                else addToken(TokenType.MINUS);
                break;
            case '*': addToken(TokenType.STAR); break;

            case '=': addToken(match('=') ? TokenType.EQ : TokenType.ASSIGN); break;
            case '<': addToken(match('=') ? TokenType.LE : TokenType.LT); break;
            case '>': addToken(match('=') ? TokenType.GE : TokenType.GT); break;
            case '!':
                if (match('=')) addToken(TokenType.NEQ);
                else error(c); // spec doesnt have a lone !
                break;
            case '.':
                if (match('.')) addToken(TokenType.RANGE);
                else error(c);
                break;

            default:
                // uh oh, not an operator/punctuation char, so its either the start
                // of a number, the start of a word, or just garbage we dont recognize
                if (isWhitespace(c)) {
                    // weird whitespace that isn't a normal space/tab, like the
                    // non-breaking spaces you get from copy-pasting out of a PDF or website
                } else if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    error(c);
                }
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        addToken(KEYWORDS.getOrDefault(text, TokenType.ID));
    }

    // also used for negative numbers, in that case the '-' was already consumed
    // and ends up in the lexeme since it's between start and current
    private void number() {
        while (isDigit(peek())) advance();

        // careful here: only grab the '.' if it's followed by a digit and NOT
        // another dot, otherwise "1..10" from the for loop breaks (that ".."
        // needs to stay separate so it can become a RANGE token)
        if (peek() == '.' && peekNext() != '.' && isDigit(peekNext())) {
            advance(); // consume '.'
            while (isDigit(peek())) advance();
        }
        addToken(TokenType.NUM);
    }

    // decides if a '-' right before a digit is a negative number or a subtraction.
    // after a value (x, 5, ), ], true/false) it's normally subtraction, like "i -1" or "i-1".
    // exception is inside a matrix: "[0 -1 0]" means 3 elements, so a space before
    // the '-' (and none after it) makes it a negative number there
    private boolean startsNegativeNumber() {
        if (tokens.isEmpty()) return true;
        switch (tokens.get(tokens.size() - 1).type) {
            case ID: case NUM: case TRUE: case FALSE: case RPAREN: case RBRACKET:
                boolean spaceBefore = start > 0 && isWhitespace(source.charAt(start - 1));
                return bracketDepth > 0 && spaceBefore;
            default:
                return true; // after =, (, [, comma, an operator etc. it can only be negation
        }
    }

    private void error(char c) {
        // the U+ code is there so invisible characters actually show up as something
        errors.add(String.format("line %d, col %d: unexpected character '%c' (U+%04X)", tokenLine, tokenCol, c, (int) c));
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

    private boolean isWhitespace(char c) {
        // isSpaceChar catches the unicode ones like non-breaking space (U+00A0)
        // that Character.isWhitespace on its own doesn't count
        return Character.isWhitespace(c) || Character.isSpaceChar(c);
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
