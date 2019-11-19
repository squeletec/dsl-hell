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
import fluent.dsl.Dsl;
import fluent.dsl.processor.DslAnnotationProcessorPlugin;
import fluent.dsl.processor.DslAnnotationProcessorPluginFactory;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import java.util.List;
import java.util.function.Function;

import static fluent.dsl.plugin.DslUtils.*;
import static fluent.dsl.plugin.InitialState.start;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static javax.lang.model.element.ElementKind.FIELD;
import static javax.lang.model.element.ElementKind.PARAMETER;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.util.ElementFilter.constructorsIn;
import static javax.lang.model.util.ElementFilter.methodsIn;

public class BuilderParser implements DslAnnotationProcessorPlugin {

    private final ModelFactory factory;

    public BuilderParser(ModelFactory factory) {
        this.factory = factory;
    }

    @Override
    public boolean isFor(Element element) {
        return element.getKind() == PARAMETER || element.getKind() == FIELD;
    }

    @Override
    public InterfaceModel process(Element element, Dsl dsl) {
        TypeModel<?> model = factory.parameter((VariableElement) element).type();

        String packageName = override(dsl.packageName(), model.packageName());
        String dslName = override(dsl.className(), model.rawType().simpleName() + "With");
        InterfaceModel dslModel = factory.interfaceModel(packageName, dslName);

        Element typeElement = ((DeclaredType) element.asType()).asElement();
        List<ExecutableElement> constructors = constructorsIn(typeElement.getEnclosedElements());
        boolean hasSetters = methodsIn(typeElement.getEnclosedElements()).stream().anyMatch(DslUtils::isSetter);
        if(hasSetters || constructors.size() > 1) {
            InterfaceModel builderModel = factory.interfaceModel("", "Builder").typeParameters(model.typeParameters());
            ClassModel builderImpl = factory.classModel("", "BuilderImpl").typeParameters(builderModel.typeParameters());
            VarModel object = factory.parameter(model, "object");
            builderImpl.interfaces().add(builderModel);
            builderImpl.fields().put(object.name(), object);
            MethodModel constructor = factory.constructor(builderImpl, object);
            constructor.body().add(new StatementModel() {
                @Override public String toString() {
                    return "this." + object.name() + " = " + object.name() + ";";
                }
            });
            builderImpl.methods().add(constructor);

            readConstructors(typeElement, start(factory, dslModel, PUBLIC, STATIC), c -> builderConstructor(c, builderImpl), builderModel);
            State state = start(factory, builderModel, PUBLIC);
            VarModel thisModel = factory.parameter(builderImpl, "this");
            for (ExecutableElement method : methodsIn(typeElement.getEnclosedElements()))
                if(isSetter(method))
                    state.keyword(unCapitalize(method.getSimpleName().toString().substring(3)))
                            .parameter(factory.parameter(method.getParameters().get(0)))
                            .body(builderModel, factory.statementModel(object, factory.method(method)), factory.statementModel(thisModel, null));
            dslModel.types().add(builderModel);
            dslModel.types().add(builderImpl);
            MethodModel buildMethod = factory.method("build").returnType(model);
            buildMethod.body().add(factory.statementModel(object, null));
            builderModel.methods().add(buildMethod);
            builderImpl.methods().addAll(builderModel.methods());
        } else if(constructors.size() == 1 && constructors.get(0).getParameters().size() > 0) {
            readConstructors(typeElement, start(factory, dslModel, PUBLIC, STATIC), identity(), model);
        }
        return dslModel;
    }

    private String constructorCall(MethodModel constructor) {
        return "new " + constructor.returnType().fullName() + "(" + constructor.parameters().stream().map(VarModel::name).collect(joining(", ")) + ")";
    }

    private void readConstructors(Element element, State state, Function<MethodModel, MethodModel> constructorFactory, TypeModel returnType) {
        for(ExecutableElement constructor : constructorsIn(element.getEnclosedElements()))
            readMethodParameters(constructor, state, constructorFactory, returnType);
    }

    private void readMethodParameters(ExecutableElement method, State state, Function<MethodModel, MethodModel> constructor, TypeModel returnType) {
        for (VariableElement parameter : method.getParameters())
            state = state.keyword(from(parameter)).parameter(factory.parameter(parameter));
        state.body(returnType, factory.statementModel(null, constructor.apply(factory.method(method))));
    }

    private MethodModel builderConstructor(MethodModel constructor, TypeModel typeModel) {
        return factory.constructor(typeModel, factory.parameter(constructor.returnType(), constructorCall(constructor)));
    }

    public static final class Factory implements DslAnnotationProcessorPluginFactory {

        @Override
        public DslAnnotationProcessorPlugin createPlugin(ModelFactory factory) {
            return new BuilderParser(factory);
        }
    }
}
