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
import fluent.dsl.Constant;
import fluent.dsl.model.AnnotationModel;
import fluent.dsl.model.DslModel;
import fluent.dsl.model.ParameterModel;
import fluent.dsl.model.TypeModel;

import javax.lang.model.element.*;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.ElementKind.ANNOTATION_TYPE;
import static javax.lang.model.util.ElementFilter.methodsIn;

public class DslParser {

    private DslModel createModel(Element element) {
        Dsl dsl = element.getAnnotation(Dsl.class);
        String packageName = dsl.packageName().isEmpty() ? element.getEnclosingElement().toString() : dsl.packageName();
        String dslName = dsl.className().isEmpty() ? element.getSimpleName() + "Dsl" : dsl.className();
        ParameterModel source = new ParameterModel(emptyList(), new TypeModel(emptyList(), element.toString()), dsl.parameterName());
        return new DslModel(packageName, new ArrayList<>(), dslName, dsl.factoryMethod(), source, dsl.delegateMethod());
    }

    public DslModel parseModel(Element element) {
        DslModel model = createModel(element);
        State prefix = start(model);
        for(AnnotationMirror annotation : element.getAnnotationMirrors())
            prefix = annotationState(prefix, annotation);
        for(ExecutableElement method : methodsIn(element.getEnclosedElements())) {
            State state = prefix.method(method.getSimpleName().toString());
            for (VariableElement parameter : method.getParameters()) {
                for(AnnotationMirror annotation : parameter.getAnnotationMirrors())
                    state = annotationState(state, annotation);
                state = state.parameter(parameter);
            }
            for(AnnotationMirror annotation : method.getAnnotationMirrors())
                state = annotationState(state, annotation);
            state.bind(method);
        }
        return model;
    }

    private State annotationState(State prev, AnnotationMirror annotation) {
        Element element = annotation.getAnnotationType().asElement();
        String name = element.getSimpleName().toString();
        if(nonNull(element.getAnnotation(Constant.class)))
            return prev.constant(name);
        if(isKeyword(element))
            return prev.keyword(name, element.getEnclosedElements().stream().filter(e -> e.getKind() == ANNOTATION_TYPE).map(e -> e.getSimpleName().toString()).collect(toList()));
        return prev.annotation(new AnnotationModel(annotation.toString()));
    }

    private boolean isKeyword(Element element) {
        if(nonNull(element.getAnnotation(Dsl.class)))
            return true;
        Element enclosingElement = element.getEnclosingElement();
        if(nonNull(enclosingElement))
            return isKeyword(enclosingElement);
        try {
            Element packageElement = (Element) element.getClass().getField("owner").get(element);
            if(nonNull(packageElement))
                return isKeyword(packageElement);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return false;
    }

    public interface State {
        State annotation(AnnotationModel annotationModel);
        State keyword(String name, List<String> aliases);
        State method(String name);
        State constant(String name);
        State parameter(VariableElement parameterModel);
        void bind(ExecutableElement method);
    }

    private State start(DslModel model) {
        return new DslParserState(model, model.factory().parameters().get(0)).new InitialState();
    }

}
