import java.util.List;

/**
 * Class for interpreter
 */
public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>{

    private Environment environment = new Environment();

    /**
     * Method to interpret the code
     *
     * @param statements List<Stmt>
     */
    void interpret(List<Stmt> statements){
        try{
            for(Stmt statement : statements){
                execute(statement);
            }
        } catch (RuntimeError error){
            Fein.runtimeError(error);
        }
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr){
        return expr.value;
    }


    @Override
    public Object visitGroupingExpr(Expr.Grouping expr){
        return evaluate(expr.expression);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr){
        Object right = evaluate(expr.right);

        switch (expr.operator.type){
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
        }

        // Unreachable.
        return null;
    }

    /**
     * Method to validate number operand
     *
     * @param operator Token
     * @param operand Object
     */
    private void checkNumberOperand(Token operator, Object operand) {
        if(operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number");
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr){
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type){
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }

                if (left instanceof String && right instanceof String) {
                    return left + (String)right;
                }

                if( left instanceof String && right instanceof Double){
                    return left + stringify(right);
                }

                if( left instanceof Double && right instanceof String ){
                    return stringify(left) + right;
                }
                throw new RuntimeError(expr.operator, "Operands must be numbers or strings.");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if(right instanceof Double && (right.toString().startsWith("0"))){
                    throw new RuntimeError(expr.operator, "Cannot divide by 0.");
                }
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
        }


        // Unreachable.
        return null;
    }

    /**
     * Method to visit expression statement
     *
     * @param stmt Stmt.Expression
     * @return Void
     */
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt){
        evaluate(stmt.expression);
        return null;
    }

    /**
     * Method to visit print stmt
     *
     * @param stmt Stmt.Print
     * @return Void
     */
    @Override
    public Void visitPrintStmt(Stmt.Print stmt){
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    /**
     * Method to evaluate variable statements
     *
     * @param stmt Stmt.Var
     * @return Void
     */
    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if(stmt.initializer != null){
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    /**
     * Method to process variable expression
     *
     * @param expr Expr.Variable
     * @return Object
     */
    @Override
    public Object visitVariableExpr(Expr.Variable expr){
        return environment.get(expr.name);
    }

    /**
     * Method to process assignment
     *
     * @param expr Expr.Assign
     * @return Object
     */
    @Override
    public Object visitAssignExpr(Expr.Assign expr){
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt){
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt){
        if(isTruthy(evaluate(stmt.condition))){
            execute(stmt.thenBranch);
        } else if(stmt.elseBranch != null){
            execute(stmt.elseBranch);
        }

        return null;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr){
        Object left = evaluate(expr.left);

        if(expr.operator.type == TokenType.OR){
            if(isTruthy(left)) return left;
        } else {
            if(!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt){
        while(isTruthy(evaluate(stmt.condition))){
            execute(stmt.body);
        }

        return null;
    }

    /**
     * Method to validate number operands
     *
     * @param operator Token
     * @param left Object
     * @param right Object
     */
    private void checkNumberOperands(Token operator, Object left, Object right) {
        if(left instanceof Double && right instanceof Double) return;

        throw new RuntimeError(operator, "Operands must be numbers");
    }

    /**
     * Method to check Truthy
     * @param object Object
     *
     * @return boolean
     */
    private boolean isTruthy(Object object){
        if(object == null ) return false;
        if(object instanceof  Boolean) return  (boolean) object;
        return true;
    }

    /**
     * Method to check equality
     *
     * @param a Object
     * @param b Object
     * @return boolean
     */
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    /**
     * Method to stringfy the object
     *
     * @param object Object
     *
     * @return String
     */
    private String stringify(Object object){
        if(object == null) return "nil";

        if(object instanceof Double){
            String text = object.toString();
            if(text.endsWith(".0")){
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }


    /**
     * Method to evaluate expression which calls interpreter's visitor implementation
     *
     * @param expr Expr
     * @return Object
     */
    private Object evaluate(Expr expr){
        return expr.accept(this);
    }

    /**
     * Method to execute statements
     *
     * @param stmt Stmt
     */
    private void execute(Stmt stmt){
        stmt.accept(this);
    }

    /**
     * Method to execute block statments
     *
     * @param statements List<Stmt>
     * @param environment Environment
     */
    void executeBlock(List<Stmt> statements, Environment environment){
        Environment previous = this.environment;
        try{
            this.environment = environment;

            for(Stmt statement : statements){
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }
}
