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

import fluent.api.model.MethodModel;
import fluent.api.model.StatementModel;
import fluent.api.model.TypeModel;
import fluent.api.model.VarModel;
import fluent.dsl.model.DslModelFactory;

import java.util.*;
import java.util.function.Supplier;

import static fluent.dsl.model.DslModelFactory.type;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

final class Node implements Supplier<List<MethodModel>> {
    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final MethodModel methodModel;

    private Node(MethodModel methodModel) {
        this.methodModel = methodModel;
    }

    private void traverse(TypeModel t, List<TypeModel> out) {
        if(t.isTypeVariable())
            out.add(t);
        else
            t.typeParameters().forEach(p -> traverse(p, out));
    }

    private List<TypeModel> usedTypeParameters(List<VarModel> parameters) {
        List<TypeModel> out = new ArrayList<>();
        parameters.stream().map(VarModel::type).forEach(t -> traverse(t, out));
        return out;
    }

    public Node(boolean isStatic, TypeModel typeModel, List<TypeModel> typeParameters, String packageName, String className, String methodName, List<String> aliases, List<VarModel> parameters, StatementModel... bindingModel) {
        Map<String, TypeModel> map = new LinkedHashMap<>();
        typeParameters.forEach(p -> map.put(p.fullName(), p));
        usedTypeParameters(parameters).forEach(t -> map.put(t.fullName(), t));
        ArrayList<TypeModel> newParameters = new ArrayList<>(map.values());
        if(isNull(typeModel)) {
            typeModel = type(emptyList(), newParameters, packageName, className, this);
        }
        List<TypeModel> methodTypeParameters = isStatic ? newParameters : newParameters.subList(typeParameters.size(), newParameters.size());
        this.methodModel = DslModelFactory.method(emptyList(), isStatic, true, methodTypeParameters, typeModel, methodName, parameters, bindingModel);
        this.methodModel.metadata().put("aliases", aliases);
    }

    public Node add(TypeModel typeModel, String className, String methodName, List<String> aliases, List<VarModel> parameters, StatementModel[] bindingModel) {
        return nodes.computeIfAbsent(className, key -> new Node(false, typeModel, methodModel.returnType().typeParameters(), "", className, methodName, aliases, parameters, bindingModel));
    }

    @Override
    public List<MethodModel> get() {
        return nodes.values().stream().map(n -> n.methodModel).collect(toList());
    }

    public MethodModel methodModel() {
        return methodModel;
    }
}
