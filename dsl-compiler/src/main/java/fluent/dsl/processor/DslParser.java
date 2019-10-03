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
import fluent.dsl.Parametrized;
import fluent.dsl.model.DslModel;
import fluent.dsl.model.ParameterModel;
import fluent.dsl.model.TypeModel;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class DslParser {

    private DslModel createModel(Element element) {
        Dsl dsl = element.getAnnotation(Dsl.class);
        String packageName = dsl.packageName().isEmpty() ? element.getEnclosingElement().toString() : dsl.packageName();
        String dslName = dsl.className().isEmpty() ? element.getSimpleName() + "Dsl" : dsl.className();
        ParameterModel source = new ParameterModel(emptyList(), new TypeModel(emptyList(), element.toString()), dsl.parameterName());
        return new DslModel(packageName, emptyList(), dslName, dsl.factoryMethod(), source, dsl.delegateMethod());
    }

    public DslModel parseModel(Element element) {
        DslModel dslModel = createModel(element);
        PrefixState prefix = start(dslModel);
        for(AnnotationMirror annotation : element.getAnnotationMirrors()) {
            Parametrized parametrized = annotation.getAnnotationType().asElement().getAnnotation(Parametrized.class);
            prefix = isKeyword(annotation) ? isNull(parametrized) ? prefix.keyword(annotation) : prefix.parametrizedKeyword(annotation, parametrized.value()) : prefix.annotation(annotation);
        }
        for(ExecutableElement method : ElementFilter.methodsIn(element.getEnclosedElements())) {
            MethodState state = prefix.method(method);
            for (VariableElement parameter : method.getParameters()) {
                for(AnnotationMirror annotation : parameter.getAnnotationMirrors()) {
                    Parametrized parametrized = annotation.getAnnotationType().asElement().getAnnotation(Parametrized.class);
                    state = isKeyword(annotation) ? isNull(parametrized) ? state.keyword(annotation) : state.parametrizedKeyword(annotation, parametrized.value()) : state.annotation(annotation);
                }
                state = state.parameter(parameter);
            }
            for(AnnotationMirror annotation : method.getAnnotationMirrors()) {
                Parametrized parametrized = annotation.getAnnotationType().asElement().getAnnotation(Parametrized.class);
                state = isKeyword(annotation) ? isNull(parametrized) ? state.keyword(annotation) : state.parametrizedKeyword(annotation, parametrized.value()) : state.annotation(annotation);
            }
            state.bind(method);
        }
        return dslModel;
    }

    private boolean isKeyword(AnnotationMirror annotationMirror) {
        return nonNull(annotationMirror.getAnnotationType().asElement().getAnnotation(Dsl.class));
    }

    public interface MethodState {
        MethodState annotation(AnnotationMirror annotationMirror);
        MethodState keyword(AnnotationMirror annotationMirror);
        MethodState parametrizedKeyword(AnnotationMirror annotationMirror, int count);
        MethodState parameter(VariableElement variableElement);
        void bind(ExecutableElement method);
    }

    public interface PrefixState {
        PrefixState annotation(AnnotationMirror annotationMirror);
        PrefixState keyword(AnnotationMirror annotationMirror);
        PrefixState parametrizedKeyword(AnnotationMirror annotationMirror, int count);
        MethodState method(ExecutableElement method);
    }

    private PrefixState start(DslModel model) {
        return new DslParserContext(model, model.factory().parameters().get(0)).new StartPrefix();
    }

}
