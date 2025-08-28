import java.util.List;
import java.util.Stack;

class JsonParser {
    static void parse(String input) {
        List<Token> tokens = Lexer.tokenize(input);

        Stack<Token> stack = new Stack<>();
        System.out.println("Parsing: " + input);
        System.out.println("Tokens: " + tokens + "\n");

        int pos = 0;

        while (pos < tokens.size() || canReduce(stack)) {

            if (canReduce(stack)) {
                reduce(stack);
                printStep("REDUCE", stack);
            }
            else if (pos < tokens.size()) {
                Token next = tokens.get(pos);

                System.out.println("----"+next.toString());
                stack.push(next);
                pos++;
                printStep("SHIFT " + next, stack);
            }
            else {
                break;
            }
        }

        if (stack.size() == 1 && stack.peek().type == TokenType.VALUE) {
            System.out.println("✅ SUCCESS!\n");
        } else {
            System.out.println("❌ FAILED - stack: " + stack + "\n");
        }
    }

    static boolean canReduce(Stack<Token> stack) {
        if (stack.isEmpty()) return false;

        TokenType top = stack.peek().type;

        if (top == TokenType.STRING || top == TokenType.NUMBER ||
                top == TokenType.TRUE || top == TokenType.FALSE || top == TokenType.NULL) {
            return true;
        }

        if (top == TokenType.OBJECT || top == TokenType.ARRAY) {
            return true;
        }

        if (stack.size() >= 2 &&
                stack.get(stack.size()-2).type == TokenType.LBRACE &&
                stack.get(stack.size()-1).type == TokenType.RBRACE) {
            return true;
        }

        if (stack.size() >= 2 &&
                stack.get(stack.size()-2).type == TokenType.LBRACKET &&
                stack.get(stack.size()-1).type == TokenType.RBRACKET) {
            return true;
        }

        if (stack.size() >= 3 &&
                stack.get(stack.size()-3).type == TokenType.STRING &&
                stack.get(stack.size()-2).type == TokenType.COLON &&
                stack.get(stack.size()-1).type == TokenType.VALUE) {
            return true;
        }

        if (stack.size() >= 3 &&
                stack.get(stack.size()-3).type == TokenType.VALUE &&
                stack.get(stack.size()-2).type == TokenType.COLON &&
                stack.get(stack.size()-1).type == TokenType.VALUE) {
            return true;
        }

        if (stack.size() >= 3 &&
                stack.get(stack.size()-3).type == TokenType.LBRACE &&
                stack.get(stack.size()-2).type == TokenType.PAIRS &&
                stack.get(stack.size()-1).type == TokenType.RBRACE) {
            return true;
        }

        if (stack.size() >= 3 &&
                stack.get(stack.size()-3).type == TokenType.LBRACKET &&
                stack.get(stack.size()-2).type == TokenType.VALUES &&
                stack.get(stack.size()-1).type == TokenType.RBRACKET) {
            return true;
        }

        if (top == TokenType.PAIR) {
            return true;
        }

        if (stack.size() >= 3 &&
                stack.get(stack.size()-3).type == TokenType.PAIRS &&
                stack.get(stack.size()-2).type == TokenType.COMMA &&
                stack.get(stack.size()-1).type == TokenType.PAIR) {
            return true;
        }

        // NEW RULE: Handle PAIRS, COMMA, PAIRS
        if (stack.size() >= 3 &&
                stack.get(stack.size()-3).type == TokenType.PAIRS &&
                stack.get(stack.size()-2).type == TokenType.COMMA &&
                stack.get(stack.size()-1).type == TokenType.PAIRS) {
            return true;
        }

        if (stack.size() >= 3 &&
                stack.get(stack.size()-3).type == TokenType.VALUES &&
                stack.get(stack.size()-2).type == TokenType.COMMA &&
                stack.get(stack.size()-1).type == TokenType.VALUE) {
            return true;
        }

        if (top == TokenType.VALUE) {
            if (stack.size() >= 2 && stack.get(stack.size()-2).type == TokenType.LBRACKET) {
                return true;
            }
        }

        return false;
    }

