package core;

import keywords.If;
import statements.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Parser {

    // List of the Lexer generated tokens from the source code
    private final List<Lexer.Token> tokens;

    // Queue of generated bytecode statements
    private final Queue<Statement> statements = new LinkedList<>();

    // Current selected directory to get variables and methods from
    private String currentDirectory = "global";

    // Current selected datatype for creating variables
    private String currentDataType = "int";

    // Current selected operator for math expression operations
    private String currentOperator = "+";

    // Current index of the given tokens
    private int index = 0;

    protected Parser(List<Lexer.Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * @return a new Queue of generated bytecode statements based on the tokens given to the Parser object
     */
    protected Queue<Statement> getStatements() {
        statements.add(new Directory(currentDirectory));
        statements.add(new Type(currentDataType));
        statements.add(new Operation(currentOperator));

        // Loop through tokens and decide which operation should happen to generate a bytecode statement for each token
        for (;index < tokens.size(); index++) identifyToken();
        return statements;
    }

    // Executes an action for adding statements based on token
    private void identifyToken(){
        switch (tokens.get(index).tokenType()) {
            case TYPE -> whenType();
            case IDENTIFIER -> whenIdentifier();
            case EQUALS -> whenEquals();
            case NUMBER -> whenNumber();
            case BINARY_OPERATOR -> whenBinaryOperation();
            case OPEN_PAREN -> whenOpenParen();
            case CLOSE_PAREN -> whenCloseParen();
            case END -> whenEnd();
            case KEYWORD -> whenKeyWord();
        }
    }

    /*
     * Adds bytecode statement for the current datatype if it's different from the current datatype
     */
    private void whenType() {
        String type = tokens.get(index).value();
        if (!currentDataType.equals(type)) {
            statements.add(new Type(type));
            currentDataType = type;
        }
    }

    /*
     * Adds bytecode statement for creating new a variable
     */
    private void whenIdentifier() {
        statements.add(new Variable(tokens.get(index).value()));
    }

    /*
     * Adds bytecode statements for an assignment operation.
     */
    private void whenEquals() {
        if (index < 1 || !tokens.get(index - 1).tokenType().equals(Lexer.TokenType.IDENTIFIER))
            throw new IllegalArgumentException("Illegal start of statement");

        // Add bytecode statements for loading the variable and setting its value to default
        statements.add(new Load(tokens.get(index - 1).value()));

        if (currentDataType.equals("int") || currentDataType.equals("float"))
            statements.add(new Setter("0"));
        else
            statements.add(new Setter("false"));

        if (!currentOperator.equals("+") && !currentDataType.equals("bool")) {
            statements.add(new Operation("+"));
            currentOperator = "+";
        }

        index++;

        generateMathExpression();
    }

    private void whenNumber() {
        throw new IllegalArgumentException("Illegal start of statement");
    }

    private void whenBinaryOperation() {
        throw new IllegalArgumentException("Illegal start of statement");
    }

    private void whenOpenParen() {
        throw new IllegalArgumentException("Illegal start of statement");
    }

    private void whenCloseParen() {
        throw new IllegalArgumentException("Illegal start of statement");
    }

    private void whenEnd() {
        throw new IllegalArgumentException("Illegal start of statement");
    }

    private void whenKeyWord() {
        if (tokens.get(index).value().equals("if")) {
            List<Lexer.Token> headTokens = new LinkedList<>();

            while (index < tokens.size() && tokens.get(index).tokenType() != Lexer.TokenType.OPEN_PAREN) {
                index++;

                headTokens.add(tokens.get(index));
            }

            statements.addAll(new If(headTokens).getKeywordBody());

            index++;
            addBody();
        }
    }

    // Generate bytecode statements for the math expression
    private void generateMathExpression() {
        while (tokens.get(index).tokenType() != Lexer.TokenType.END) {
            Lexer.Token token = tokens.get(index);
            Lexer.TokenType tokenType = token.tokenType();

            switch (tokenType) {
                case NUMBER -> statements.add(new Adder(tokens.get(index).value()));
                case IDENTIFIER -> statements.add(new Getter(tokens.get(index).value()));
                case BINARY_OPERATOR -> {
                    String type = tokens.get(index).value();
                    if (!currentOperator.equals(type)) {
                        statements.add(new Operation(type));
                        currentOperator = type;
                    }
                }
            }

            if (index == tokens.size() - 1)
                throw new IllegalArgumentException("Missing ; for ending statements");

            index++;
        }
    }

    // Add body statements for a keyword head
    private void addBody() {
        for (; tokens.get(index).tokenType() != Lexer.TokenType.CLOSE_PAREN; index++) identifyToken();
        statements.add(new End());
    }
}