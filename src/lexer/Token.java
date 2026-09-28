package lexer;

// holds one token - what kind it is, the actual text, and where it was in the source
/**
 * @author Kevyn Victor Salonga
 * @author Ryan Stencavage
 * @author Domenic doyle
 */
public class Token {
    public final TokenType type;
    public final String lexeme;
    public final int line;
    public final int column;

    public Token(TokenType type, String lexeme, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.column = column;
    }

    // so println(token) actually prints something readable instead of like
    // "lexer.Token@1b6d3586" or whatever
    @Override
    public String toString() {
        return String.format("%-6d %-6d %-14s %s", line, column, type, lexeme);
    }
}
