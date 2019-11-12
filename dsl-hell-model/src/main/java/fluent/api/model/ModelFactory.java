package fluent.api.model;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

public interface ModelFactory {


    VarModel parameter(TypeModel model, String parameterName);

    InterfaceModel interfaceModel(String packageName, String dslName);
    ClassModel classModel(String packageName, String dslName);

    MethodModel method(Collection<Modifier> modifiers, String method, List<VarModel> parameters);

    MethodModel method(String method, List<VarModel> parameters);

    default MethodModel method(String method, VarModel... parameters) {
        return method(method, asList(parameters));
    }

    StatementModel statementModel(VarModel impl, MethodModel body);

    TypeModel type(Element element);

    VarModel parameter(VariableElement parameterModel);

    MethodModel method(ExecutableElement method);

    MethodModel constructor(TypeModel builderModel, VarModel... parameters);

    VarModel constant(String name);

    MethodModel defaultMethod(String name, List<VarModel> parameters);

    MethodModel staticMethod(String name, List<VarModel> parameters);
}
