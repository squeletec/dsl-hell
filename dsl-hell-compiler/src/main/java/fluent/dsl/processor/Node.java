package fluent.dsl.processor;

import fluent.api.model.MethodModel;
import fluent.api.model.StatementModel;
import fluent.api.model.TypeModel;
import fluent.api.model.VarModel;
import fluent.dsl.model.DslModelFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    public Node(TypeModel typeModel, List<TypeModel> typeParameters, String packageName, String className, String methodName, List<VarModel> parameters, StatementModel... bindingModel) {
        if(isNull(typeModel)) {
            typeModel = type(emptyList(), typeParameters, packageName, className, this);
        }
        this.methodModel = DslModelFactory.method(emptyList(), false, true, emptyList(), typeModel, methodName, parameters, bindingModel);
    }
    public static Node node() {
        return new Node(null);
    }
    public Node add(TypeModel typeModel, String className, String methodName, List<VarModel> parameters, StatementModel[] bindingModel) {
        return nodes.computeIfAbsent(className, key -> new Node(typeModel, methodModel.returnType().typeParameters(), "", className, methodName, parameters, bindingModel));
    }
    @Override public List<MethodModel> get() {
        return nodes.values().stream().map(n -> n.methodModel).collect(toList());
    }

    public MethodModel methodModel() {
        return methodModel;
    }
}
