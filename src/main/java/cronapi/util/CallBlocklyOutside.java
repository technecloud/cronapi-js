package cronapi.util;

import cronapi.Var;
import org.springframework.stereotype.Component;

@Component("blockly")
public class CallBlocklyOutside {

    public static Object call(String className, String methodName, Object... args) throws Exception {

        Var[] vars = new Var[args.length];

        for (int i = 0; i < args.length; i++) {
            vars[i] = Var.valueOf(args[i]);
        }

        Var result = cronapi.util.Operations.callBlockly(Var.valueOf(className + ":" + methodName), vars);

        return result.getObject();

    }
}