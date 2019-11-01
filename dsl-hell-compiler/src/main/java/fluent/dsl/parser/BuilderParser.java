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

import fluent.api.model.ModelFactory;
import fluent.api.model.TypeModel;
import fluent.dsl.Dsl;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

import static fluent.dsl.model.DslUtils.override;
import static fluent.dsl.model.DslUtils.unCapitalize;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static javax.lang.model.element.Modifier.PUBLIC;
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

        ParserState start = start(dslModel);
        for(ExecutableElement constructor : constructorsIn(((DeclaredType) element.asType()).asElement().getEnclosedElements())) {
            ParserState state = start;
            for (VariableElement parameter : constructor.getParameters())
                state = state.keyword(parameter.getSimpleName().toString(), emptyList(), true).parameter(parameter);
            state.bind(constructor);
        }
        for (ExecutableElement setter : methodsIn(element.getEnclosedElements())) if(isSetter(setter))
            start.keyword(unCapitalize(setter.getSimpleName().toString().substring(3)), emptyList(), true)
                    .parameter(setter.getParameters().get(0))
                    .bind(setter);
        return dslModel;
    }

    private boolean isSetter(ExecutableElement element) {
        return element.getSimpleName().toString().startsWith("set") && element.getParameters().size() == 1;
    }

    private ParserState start(TypeModel model) {
        return new ParserContext(factory, model, null).new InitialState(STATIC);
    }

}
