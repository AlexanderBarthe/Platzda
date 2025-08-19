package eva.platzda.cli;

import java.util.*;

public final class ExpressionEvaluator {

    private enum Assoc { LEFT, RIGHT }

    private static final class Op {
        final String symbol;
        final int prec;
        final Assoc assoc;
        final int arity;
        Op(String s, int p, Assoc a, int ar) { symbol = s; prec = p; assoc = a; arity = ar; }
    }

    // operator definitions: +, -, *, /, %, unary -
    private static final Map<String, Op> OPS = new HashMap<>();
    static {
        OPS.put("+", new Op("+", 1, Assoc.LEFT, 2));
        OPS.put("-", new Op("-", 1, Assoc.LEFT, 2));
        OPS.put("*", new Op("*", 2, Assoc.LEFT, 2));
        OPS.put("/", new Op("/", 2, Assoc.LEFT, 2)); // integer division
        OPS.put("%", new Op("%", 2, Assoc.LEFT, 2)); // modulo
        OPS.put("u-", new Op("u-", 3, Assoc.RIGHT, 1)); // unary minus
    }

    private static boolean isOperator(String token) {
        return OPS.containsKey(token);
    }

    /**
     * Evaluiert einen Ausdruck sofort (parst intern) mit gesetztem xValue.
     */
    public static long evaluate(String expression, long xValue) {
        List<String> rpn = parseToRPNInternal(expression, true, xValue);
        return evalRPN(rpn, null); // bereits ersetzt, daher kein xValue nötig
    }

    /**
     * Parst Ausdruck in RPN und liefert eine Liste von Tokens.
     * X (klein) wird als Platzhalter "X" in der RPN belassen (cache-freundlich).
     */
    public static List<String> parseToRPN(String expression) {
        return parseToRPNInternal(expression, false, 0L);
    }

    /**
     * Evaluiert eine zuvor geparste RPN-Liste und ersetzt dabei bei Bedarf "X" durch xValue.
     */
    public static long evalRPNWithX(List<String> rpn, long xValue) {
        return evalRPN(rpn, xValue);
    }

    // -------------------------
    // Core: Parsing (single implementation)
    // -------------------------
    // If substituteX == true -> occurrences of 'x' become the decimal literal of xValue.
    // If substituteX == false -> occurrences of 'x' become the placeholder token "X".
    private static List<String> parseToRPNInternal(String expr, boolean substituteX, long xValue) {
        List<String> output = new ArrayList<>();
        Deque<String> ops = new ArrayDeque<>();

        int len = expr.length();
        int i = 0;
        String prevTokenType = null; // "num" | "op" | "lparen" | null

        while (i < len) {
            char ch = expr.charAt(i);
            if (Character.isWhitespace(ch)) { i++; continue; }

            // number literal
            if (Character.isDigit(ch)) {
                int j = i;
                while (j < len && Character.isDigit(expr.charAt(j))) j++;
                output.add(expr.substring(i, j));
                prevTokenType = "num";
                i = j;
                continue;
            }

            // identifier: only 'x' is recognized
            if (Character.isLetter(ch)) {
                int j = i;
                while (j < len && Character.isLetter(expr.charAt(j))) j++;
                String id = expr.substring(i, j).toLowerCase(Locale.ROOT);
                if ("x".equals(id)) {
                    if (substituteX) output.add(Long.toString(xValue));
                    else output.add("X");
                } else {
                    throw new IllegalArgumentException("Unrecognized identifier: " + id + " in expression: " + expr);
                }
                prevTokenType = "num";
                i = j;
                continue;
            }

            // parentheses
            if (ch == '(') {
                ops.push("(");
                prevTokenType = "lparen";
                i++;
                continue;
            }
            if (ch == ')') {
                boolean found = false;
                while (!ops.isEmpty()) {
                    String top = ops.pop();
                    if ("(".equals(top)) { found = true; break; }
                    output.add(top);
                }
                if (!found) throw new IllegalArgumentException("Mismatched parentheses in expression: " + expr);
                prevTokenType = "num";
                i++;
                continue;
            }

            // operators: + - * / %
            if (ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '%') {
                String opSym = String.valueOf(ch);
                // detect unary minus
                if (ch == '-') {
                    if (prevTokenType == null || "op".equals(prevTokenType) || "lparen".equals(prevTokenType)) {
                        opSym = "u-";
                    }
                }
                pushOperator(output, ops, opSym);
                prevTokenType = "op";
                i++;
                continue;
            }

            throw new IllegalArgumentException("Unexpected character '" + ch + "' in expression: " + expr);
        }

        while (!ops.isEmpty()) {
            String top = ops.pop();
            if ("(".equals(top) || ")".equals(top)) throw new IllegalArgumentException("Mismatched parentheses in expression: " + expr);
            output.add(top);
        }
        return output;
    }

