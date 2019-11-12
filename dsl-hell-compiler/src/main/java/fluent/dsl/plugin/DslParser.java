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
package fluent.dsl.plugin;

import fluent.api.model.*;
import fluent.dsl.Constant;
import fluent.dsl.Dsl;
import fluent.dsl.processor.DslAnnotationProcessorPlugin;
import fluent.dsl.processor.DslAnnotationProcessorPluginFactory;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.LinkedHashSet;
import java.util.Set;

import static fluent.dsl.plugin.DslUtils.*;
import static fluent.dsl.plugin.InitialState.start;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toCollection;
import static javax.lang.model.element.ElementKind.*;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.util.ElementFilter.methodsIn;

public class DslParser implements DslAnnotationProcessorPlugin {

    private final ModelFactory factory;

    public DslParser(ModelFactory factory) {
        this.factory = factory;
    }

    @Override
    public boolean isFor(Element element) {
        return element.getKind() == CLASS || element.getKind() == INTERFACE;
    }

    @Override
    public InterfaceModel process(Element element, Dsl dsl) {
        TypeModel<?> model = factory.type(element);

        String packageName = override(dsl.packageName(), model.packageName());
        String dslName = override(dsl.className(), model.rawType().simpleName() + "Dsl");

        VarModel source = factory.parameter(model, dsl.parameterName());
        InterfaceModel dslType = factory.interfaceModel(packageName, dslName).typeParameters(model.typeParameters());
        MethodModel factoryMethod = factory.staticMethod(dsl.factoryMethod(), singletonList(source)).typeParameters(model.typeParameters()).returnType(dslType);

        parseMethods(element, start(factory, dslType, PUBLIC), source);
        InterfaceModel delegate = factory.interfaceModel("", "Delegate").typeParameters(model.typeParameters());
        delegate.interfaces().add(dslType);
        delegate.methods().add(factory.method(dsl.delegateMethod()).returnType(dslType));
        dslType.methods().forEach(m -> {
            MethodModel model1 = factory.defaultMethod(m.name(), m.parameters()).returnType(m.returnType()).typeParameters(m.typeParameters());
            model1.body().add(factory.statementModel(factory.parameter(dslType, dsl.delegateMethod() + "()"), m));
            delegate.methods().add(model1);
        });
        dslType.methods().add(factoryMethod);
        dslType.types().add(delegate);
        return dslType;
    }



    private void parseMethods(Element element, State state, VarModel impl) {
        for(AnnotationMirror annotation : element.getAnnotationMirrors())
            state = annotation(state, annotation);
        for(ExecutableElement method : methodsIn(element.getEnclosedElements()))
            parseParameters(method, state.method(from(method)), impl);
    }

    private void parseParameters(ExecutableElement method, State state, VarModel impl) {
        for (VariableElement parameter : method.getParameters()) {
            for(AnnotationMirror annotation : parameter.getAnnotationMirrors())
                state = annotation(state, annotation);
            state = state.parameter(factory.parameter(parameter));
        }
        for(AnnotationMirror annotation : method.getAnnotationMirrors())
            state = annotation(state, annotation);
        MethodModel methodModel = factory.method(method);
        state.body(methodModel.returnType(), factory.statementModel(impl, methodModel));
    }

    public State annotation(State state, AnnotationMirror annotation) {
        Element element = annotation.getAnnotationType().asElement();
        if(nonNull(element.getAnnotation(Constant.class)))
            return state.constant(factory.constant(from(element)));
        if(nonNull(getDsl(element)))
            return state.keyword(from(element), aliases(element));
        return state;
    }

    private Set<String> aliases(Element element) {
        return element.getEnclosedElements().stream().filter(e -> e.getKind() == ANNOTATION_TYPE).map(DslUtils::from).collect(toCollection(LinkedHashSet::new));
    }

    public static final class Factory implements DslAnnotationProcessorPluginFactory {

        @Override
        public DslAnnotationProcessorPlugin createPlugin(ModelFactory factory) {
            return new DslParser(factory);
        }
    }
}
