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

import fluent.api.model.*;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static fluent.dsl.model.DslUtils.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;

public class ParserContext {
    private final ModelFactory factory;
    private final TypeModel dsl;
    private final VarModel impl;

    public ParserContext(ModelFactory factory, TypeModel dsl, VarModel impl) {
        this.factory = factory;
        this.dsl = dsl;
        this.impl = impl;
    }

    public abstract class AbstractState implements ParserState {
        final Node node;
        public AbstractState(Node node) {
            this.node = node;
        }
        @Override public ParserState annotation(AnnotationModel annotationModel) {
            return this;
        }
        @Override public ParserState keyword(String name, List<String> aliases, boolean useVarargs) {
            return new KeywordState(finish(null), name, aliases, useVarargs);
        }
        @Override public ParserState method(String name) {
            return new DecideState(finish(null), name, false);
        }
        Node finish(TypeModel returnTypeModel, StatementModel... bindingModel) {
            return node;
        }
    }

    public class InitialState extends AbstractState {
        public InitialState(MethodModel factoryMethod) {
            super(new Node(factoryMethod));
        }
        @Override public ParserState constant(String name) {
            return this;
        }
        @Override public ParserState parameter(VariableElement parameterModel) {
            return this;
        }
        @Override public void bind(ExecutableElement method) {
        }
    }


    public class KeywordState extends AbstractState {
        private final String methodName;
        private final List<String> aliases;
        private final List<VarModel> parameters = new ArrayList<>();
        final boolean useVarargs;

        public KeywordState(Node node, String name, List<String> aliases, boolean useVarargs) {
            super(node);
            this.methodName = name;
            this.aliases = aliases;
            this.useVarargs = useVarargs;
        }
        @Override public ParserState constant(String name) {
            VarModel parameter = factory.parameter(factory.type("", name), name);
            dsl.fields().add(parameter);
            parameters.add(parameter);
            return this;
        }
        @Override public ParserState parameter(VariableElement parameterModel) {
            parameters.add(factory.parameter(parameterModel));
            return this;
        }
        @Override public void bind(ExecutableElement method) {
            MethodModel body = factory.method(method);
            StatementModel binding = factory.statementModel(impl, body);
            if(body.isConstructor()) {
                finish(factory.type(method.getEnclosingElement()), binding);
            } else {
                finish(body.returnType(), binding);
            }
        }
        Node finish(TypeModel returnTypeModel, StatementModel... bindingModel) {
            String className = capitalize(methodName) + parameters.stream().map(p -> simpleName(p.type())).collect(joining());
            return node.add(returnTypeModel, className, methodName, aliases, parameters, bindingModel);
        }
    }

    public class DecideState extends KeywordState implements ParserState {
        private final String methodName;
        public DecideState(Node node, String name, boolean useVarargs) {
            super(node, name, emptyList(), useVarargs);
            this.methodName = name;
        }
        @Override public ParserState keyword(String name, List<String> aliases, boolean useVarargs) {
            return new KeywordState(node, name, aliases, useVarargs);
        }
        @Override public ParserState constant(String name) {
            return keyword(methodName, emptyList(), useVarargs).constant(name);
        }
        @Override public ParserState parameter(VariableElement parameterModel) {
            return keyword(methodName, emptyList(), useVarargs).parameter(parameterModel);
        }
    }

    final class Node {
        private final Map<String, Node> nodes = new LinkedHashMap<>();
        private final MethodModel methodModel;

        private Node(MethodModel methodModel) {
            this.methodModel = methodModel;
        }

        public Node add(TypeModel typeModel, String className, String methodName, List<String> aliases, List<VarModel> parameters, StatementModel[] bindingModel) {
            return nodes.computeIfAbsent(className, key -> {
                List<TypeModel> typeParameters = this.methodModel.returnType().typeParameters();
                Map<String, TypeModel> map = new LinkedHashMap<>();
                typeParameters.forEach(p -> map.put(p.fullName(), p));
                usedTypeParameters(parameters).forEach(t -> map.put(t.fullName(), t));
                ArrayList<TypeModel> newParameters = new ArrayList<>(map.values());
                List<TypeModel> methodTypeParameters = newParameters.subList(typeParameters.size(), newParameters.size());

                MethodModel methodModel = factory.method(methodName, parameters).returnType(isNull(typeModel)
                        ? factory.type("", className).typeParameters(newParameters)
                        : typeModel).typeParameters(methodTypeParameters).owner(this.methodModel.returnType());
                methodModel.metadata().put("aliases", aliases);
                methodModel.body().addAll(asList(bindingModel));
                this.methodModel.returnType().methods().add(methodModel);
                return new Node(methodModel);
            });
        }
    }
}