    // Shunting-yard helper: push operator respecting precedence/associativity
    private static void pushOperator(List<String> output, Deque<String> ops, String opSym) {
        if (!isOperator(opSym)) throw new IllegalArgumentException("Unknown operator: " + opSym);
        Op cur = OPS.get(opSym);
        while (!ops.isEmpty() && isOperator(ops.peek())) {
            Op top = OPS.get(ops.peek());
            if (top == null) break;
            if ((cur.assoc == Assoc.LEFT  && cur.prec <= top.prec) ||
                    (cur.assoc == Assoc.RIGHT && cur.prec <  top.prec)) {
                output.add(ops.pop());
            } else break;
        }
        ops.push(opSym);
    }

    // -------------------------
    // Core: RPN evaluation (single implementation)
    // -------------------------
    // If xValue == null, RPN must not contain "X"; otherwise "X" tokens are replaced by xValue.
    private static long evalRPN(List<String> rpn, Long xValue) {
        Deque<Long> st = new ArrayDeque<>();
        for (String tok : rpn) {
            if ("X".equals(tok)) {
                if (xValue == null) throw new IllegalArgumentException("RPN contains placeholder X but no xValue was provided");
                st.push(xValue);
                continue;
            }

            if (isOperator(tok)) {
                Op op = OPS.get(tok);
                if (op == null) throw new IllegalArgumentException("Unknown operator in RPN: " + tok);

                if (op.arity == 1) {
                    if (st.isEmpty()) throw new IllegalArgumentException("Invalid expression (missing operand)");
                    long a = st.pop();
                    long res;
                    if ("u-".equals(tok)) res = -a;
                    else throw new IllegalArgumentException("Unsupported unary operator: " + tok);
                    st.push(res);
                } else if (op.arity == 2) {
                    if (st.size() < 2) throw new IllegalArgumentException("Invalid expression (missing operands)");
                    long b = st.pop();
                    long a = st.pop();
                    long res;
                    switch (tok) {
                        case "+": res = a + b; break;
                        case "-": res = a - b; break;
                        case "*": res = a * b; break;
                        case "/":
                            if (b == 0) throw new ArithmeticException("Division by zero");
                            res = a / b;
                            break;
                        case "%":
                            if (b == 0) throw new ArithmeticException("Modulo by zero");
                            res = a % b;
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown binary operator during eval: " + tok);
                    }
                    st.push(res);
                } else {
                    throw new IllegalArgumentException("Unsupported operator arity: " + op.arity);
                }
            } else {
                // number literal
                try {
                    st.push(Long.parseLong(tok));
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Invalid number token in RPN: " + tok, ex);
                }
            }
        }

        if (st.size() != 1) throw new IllegalArgumentException("Invalid expression (stack size != 1)");
        return st.pop();
    }

    // Convenience wrapper for existing API compatibility
    private static long evalRPN(List<String> rpn) {
        return evalRPN(rpn, null);
    }
}
