package lexer;

import java.util.List;

// just a quick way to test the lexer without the GUI - runs it on some
// sample MatrixLang code and prints out every token it finds
/**
 * @author Kevyn Victor Salonga
 * @author Ryan Stencavage
 */
public class LexerDemo {

    private static final String SAMPLE_PROGRAM = String.join("\n",
        "// sample MatrixLang program to test the scanner",
        "mat x = [0 -1 0,-1 2 -1 ,0 -1 0]",
        "mat y = rand(2,9)",
        "int i = 10",
        "float pi = 3.14",
        "bool flag = true",
        "cmplx num = (2,5) // represents the 2+5i complex number",
        "",
        "if (i > 0) {",
        "    i = i - 1",
        "}",
        "",
        "for (i=1..10) {",
        "    mat t = trans(x)",
        "}",
        "",
        "while (flag) {",
        "    int d = det(x)",
        "    flag = false",
        "}",
        "",
        "float r = sqrt(pi)",
        "mat inverse = inv(x)",
        "int length = len(x)"
    );

    public static void main(String[] args) {
        // print the source first so you can eyeball it next to the token list below
        System.out.println("=== MatrixLang source ===");
        System.out.println(SAMPLE_PROGRAM);
        System.out.println();

        List<Token> tokens = new Lexer(SAMPLE_PROGRAM).scanTokens();

        System.out.println("=== Tokens ===");
        System.out.printf("%-6s %-6s %-14s %s%n", "LINE", "COL", "TYPE", "LEXEME");
        for (Token token : tokens) {
            System.out.println(token);
        }
    }
}
