package fluent.dsl.processor;

import fluent.dsl.model.*;

import javax.lang.model.element.*;

import java.util.ArrayList;
import java.util.List;

import static fluent.dsl.processor.DslUtils.capitalize;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.ElementKind.ANNOTATION_TYPE;
import static javax.lang.model.element.Modifier.STATIC;

public class DslParserContext {

    private final DslModel dsl;
    private final ParameterModel impl;

    public DslParserContext(DslModel dsl, ParameterModel impl) {
        this.dsl = dsl;
        this.impl = impl;
    }

    private List<String> aliases(Element element) {
        return element.getEnclosedElements().stream().filter(e -> e.getKind() == ANNOTATION_TYPE).map(e -> e.getSimpleName().toString()).collect(toList());
    }

    public class StartPrefix implements DslParser.PrefixState {
        TypeModel finish() {
            return dsl;
        }
        @Override public DslParser.PrefixState annotation(AnnotationMirror annotationMirror) {
            return this;
        }
        @Override public DslParser.PrefixState keyword(AnnotationMirror annotationMirror) {
            return new PrefixKeyword(finish(), annotationMirror);
        }
        @Override public DslParser.MethodState method(ExecutableElement method) {
            return new StartMethodState(finish(), method.getSimpleName().toString());
        }
    }

    public class PrefixKeyword extends StartPrefix implements DslParser.PrefixState {
        private final TypeModel model;
        private final String name;
        private final List<String> aliases;

        public PrefixKeyword(TypeModel model, String name, List<String> aliases) {
            this.model = model;
            this.name = name;
            this.aliases = aliases;
        }
        public PrefixKeyword(TypeModel model, Element element) {
            this(model, element.getSimpleName().toString(), aliases(element));
        }
        public PrefixKeyword(TypeModel model, AnnotationMirror annotationMirror) {
            this(model, annotationMirror.getAnnotationType().asElement());
        }
        @Override TypeModel finish() {
            return model.add(capitalize(name), name, aliases, emptyList(), null).type();
        }
    }

    public class SimpleMethodState implements DslParser.MethodState {
        final TypeModel model;
        private final String name;
        private final List<String> aliases;
        final List<ParameterModel> parameters;

        public SimpleMethodState(TypeModel model, String name, List<String> aliases, List<ParameterModel> parameters) {
            this.model = model;
            this.name = name;
            this.aliases = aliases;
            this.parameters = parameters;
        }
        TypeModel finish(BindingModel binding) {
            return model.add(capitalize(name), name, aliases, parameters, binding).type();
        }
        ParameterModel parameterModel(VariableElement parameter) {
            return new ParameterModel(emptyList(), new TypeModel(emptyList(), parameter.asType().toString()), parameter.getSimpleName().toString());
        }
        @Override public DslParser.MethodState annotation(AnnotationMirror annotationMirror) {
            return this;
        }
        @Override public DslParser.MethodState keyword(AnnotationMirror annotationMirror) {
            Element element = annotationMirror.getAnnotationType().asElement();
            return new SimpleMethodState(finish(null), element.getSimpleName().toString(), aliases(element), emptyList());
        }
        @Override public DslParser.MethodState parameter(VariableElement variableElement) {
            return new SimpleMethodState(finish(null), variableElement.getSimpleName().toString(), emptyList(), singletonList(parameterModel(variableElement)));
        }
        @Override public void bind(ExecutableElement method) {
            TypeModel returnTypeModel = new TypeModel(emptyList(), method.getReturnType().toString());
            KeywordModel binding = new KeywordModel(emptyList(), returnTypeModel, method.getSimpleName().toString(), emptyList(), method.getParameters().stream().map(this::parameterModel).collect(toList()), null);
            finish(new BindingModel(method.getModifiers().contains(STATIC) ? impl.type() : impl, binding));
        }
    }

    public class StartMethodState extends SimpleMethodState implements DslParser.MethodState {
        public StartMethodState(TypeModel model, String name) {
            super(model, name, emptyList(), emptyList());
        }
        @Override public DslParser.MethodState keyword(AnnotationMirror annotationMirror) {
            Element element = annotationMirror.getAnnotationType().asElement();
            return new SimpleMethodState(model, element.getSimpleName().toString(), aliases(element), emptyList());
        }
    }
}
