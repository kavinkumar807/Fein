import java.util.List;
import java.util.Map;

/**
 * Class for FeinClass
 */
public class FeinClass implements FeinCallable {
    final String name;
    final FeinClass superclass;
    private final Map<String, FeinFunction> methods;

    public FeinClass(String name,FeinClass superclass, Map<String, FeinFunction> methods) {
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
    }

    FeinFunction findMethod(String name) {
        if(methods.containsKey(name)) {
            return methods.get(name);
        }

        if(superclass != null) {
            return superclass.findMethod(name);
        }

        return null;
    }

    @Override
    public Object call(Interpreter interpreter,
                       List<Object> arguments){
        FeinInstance instance = new FeinInstance(this);
        FeinFunction initializer = findMethod("init");
        if(initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        return instance;
    }

    @Override
    public int arity(){
        FeinFunction initializer = findMethod("init");
        if(initializer == null) return 0;
        return initializer.arity();
    }


    @Override
    public String toString() {
        return name;
    }
}
