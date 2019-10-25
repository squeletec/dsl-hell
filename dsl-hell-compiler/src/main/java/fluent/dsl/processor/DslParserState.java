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

import fluent.api.model.*;
import fluent.dsl.model.*;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.List;

import static fluent.dsl.model.DslUtils.capitalize;
import static fluent.dsl.model.DslUtils.simpleName;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;

public class DslParserState {
    private final DslModelFactory factory;
    private final DslModel dsl;
    private final VarModel impl;

    public DslParserState(DslModelFactory factory, DslModel dsl, VarModel impl) {
        this.factory = factory;
        this.dsl = dsl;
        this.impl = impl;
    }

    public class InitialState implements DslParser.State {
        Node node;
        public InitialState(Node node) {
            this.node = node;
        }
        @Override public DslParser.State annotation(AnnotationModel annotationModel) {
            return this;
        }
        @Override public DslParser.State keyword(String name, List<String> aliases, boolean useVarargs) {
            return new KeywordState(node, name, aliases, useVarargs);
        }
        @Override public DslParser.State method(String name) {
            return new DecideState(node, name, false);
        }
        @Override public DslParser.State constant(String name) {
            return this;
        }
        @Override public DslParser.State parameter(VariableElement parameterModel) {
            return this;
        }
        @Override public void bind(ExecutableElement method) {
        }
    }


    public class KeywordState implements DslParser.State {
        private final Node node;
        private final String methodName;
        private final List<String> aliases;
        private final List<VarModel> parameters = new ArrayList<>();
        final boolean useVarargs;

        public KeywordState(Node node, String name, List<String> aliases, boolean useVarargs) {
            this.node = node;
            this.methodName = name;
            this.aliases = aliases;
            this.useVarargs = useVarargs;
        }
        @Override public DslParser.State annotation(AnnotationModel annotationModel) {
            return this;
        }
        @Override public DslParser.State keyword(String name, List<String> aliases, boolean useVarargs) {
            return new KeywordState(finish(null), name, aliases, useVarargs);
        }
        @Override public DslParser.State method(String name) {
            return new DecideState(finish(null), name, false);
        }
        @Override public DslParser.State constant(String name) {
            parameters.add(dsl.addConstant(factory.parameter(emptyList(), factory.type(emptyList(), "", name), name)));
            return this;
        }
        @Override public DslParser.State parameter(VariableElement parameterModel) {
            parameters.add(factory.parameter(parameterModel));
            return this;
        }
        @Override public void bind(ExecutableElement method) {
            MethodModel body = factory.method(method);
            StatementModel binding = factory.statementModel(impl, body);
            finish(body.returnType(), binding);
        }
        private Node finish(TypeModel returnTypeModel, StatementModel... bindingModel) {
            String className = capitalize(methodName) + parameters.stream().map(p -> simpleName(p.type())).collect(joining());
            return node.add(returnTypeModel, className, methodName, aliases, parameters, bindingModel);
        }
    }

    public class DecideState extends KeywordState implements DslParser.State {
        private final Node node;
        private final String methodName;

        public DecideState(Node node, String name, boolean useVarargs) {
            super(node, name, emptyList(), useVarargs);
            this.node = node;
            this.methodName = name;
        }
        @Override public DslParser.State keyword(String name, List<String> aliases, boolean useVarargs) {
            return new KeywordState(node, name, aliases, useVarargs);
        }
        @Override public DslParser.State constant(String name) {
            return keyword(methodName, emptyList(), useVarargs).constant(name);
        }
        @Override public DslParser.State parameter(VariableElement parameterModel) {
            return keyword(methodName, emptyList(), useVarargs).parameter(parameterModel);
        }
    }
}
