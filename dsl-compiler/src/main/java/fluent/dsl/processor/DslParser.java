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
import fluent.dsl.model.DslModel;
import fluent.dsl.model.ParameterModel;
import fluent.dsl.model.TypeModel;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.ElementFilter;

import static java.util.Collections.emptyList;

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
        DslParserState prefix = start(dslModel);
        for(AnnotationMirror annotation : element.getAnnotationMirrors()) {
            prefix = accept(prefix, annotation);
        }
        for(ExecutableElement method : ElementFilter.methodsIn(element.getEnclosedElements())) {
            DslParserState state = prefix.branch();
            for (VariableElement parameter : method.getParameters()) {
                for(AnnotationMirror annotation : parameter.getAnnotationMirrors()) {
                    state = accept(state, annotation);
                }
                state = state.accept(parameter);
            }
            state = state.suffix();
            for(AnnotationMirror annotation : method.getAnnotationMirrors()) {
                state = accept(state, annotation);
            }
            state.bind(method);
        }
        return dslModel;
    }


    private DslParserState start(DslModel model) {
        return new DslParserState.Start(model.factory(), model.factory().parameters().get(0));
    }


    private static DslParserState accept(DslParserState state, AnnotationMirror annotationMirror) {
        DeclaredType annotationType = annotationMirror.getAnnotationType();
        if(annotationType.asElement().getAnnotation(Dsl.Keyword.class) != null) {
            return state.keyword(annotationType);
        } if(annotationType.asElement().getAnnotation(Dsl.Parameter.class) != null) {
            return state.parameter(annotationType);
        } if(annotationType.asElement().getAnnotation(Dsl.Plugin.class) != null) {
            return state.plugin(annotationType);
        }
        return state.accept(annotationMirror);
    }
}
