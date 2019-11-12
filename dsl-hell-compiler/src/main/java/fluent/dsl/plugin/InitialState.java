package fluent.dsl.plugin;

import fluent.api.model.*;

import javax.lang.model.element.Modifier;
import java.util.*;

import static fluent.dsl.plugin.DslUtils.capitalize;
import static fluent.dsl.plugin.DslUtils.usedTypeParameters;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static javax.lang.model.element.Modifier.PUBLIC;

public class InitialState implements State {
    private final ModelFactory factory;
    private final TypeModel rootTypeModel;
    private final Modifier[] initialModifiers;

    private InitialState(ModelFactory factory, TypeModel rootTypeModel, Modifier[] initialModifiers) {
        this.factory = factory;
        this.rootTypeModel = rootTypeModel;
        this.initialModifiers = initialModifiers;
    }
    public static State start(ModelFactory factory, TypeModel rootTypeModel, Modifier... modifiers) {
        return new InitialState(factory, rootTypeModel, modifiers);
    }
    @Override public State method(String name) {
        return new MethodState(rootTypeModel, name);
    }
    @Override public State keyword(String name, Set<String> aliases) {
        return new KeywordState(rootTypeModel, name, aliases, initialModifiers);
    }
    @Override public State parameter(VarModel variable) {
        return this;
    }
    @Override public State constant(VarModel constant) {
        return this;
    }
    @Override public void body(TypeModel returnType, StatementModel... statement) {
    }
    private static String signatureKey(String name, List<VarModel> parameters) {
        return capitalize(name) + parameters.stream().map(p -> DslUtils.simpleName(p.type())).collect(joining());
    }

    private class KeywordState implements State {
        private final TypeModel<?> typeModel;
        private final String methodName;
        private final Set<String> aliases;
        private final Modifier[] modifiers;
        private List<VarModel> parameters = new ArrayList<>();
        private final Map<String, MethodModel> methodSignatures;

        private KeywordState(TypeModel<?> typeModel, String methodName, Set<String> aliases, Modifier... modifiers) {
            this.typeModel = typeModel;
            this.methodName = methodName;
            this.aliases = aliases;
            methodSignatures = typeModel.methods().stream().collect(toMap(m -> signatureKey(m.name(), m.parameters()), identity()));
            this.modifiers = modifiers;
        }
        private MethodModel reduce(TypeModel returnType) {
            return methodSignatures.computeIfAbsent(signatureKey(methodName, parameters), key -> addMethod(key, returnType));
        }
        private MethodModel addMethod(String key, TypeModel<?> returnType) {
            List<TypeModel> typeParameters = this.typeModel.typeParameters();
            Map<String, TypeModel> map = new LinkedHashMap<>();
            typeParameters.forEach(p -> map.put(p.fullName(), p));
            usedTypeParameters(parameters).forEach(t -> map.put(t.fullName(), t));
            ArrayList<TypeModel> newParameters = new ArrayList<>(map.values());
            List<TypeModel> methodTypeParameters = newParameters.subList(typeParameters.size(), newParameters.size());


            if(isNull(returnType)) {
                returnType = factory.interfaceModel("", key).typeParameters(newParameters);
                typeModel.types().add(returnType);
            }
            MethodModel method = factory.method(asList(modifiers), methodName, parameters).returnType(returnType).typeParameters(methodTypeParameters);
            typeModel.methods().add(method);
            for(String alias : aliases) {
                MethodModel aliasMethod = factory.defaultMethod(alias, parameters).returnType(returnType).typeParameters(methodTypeParameters);
                aliasMethod.body().add(factory.statementModel(factory.parameter(typeModel, "this"), method));
                typeModel.methods().add(aliasMethod);

            }

            method.metadata().put("aliases", aliases);
            return method;
        }
        @Override public State method(String name) {
            return new MethodState(reduce(null).returnType(), name);
        }
        @Override public State keyword(String name, Set<String> aliases) {
            return new KeywordState(reduce(null).returnType(), name, aliases, PUBLIC);
        }
        @Override public State parameter(VarModel parameter) {
            parameters.add(parameter);
            return this;
        }
        @Override public State constant(VarModel constant) {
            rootTypeModel.fields().putIfAbsent(constant.name(), constant);
            return parameter(constant);
        }
        @Override public void body(TypeModel returnType, StatementModel... statement) {
            reduce(returnType).body().addAll(asList(statement));
        }
    }


    private class MethodState implements State {
        private final TypeModel model;
        private final String methodName;

        private MethodState(TypeModel model, String methodName) {
            this.model = model;
            this.methodName = methodName;
        }
        @Override public State method(String name) {
            return keyword(methodName).method(name);
        }
        @Override public State keyword(String name, Set<String> aliases) {
            return new KeywordState(model, name, aliases, PUBLIC);
        }
        @Override public State parameter(VarModel variable) {
            return keyword(methodName).parameter(variable);
        }
        @Override public State constant(VarModel constant) {
            return keyword(methodName).constant(constant);
        }
        @Override public void body(TypeModel returnType, StatementModel... statement) {
            keyword(methodName).body(returnType, statement);
        }
    }

}
