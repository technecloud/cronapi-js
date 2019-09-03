package cronapi.util;

import cronapi.Var;
import org.springframework.stereotype.Component;

@Component("blockly")
public class CallBlocklyOutside {

    public static Object call(Object... args) throws Exception {

        String className = (String) args[0];
        String methodName = (String) args[1];

        Var[] vars = new Var[args.length - 2];

        for (int i = 2; i < args.length; i++) {
            vars[i-2] = Var.valueOf(args[i]);
        }

        Var result = cronapi.util.Operations.callBlockly(Var.valueOf(className + ":" + methodName), vars);

        return result.getObject();

    }
}