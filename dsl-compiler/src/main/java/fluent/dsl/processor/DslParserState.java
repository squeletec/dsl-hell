package fluent.dsl.processor;

import fluent.dsl.model.*;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static fluent.dsl.model.DslUtils.capitalize;
import static fluent.dsl.model.DslUtils.simpleName;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.STATIC;

public class DslParserState {
    private final DslModel dsl;
    private final ParameterModel impl;

    public DslParserState(DslModel dsl, ParameterModel impl) {
        this.dsl = dsl;
        this.impl = impl;
    }

    public class InitialState implements DslParser.State {
        @Override public DslParser.State annotation(AnnotationModel annotationModel) {
            return this;
        }
        @Override public DslParser.State keyword(String name, List<String> aliases) {
            return new KeywordState(dsl, name, aliases);
        }
        @Override public DslParser.State method(String name) {
            return new DecideState(dsl, name);
        }
        @Override public DslParser.State constant(String name) {
            return this;
        }
        @Override public DslParser.State parameter(VariableElement parameterModel) {
            return this;
        }
        @Override public void bind(ExecutableElement method) {
        }
    }


    public class KeywordState implements DslParser.State {
        private final TypeModel model;
        private final String methodName;
        private final List<String> aliases;
        private final List<ParameterModel> parameters = new ArrayList<>();

        public KeywordState(TypeModel model, String name, List<String> aliases) {
            this.model = model;
            this.methodName = name;
            this.aliases = aliases;
        }
        @Override public DslParser.State annotation(AnnotationModel annotationModel) {
            return this;
        }
        @Override public DslParser.State keyword(String name, List<String> aliases) {
            return new KeywordState(finish(null), name, aliases);
        }
        @Override public DslParser.State method(String name) {
            return new DecideState(finish(null), name);
        }
        @Override public DslParser.State constant(String name) {
            parameters.add(dsl.addConstant(name));
            return this;
        }
        @Override public DslParser.State parameter(VariableElement parameterModel) {
            parameters.add(toModel(parameterModel));
            return this;
        }
        @Override public void bind(ExecutableElement method) {
            TypeModel returnTypeModel = new TypeModel(emptyList(), method.getReturnType().toString());
            KeywordModel body = new KeywordModel(emptyList(), returnTypeModel, method.getSimpleName().toString(), emptyList(), method.getParameters().stream().map(this::toModel).collect(toList()), null);
            BindingModel binding = new BindingModel(method.getModifiers().contains(STATIC) ? impl.type() : impl, body);
            finish(binding);
        }
        private TypeModel finish(BindingModel bindingModel) {
            return model.add(capitalize(methodName) + parameters.stream().map(p -> simpleName(p.type().name())).collect(joining()), methodName, this.aliases, parameters, bindingModel).type();
        }
        private ParameterModel toModel(VariableElement parameter) {
            return new ParameterModel(emptyList(), new TypeModel(emptyList(), parameter.asType().toString()), parameter.getSimpleName().toString());
        }
    }

    public class DecideState extends KeywordState implements DslParser.State {
        private final TypeModel model;
        private final String methodName;

        public DecideState(TypeModel model, String name) {
            super(model, name, emptyList());
            this.model = model;
            this.methodName = name;
        }
        @Override public DslParser.State keyword(String name, List<String> aliases) {
            return new KeywordState(model, name, aliases);
        }
        @Override public DslParser.State constant(String name) {
            return keyword(methodName, emptyList()).constant(name);
        }
        @Override public DslParser.State parameter(VariableElement parameterModel) {
            return keyword(methodName, emptyList()).parameter(parameterModel);
        }
    }
}