    static void reduce(Stack<Token> stack) {
        TokenType top = stack.peek().type;

        if (top == TokenType.STRING || top == TokenType.NUMBER ||
                top == TokenType.TRUE || top == TokenType.FALSE || top == TokenType.NULL) {
            Token token = stack.pop();
            stack.push(new Token(TokenType.VALUE, token.value));
            return;
        }

        if (top == TokenType.OBJECT || top == TokenType.ARRAY) {
            Token token = stack.pop();
            stack.push(new Token(TokenType.VALUE, token.value));
            return;
        }

        if (stack.size() >= 2 &&
                stack.get(stack.size()-2).type == TokenType.LBRACE &&
                stack.get(stack.size()-1).type == TokenType.RBRACE) {
            stack.pop();
            stack.pop();
            stack.push(new Token(TokenType.OBJECT, "{}"));
            return;
        }

        if (stack.size() >= 2 &&
                stack.get(stack.size()-2).type == TokenType.LBRACKET &&
                stack.get(stack.size()-1).type == TokenType.RBRACKET) {
            stack.pop();
            stack.pop();
            stack.push(new Token(TokenType.ARRAY, "[]"));
            return;
        }

        if (stack.size() >= 3 &&
                stack.get(stack.size()-3).type == TokenType.STRING &&
                stack.get(stack.size()-2).type == TokenType.COLON &&
                stack.get(stack.size()-1).type == TokenType.VALUE) {
            Token val = stack.pop();
            stack.pop();
            Token key = stack.pop();
            stack.push(new Token(TokenType.PAIR, key.value + ":" + val.value));
            return;
        }

        if (stack.size() >= 3 &&
                stack.get(stack.size()-3).type == TokenType.VALUE &&
                stack.get(stack.size()-2).type == TokenType.COLON &&
                stack.get(stack.size()-1).type == TokenType.VALUE) {
            Token val = stack.pop();
            stack.pop();
            Token key = stack.pop();
            stack.push(new Token(TokenType.PAIR, key.value + ":" + val.value));
            return;
        }

        if (top == TokenType.PAIR) {
            Token pair = stack.pop();
            stack.push(new Token(TokenType.PAIRS, pair.value));
            return;
        }

        if (stack.size() >= 3 &&
                stack.get(stack.size()-3).type == TokenType.PAIRS &&
                stack.get(stack.size()-2).type == TokenType.COMMA &&
                stack.get(stack.size()-1).type == TokenType.PAIR) {
            Token pair = stack.pop();
            stack.pop();
            Token pairs = stack.pop();
            stack.push(new Token(TokenType.PAIRS, pairs.value + "," + pair.value));
            return;
        }

        // Handle PAIRS, COMMA, PAIRS reduction
        if (stack.size() >= 3 &&
                stack.get(stack.size()-3).type == TokenType.PAIRS &&
                stack.get(stack.size()-2).type == TokenType.COMMA &&
                stack.get(stack.size()-1).type == TokenType.PAIRS) {
            Token rightPairs = stack.pop();
            stack.pop();
            Token leftPairs = stack.pop();
            stack.push(new Token(TokenType.PAIRS, leftPairs.value + "," + rightPairs.value));
            return;
        }

        if (stack.size() >= 3 &&
                stack.get(stack.size()-3).type == TokenType.LBRACE &&
                stack.get(stack.size()-2).type == TokenType.PAIRS &&
                stack.get(stack.size()-1).type == TokenType.RBRACE) {
            stack.pop();
            Token pairs = stack.pop();
            stack.pop();
            stack.push(new Token(TokenType.OBJECT, "{" + pairs.value + "}"));
            return;
        }

        if (top == TokenType.VALUE) {
            if (stack.size() >= 2 && stack.get(stack.size()-2).type == TokenType.LBRACKET) {
                Token val = stack.pop();
                stack.push(new Token(TokenType.VALUES, val.value));
                return;
            }
        }

        if (stack.size() >= 3 &&
                stack.get(stack.size()-3).type == TokenType.VALUES &&
                stack.get(stack.size()-2).type == TokenType.COMMA &&
                stack.get(stack.size()-1).type == TokenType.VALUE) {
            Token val = stack.pop();
            stack.pop();
            Token vals = stack.pop();
            stack.push(new Token(TokenType.VALUES, vals.value + "," + val.value));
            return;
        }

        if (stack.size() >= 3 &&
                stack.get(stack.size()-3).type == TokenType.LBRACKET &&
                stack.get(stack.size()-2).type == TokenType.VALUES &&
                stack.get(stack.size()-1).type == TokenType.RBRACKET) {
            stack.pop();
            Token vals = stack.pop();
            stack.pop();
            stack.push(new Token(TokenType.ARRAY, "[" + vals.value + "]"));
            return;
        }
    }

    static void printStep(String action, Stack<Token> stack) {
        System.out.println(action + " → " + stack);
    }
}