package fluent.dsl.model;

import fluent.api.model.*;
import fluent.api.model.lazy.LazyAnnotationModel;
import fluent.api.model.lazy.LazyMethodModel;
import fluent.api.model.lazy.LazyTypeModel;
import fluent.api.model.lazy.LazyVarModel;

import javax.lang.model.element.*;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

public class DslModelFactory {

    private final TypeModelFactoryVisitor visitor;

    public DslModelFactory(TypeModelFactoryVisitor visitor) {
        this.visitor = visitor;
    }

    public TypeModel type(List<AnnotationModel> emptyList, String fullName) {
        int i = fullName.lastIndexOf('.');
        return i < 0 ? type(emptyList, "", fullName) : type(emptyList, fullName.substring(0, i), fullName.substring(i + 1));
    }

    public VarModel parameter(List<AnnotationModel> emptyList, TypeModel type, String name) {
        return new LazyVarModel(() -> emptyList, false, true, type, name);
    }

    public static MethodModel method(List<AnnotationModel> annotations,
                                     boolean isStatic,
                                     boolean isPublic,
                                     List<TypeModel> typeParameters,
                                     TypeModel returnType,
                                     String name,
                                     List<VarModel> parameters,
                                     StatementModel... body) {
        return new LazyMethodModel(() -> annotations, isStatic, isPublic, () -> typeParameters, returnType, name, () -> parameters, () -> asList(body));
    }

    public AnnotationModel annotation(String type) {
        return new LazyAnnotationModel(Collections::emptyList, true, true, null);
    }

    public TypeModel type(List<AnnotationModel> annotations, String packageName, String simpleName) {
        return new LazyTypeModel(() -> annotations, true, true, Collections::emptyList, packageName, simpleName, packageName.isEmpty() ? simpleName : packageName + "." + simpleName, Collections::emptyList, Collections::emptyList);
    }

    public TypeModel type(Element element) {
        return visitor.visit(element.asType(), element);
    }

    public VarModel parameter(VariableElement element) {
        return visitor.parameter(element);
    }

    public MethodModel method(ExecutableElement element) {
        return visitor.method(element);
    }
    public static TypeModel type(List<AnnotationModel> annotations, List<TypeModel> typeParameters, String packageName, String simpleName, Supplier<List<MethodModel>> methodSupplier) {
        TypeModel raw = new LazyTypeModel(
                () -> annotations,
                true, true,
                Collections::emptyList,
                packageName,
                simpleName,
                packageName.isEmpty() ? simpleName : packageName + "." + simpleName,
                methodSupplier,
                Collections::emptyList
        );
        if(typeParameters.isEmpty())
            return raw;
        String collect = typeParameters.stream().map(TypeModel::fullName).collect(joining(", ", "<", ">"));
        return new LazyTypeModel(
                () -> annotations,
                true, true,
                () -> typeParameters,
                packageName,
                simpleName + collect,
                raw.fullName() + collect,
                false,
                () -> raw,
                null,
                methodSupplier,
                Collections::emptyList
        );
    }

    public DslModel dsl(MethodModel factory, String delegate) {
        return new DslModel(factory, delegate);
    }

    public StatementModel statementModel(VarModel target, MethodModel method) {
        return new StatementModel() {
            @Override public String toString() {
                return (method.returnsValue() ? "return " : "") + (method.isStatic() ? target.type().fullName() : target.name()) + "." + method.name() + "(" + method.parameters().stream().map(VarModel::name).collect(joining(", ")) + ");";
            }
        };
    }
}
