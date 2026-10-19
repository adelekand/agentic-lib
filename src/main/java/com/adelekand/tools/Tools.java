package com.adelekand.tools;

import com.adelekand.tools.annotations.AgentTool;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Tools {
    private Tools() {}

    public static List<Tool> from(Object target) {
        List<Tool> out = new ArrayList<>();
        for (Method m : target.getClass().getMethods()) {
            AgentTool a = m.getAnnotation(AgentTool.class);
            if (a != null) out.add(new ReflectiveTool(target, m, a));
        }
        return out;
    }
}
