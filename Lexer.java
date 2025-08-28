
import java.util.ArrayList;
import java.util.List;

class Lexer {
    static List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            if (c == '"') {
                StringBuilder str = new StringBuilder();
                i++;
                while (i < input.length() && input.charAt(i) != '"') {
                    str.append(input.charAt(i));
                    i++;
                }
                i++;
                tokens.add(new Token(TokenType.STRING, str.toString()));
            }

            else if (Character.isDigit(c)) {
                StringBuilder num = new StringBuilder();
                while (i < input.length() && Character.isDigit(input.charAt(i))) {
                    num.append(input.charAt(i));
//                    System.out.println(input.charAt(i));
                    i++;
                }
                tokens.add(new Token(TokenType.NUMBER, num.toString()));
            }

            else {
                switch (c) {
                    case '{': tokens.add(new Token(TokenType.LBRACE, "{")); break;
                    case '}': tokens.add(new Token(TokenType.RBRACE, "}")); break;
                    case '[': tokens.add(new Token(TokenType.LBRACKET, "[")); break;
                    case ']': tokens.add(new Token(TokenType.RBRACKET, "]")); break;
                    case ':': tokens.add(new Token(TokenType.COLON, ":")); break;
                    case ',': tokens.add(new Token(TokenType.COMMA, ",")); break;
                    case 't':
                        if (input.substring(i).startsWith("true")) {
                            tokens.add(new Token(TokenType.TRUE, "true"));
                            i += 3;
                        }
                        break;
                    case 'f':
                        if (input.substring(i).startsWith("false")) {
                            tokens.add(new Token(TokenType.FALSE, "false"));
                            i += 4;
                        }
                        break;
                    case 'n':
                        if (input.substring(i).startsWith("null")) {
                            tokens.add(new Token(TokenType.NULL, "null"));
                            i += 3;
                        }
                        break;
                }
                i++;
            }
        }
        return tokens;
    }
}