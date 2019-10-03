/*
 * BSD 2-Clause License
 *
 * Copyright (c) 2019, Ondrej Fischer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package fluent.dsl.processor;

import fluent.dsl.model.*;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static fluent.dsl.processor.DslUtils.capitalize;
import static java.lang.annotation.ElementType.*;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.ElementKind.ANNOTATION_TYPE;
import static javax.lang.model.element.Modifier.STATIC;

interface DslParserState {

    DslParserState accept(AnnotationMirror annotation);

    DslParserState branch(Element element);

    DslParserState accept(VariableElement parameter);

    DslParserState suffix();

    void bind(ExecutableElement method);

    DslParserState keyword(DeclaredType annotationType);

    DslParserState parameter(DeclaredType annotationType);

    class SimpleState implements DslParserState {
        final ParameterModel source;
        final TypeModel model;
        private final String className;
        private final String methodName;
        private final List<String> aliases;
        private final List<ParameterModel> parameters;
        private final List<AnnotationMirror> annotations = new ArrayList<>();

        public SimpleState(ParameterModel source, TypeModel model, String className, String methodName, List<String> aliases, List<ParameterModel> parameters) {
            this.source = source;
            this.model = model;
            this.className = className;
            this.methodName = methodName;
            this.aliases = aliases;
            this.parameters = parameters;
        }

        @Override
        public DslParserState accept(AnnotationMirror annotation) {
            annotations.add(annotation);
            return this;
        }

        TypeModel finish(BindingModel binding) {
            return model.add(className, methodName, aliases, parameters, binding).type();
        }

        @Override
        public DslParserState branch(Element element) {
            String methodName = element.getSimpleName().toString();
            return new SimpleState(source, finish(null), capitalize(methodName), methodName, emptyList(), emptyList());
        }

        @Override
        public DslParserState accept(VariableElement parameter) {
            String methodName = parameter.getSimpleName().toString();
            return new SimpleState(source, finish(null), capitalize(methodName) + parameter.asType().toString(), methodName, emptyList(), singletonList(parameterModel(parameter)));
        }

        @Override
        public DslParserState suffix() {
            return this;
        }

        @Override
        public void bind(ExecutableElement method) {
            TypeModel returnTypeModel = new TypeModel(annotationsFor(TYPE), method.getReturnType().toString());
            KeywordModel binding = new KeywordModel(emptyList(), returnTypeModel, method.getSimpleName().toString(), emptyList(), method.getParameters().stream().map(this::parameterModel).collect(toList()), null);
            finish(new BindingModel(method.getModifiers().contains(STATIC) ? source.type() : source, binding));
        }

        @Override
        public DslParserState keyword(DeclaredType annotationType) {
            return branch(annotationType.asElement());
        }

        @Override
        public DslParserState parameter(DeclaredType annotationType) {
            return null;
        }

        List<AnnotationModel> annotationsFor(ElementType type) {
            return annotations.stream().filter(a -> {
                Target target = a.getAnnotationType().asElement().getAnnotation(Target.class);
                return target == null || stream(target.value()).anyMatch(isEqual(type));
            }).map(a -> new AnnotationModel(a.toString())).collect(toList());
        }

        ParameterModel parameterModel(VariableElement parameter) {
            return new ParameterModel(annotationsFor(PARAMETER), new TypeModel(emptyList(), parameter.asType().toString()), parameter.getSimpleName().toString());
        }

    }

    class MethodState extends SimpleState {

        public MethodState(ParameterModel source, TypeModel model, String className, String methodName, List<String> aliases, List<ParameterModel> parameters) {
            super(source, model, className, methodName, aliases, parameters);
        }

        @Override
        public DslParserState accept(VariableElement parameter) {
            String methodName = parameter.getSimpleName().toString();
            return new SimpleState(source, model, capitalize(methodName) + parameter.asType().toString(), methodName, emptyList(), singletonList(parameterModel(parameter)));
        }

    }

    class StartState extends SimpleState {

        public StartState(ParameterModel source, TypeModel model, String className, String methodName, List<String> aliases, List<ParameterModel> parameters) {
            super(source, model, className, methodName, aliases, parameters);
        }

        @Override
        TypeModel finish(BindingModel binding) {
            return model;
        }
    }

    /*
    abstract class BaseState implements DslParserState {
        final TypeModel parent;
        private final List<AnnotationMirror> annotations = new ArrayList<>();
        final ParameterModel source;
        private final List<ParameterModel> parameters;

        public BaseState(TypeModel parent, ParameterModel source, List<ParameterModel> parameters) {
            this.parent = parent;
            this.source = source;
            this.parameters = parameters;
        }

        @Override
        public DslParserState accept(AnnotationMirror annotation) {
            annotations.add(annotation);
            return this;
        }

        @Override
        public DslParserState accept(VariableElement parameter) {
            parameters.add(parameterModel(parameter));
            return this;
        }

        @Override
        public DslParserState paremeter(DeclaredType annotationType) {
            TypeModel typeModel = new TypeModel(emptyList(), annotationType.asElement().getSimpleName().toString());
            parameters.add(new ParameterModel(emptyList(), typeModel, typeModel.name()));
            return this;
        }

        List<AnnotationModel> annotationsFor(ElementType type) {
            return annotations.stream().filter(a -> {
                Target target = a.getAnnotationType().asElement().getAnnotation(Target.class);
                return target == null || stream(target.value()).anyMatch(Predicate.isEqual(type));
            }).map(a -> new AnnotationModel(a.toString())).collect(toList());
        }

        ParameterModel parameterModel(VariableElement parameter) {
            return new ParameterModel(annotationsFor(PARAMETER), new TypeModel(emptyList(), parameter.asType().toString()), parameter.getSimpleName().toString());
        }

        private String simpleName(String name) {
            return name.substring(name.lastIndexOf(".") + 1);
        }

        KeywordModel kw(String methodName, List<String> aliases, BindingModel bindingModel) {
            return parent.add(capitalize(methodName) + parameters.stream().map(p -> simpleName(p.type().name())).collect(joining()), methodName, aliases, parameters, bindingModel);
        }

    }

    class Start extends BaseState implements DslParserState {
        private final Element model;

        public Start(TypeModel parent, Element model, ParameterModel source, ParameterModel parameter) {
            super(parent, source, singletonList(parameter));
            this.model = model;
        }

        public Start(TypeModel parent, Element model, ParameterModel source) {
            super(parent, source, emptyList());
            this.model = model;
        }

        @Override
        public DslParserState branch(Element element) {
            return new Start(parent, element, source);
        }

        @Override
        public DslParserState accept(VariableElement parameter) {
            KeywordModel keyword = kw(model.getSimpleName().toString(), emptyList(), null);
            return new Start(keyword.type(), parameter, source, parameterModel(parameter));
        }

        @Override
        public DslParserState suffix() {
            return this;
        }

        @Override
        public void bind(ExecutableElement method) {
            TypeModel returnTypeModel = new TypeModel(annotationsFor(TYPE), method.getReturnType().toString());
            KeywordModel binding = new KeywordModel(emptyList(), returnTypeModel, method.getSimpleName().toString(), emptyList(), method.getParameters().stream().map(this::parameterModel).collect(toList()), null);
            BindingModel bindingModel = new BindingModel(method.getModifiers().contains(STATIC) ? source.type() : source, binding);
            kw(model.getSimpleName().toString(), emptyList(), bindingModel);
        }

        @Override
        public DslParserState keyword(DeclaredType annotationType) {
            return new Keyword(parent, annotationType, source);
        }
    }

    class Keyword extends BaseState implements DslParserState {
        final ParameterModel source;
        final DeclaredType keyword;


        public Keyword(TypeModel parent, DeclaredType keyword, ParameterModel source) {
            super(parent, source, new ArrayList<>());
            this.keyword = keyword;
            this.source = source;
        }

        @Override
        public DslParserState branch(Element element) {
            return this;
        }

        @Override
        public DslParserState suffix() {
            return this;
        }

        @Override
        public void bind(ExecutableElement method) {
            TypeModel returnTypeModel = new TypeModel(annotationsFor(TYPE), method.getReturnType().toString());
            KeywordModel binding = new KeywordModel(emptyList(), returnTypeModel, method.getSimpleName().toString(), emptyList(), method.getParameters().stream().map(this::parameterModel).collect(toList()), null);
            BindingModel bindingModel = new BindingModel(method.getModifiers().contains(STATIC) ? source.type() : source, binding);
            kw(bindingModel);
        }

        @Override
        public DslParserState keyword(DeclaredType annotationType) {
            KeywordModel model = kw(null);
            return new Keyword(model.type(), annotationType, source);
        }

        private KeywordModel kw(BindingModel bindingModel) {
            Element element = keyword.asElement();
            return kw(
                    element.getSimpleName().toString(),
                    element.getEnclosedElements().stream().filter(e -> e.getKind() == ANNOTATION_TYPE).map(e -> e.getSimpleName().toString()).collect(toList()),
                    bindingModel
            );
        }
    }
*/
}
