package fluent.dsl.parser;

import fluent.api.model.AnnotationModel;
import fluent.api.model.MethodModel;
import fluent.api.model.TypeModel;

import javax.lang.model.element.VariableElement;
import java.util.List;

public interface ParserState {
    ParserState annotation(AnnotationModel annotationModel);
    ParserState keyword(String name, List<String> aliases, boolean useVarargs);
    ParserState method(String name);
    ParserState constant(String name);
    ParserState parameter(VariableElement parameterModel);
    void bind(MethodModel method);
    void bind(MethodModel method, TypeModel returnType);
}
