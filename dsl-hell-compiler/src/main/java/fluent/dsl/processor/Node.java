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
