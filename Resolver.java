import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Class for resolver
 */
public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void>{
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;

    public Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Enum for function type
     */
    private enum FunctionType {
        NONE,
        FUNCTION
    }



    @Override
    public Void visitBlockStmt(Stmt.Block stmt){
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if(stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt){
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        if(currentFunction == FunctionType.NONE) {
            Fein.error(stmt.keyword, "Can't return from top-level code.");
        }
        if(stmt.value != null) {
            resolve(stmt.value);
        }

        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt){
        declare(stmt.name);
        if(stmt.initializer != null){
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        if(!scopes.isEmpty()
                && scopes.peek().get(expr.name.lexeme) == Boolean.FALSE ) {
            Fein.error(expr.name, "Can't read local variable in its own initializer.");
        }

        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr){
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        resolve(expr.left);
        resolve(expr.right);

        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        resolve(expr.callee);

        for(Expr argument : expr.arguments){
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Expr.Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        resolve(expr.right);
        return null;
    }

    /**
     * Method to declare the variable statement for resolving
     *
     * @param name Token
     */
    private void declare(Token name) {
        if(scopes.isEmpty()) return;

        Map<String, Boolean> scope  = scopes.peek();
        if(scope.containsKey(name.lexeme)) {
            Fein.error(name, "Already a variable with this name in this scope.");
        }

        scope.put(name.lexeme, false);
    }

    /**
     * Method to define the variable statement for resolving
     *
     * @param name Token
     */
    private void define(Token name) {
        if(scopes.isEmpty()) return;

        scopes.peek().put(name.lexeme , true);
    }

    /**
     * Method to resolve function
     *
     * @param function Stmt.Function
     */
    private void resolveFunction(Stmt.Function function, FunctionType type){
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for(Token param : function.params) {
            declare(param);
            define(param);
        }

        resolve(function.body);
        endScope();
        currentFunction = enclosingFunction;
    }

    /**
     * Method to resolve local
     *
     * @param expr Expr
     * @param name Token
     */
    private void resolveLocal(Expr expr, Token name) {
        for(int i = scopes.size() - 1; i >= 0 ; i--){
            if(scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
    }

    /**
     * Method to resolve statements
     *
     * @param statements List<Stmt>
     */
    void resolve(List<Stmt> statements) {
        for(Stmt statement : statements){
            resolve(statement);
        }
    }

    /**
     * Method to resolve statement
     *
     * @param stmt Stmt
     */
    private void resolve(Stmt stmt){
        stmt.accept(this);
    }

    /**
     * Method to resolve expression
     *
     * @param expr Expr
     */
    private void resolve(Expr expr){
        expr.accept(this);
    }

    /**
     * Method to begin scope and push hashmap to stack
     */
    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    /**
     * Method to end scope and pop out of the stack
     */
    private void endScope(){
        scopes.pop();
    }


}
