package com.adelekand.tools;

import com.adelekand.messages.Json;
import com.adelekand.tools.annotations.AgentTool;
import com.adelekand.tools.annotations.Param;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.lang.reflect.*;
import java.util.List;

final public class ReflectiveTool implements Tool {
    private final Object target;
    private final Method method;
    private final ToolSpec spec;

    ReflectiveTool(Object target, Method method, AgentTool a) {
        this.target = target;
        this.method = method;
        for (Parameter p : method.getParameters()) {
            if (p.getName().matches("arg\\d+")) {
                throw new IllegalStateException("Compile with -parameters so tool argument names are available");
            }
        }
        String name = a.name().isEmpty() ? toSnake(method.getName()) : a.name();
        this.spec = new ToolSpec(name, a.description(), schema(method));
    }

    @Override public ToolSpec spec() { return spec; }

    @Override
    public String call(JsonNode args) throws Exception {
        Parameter[] ps = method.getParameters();
        Object[] values = new Object[ps.length];
        for (int i = 0; i < ps.length; i++) {
            JsonNode v = args.get(ps[i].getName());
            if (v == null || v.isNull()) {
                if (isRequired(ps[i])) throw new IllegalArgumentException("missing argument '" + ps[i].getName() + "'");
                values[i] = defaultFor(ps[i].getType());
            } else {
                values[i] = Json.MAPPER.convertValue(v, Json.MAPPER.constructType(ps[i].getParameterizedType()));
            }
        }
        try {
            Object result = method.invoke(target, values);
            return result instanceof String s ? s : Json.write(result);
        } catch (InvocationTargetException e) {
            throw e.getCause() instanceof Exception ex ? ex : e;
        }
    }

    private static ObjectNode schema(Method m) {
        ObjectNode root = Json.object().put("type", "object");
        ObjectNode props = root.putObject("properties");
        ArrayNode required = root.putArray("required");
        for (Parameter p : m.getParameters()) {
            ObjectNode prop = typeSchema(p.getType(), p.getParameterizedType());
            Param ann = p.getAnnotation(Param.class);
            if (ann != null && !ann.description().isEmpty()) prop.put("description", ann.description());
            props.set(p.getName(), prop);
            if (isRequired(p)) required.add(p.getName());
        }
        return root;
    }

    private static ObjectNode typeSchema(Class<?> c, Type generic) {
        ObjectNode n = Json.object();
        if (c == String.class) n.put("type", "string");
        else if (c == int.class || c == Integer.class || c == long.class || c == Long.class) n.put("type", "integer");
        else if (c == double.class || c == Double.class || c == float.class || c == Float.class) n.put("type", "number");
        else if (c == boolean.class || c == Boolean.class) n.put("type", "boolean");
        else if (c.isEnum()) {
            n.put("type", "string");
            ArrayNode values = n.putArray("enum");
            for (Object k : c.getEnumConstants()) values.add(((Enum<?>) k).name());
        } else if (List.class.isAssignableFrom(c)) {
            n.put("type", "array");
            if (generic instanceof ParameterizedType pt && pt.getActualTypeArguments()[0] instanceof Class<?> item) {
                n.set("items", typeSchema(item, item));
            }
        } else n.put("type", "object");
        return n;
    }

    private static boolean isRequired(Parameter p) {
        Param a = p.getAnnotation(Param.class);
        return a == null || a.required();
    }

    private static Object defaultFor(Class<?> c) {
        if (c == boolean.class) return false;
        if (c == int.class) return 0;
        if (c == long.class) return 0L;
        if (c == double.class) return 0.0;
        if (c == float.class) return 0f;
        return null;
    }

    private static String toSnake(String s) {
        return s.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }
}
