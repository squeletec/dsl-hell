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
package fluent.dsl.parser;

import fluent.api.model.MethodModel;
import fluent.api.model.ModelFactory;
import fluent.api.model.TypeModel;
import fluent.api.model.VarModel;
import fluent.dsl.Dsl;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

import java.util.List;

import static fluent.dsl.model.DslUtils.override;
import static fluent.dsl.model.DslUtils.unCapitalize;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.util.ElementFilter.constructorsIn;
import static javax.lang.model.util.ElementFilter.methodsIn;

public class BuilderParser {

    private final ModelFactory factory;

    public BuilderParser(ModelFactory factory) {
        this.factory = factory;
    }

    public TypeModel parseModel(Element element) {
        TypeModel model = factory.parameter((VariableElement) element).type();
        Dsl dsl = element.getAnnotation(Dsl.class);

        String packageName = override(dsl.packageName(), model.packageName());
        String dslName = override(dsl.className(), model.rawType().simpleName() + "With");
        TypeModel dslModel = factory.type(packageName, dslName);
        TypeModel builderModel = factory.type("", "Builder");
        TypeModel builderImpl = factory.type("", "BuilderImpl");

        ParserState start = start(dslModel, null, STATIC);
        Element typeElement = ((DeclaredType) element.asType()).asElement();
        List<ExecutableElement> constructors = constructorsIn(typeElement.getEnclosedElements());
        List<ExecutableElement> setters = methodsIn(typeElement.getEnclosedElements()).stream().filter(this::isSetter).collect(toList());
        boolean isBuilder = constructors.size() > 1 || !setters.isEmpty();
        VarModel object = factory.parameter(model, "object");
        if(isBuilder) {
            dslModel.nestedClasses().add(builderModel);
            builderModel.fields().add(object);
        }
        for(ExecutableElement constructor : constructors) {
            ParserState state = start;
            for (VariableElement parameter : constructor.getParameters())
                state = state.keyword(parameter.getSimpleName().toString(), emptyList(), true).parameter(parameter);
            MethodModel constructorModel = factory.method(constructor);
            if(isBuilder)
                state.bind(factory.constructor(builderImpl, factory.parameter(model, constructorCall(constructorModel))));
            else
                state.bind(constructorModel);
        }
        ParserState builder = start(builderModel, object);
        for (ExecutableElement setter : setters)
            builder.keyword(unCapitalize(setter.getSimpleName().toString().substring(3)), emptyList(), true)
                    .parameter(setter.getParameters().get(0))
                    .bind(factory.method(setter), builderModel);
        return dslModel;
    }

    private boolean isSetter(ExecutableElement element) {
        return element.getSimpleName().toString().startsWith("set") && element.getParameters().size() == 1;
    }

    private ParserState start(TypeModel model, VarModel object, Modifier... modifiers) {
        return new ParserContext(factory, model, object).new InitialState(modifiers);
    }

    private String constructorCall(MethodModel constructor) {
        return "new " + constructor.returnType().fullName() + "(" + constructor.parameters().stream().map(VarModel::name).collect(joining(", ")) + ")";
    }
}
