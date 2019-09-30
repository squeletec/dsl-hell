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

import fluent.dsl.Dsl;
import fluent.dsl.model.*;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static fluent.dsl.processor.DslUtils.capitalize;
import static java.lang.annotation.ElementType.*;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.STATIC;

interface DslParserState {

    DslParserState accept(AnnotationMirror annotation);

    DslParserState branch();

    DslParserState accept(VariableElement parameter);

    DslParserState suffix();

    void bind(ExecutableElement method);

    DslParserState keyword(DeclaredType annotationType);

    DslParserState parameter(DeclaredType annotationType);

    DslParserState plugin(DeclaredType annotationType);


    class Start implements DslParserState {
        final KeywordModel model;
        final ParameterModel source;
        final List<AnnotationMirror> annotations = new ArrayList<>();


        public Start(KeywordModel model, ParameterModel source) {
            this.model = model;
            this.source = source;
        }

        @Override
        public DslParserState accept(AnnotationMirror annotation) {
            annotations.add(annotation);
            return this;
        }

        @Override
        public DslParserState branch() {
            return this;
        }

        private String simpleName(String name) {
            return name.substring(name.lastIndexOf(".") + 1);
        }
        @Override
        public DslParserState accept(VariableElement parameter) {
            return method(parameter.getSimpleName().toString(), parameterModel(parameter));
        }

        DslParserState method(String methodName, ParameterModel... parameters) {
            String className = capitalize(methodName) + stream(parameters).map(p -> simpleName(p.type().name())).collect(joining());
            KeywordModel keyword = model.type().add(className, methodName, emptyList(), asList(parameters));
            return new Start(keyword, source);

        }

        @Override
        public DslParserState suffix() {
            return this;
        }

        private List<AnnotationModel> annotationsFor(ElementType type) {
            return annotations.stream().filter(a -> {
                Target target = a.getAnnotationType().asElement().getAnnotation(Target.class);
                return target == null || stream(target.value()).anyMatch(Predicate.isEqual(type));
            }).map(a -> new AnnotationModel(a.toString())).collect(toList());
        }

        ParameterModel parameterModel(VariableElement parameter) {
            return new ParameterModel(annotationsFor(PARAMETER), new TypeModel(emptyList(), parameter.asType().toString()), parameter.getSimpleName().toString());
        }

        @Override
        public void bind(ExecutableElement method) {
            TypeModel returnTypeModel = new TypeModel(annotationsFor(TYPE), method.getReturnType().toString());
            KeywordModel binding = new KeywordModel(emptyList(), returnTypeModel, method.getSimpleName().toString(), emptyList(), method.getParameters().stream().map(this::parameterModel).collect(toList()));
            model.bind(new BindingModel(method.getModifiers().contains(STATIC) ? source.type() : source, binding));
        }

        @Override
        public DslParserState keyword(DeclaredType annotationType) {
            return method(annotationType.asElement().getSimpleName().toString());
        }

        @Override
        public DslParserState parameter(DeclaredType annotationType) {
            return new ParameterState(model, source, annotationType.asElement().getSimpleName().toString());
        }

        @Override
        public DslParserState plugin(DeclaredType annotationType) {
            try {
                model.type().extend(annotationType.asElement().getAnnotation(Dsl.Plugin.class).value().newInstance().generate(null, null));
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            } catch (MirroredTypeException e) {
                e.printStackTrace();
            }
            return parameter(annotationType);
        }

    }

    class ParameterState extends Start {

        private final String prefix;
        public ParameterState(KeywordModel model, ParameterModel source, String prefix) {
            super(model, source);
            this.prefix = prefix;
        }

        @Override
        public DslParserState accept(VariableElement parameter) {
            return method(prefix + capitalize(parameter.getSimpleName().toString()), parameterModel(parameter));
        }

        @Override
        public DslParserState keyword(DeclaredType annotationType) {
            String name = annotationType.asElement().getSimpleName().toString();
            ParameterModel entity = new ParameterModel(emptyList(), new TypeModel(emptyList(), name), name);
            return method(prefix, entity);
        }

        @Override
        public DslParserState parameter(DeclaredType annotationType) {
            return new ParameterState(model, source, prefix + capitalize(annotationType.asElement().getSimpleName().toString()));
        }
    }

}
