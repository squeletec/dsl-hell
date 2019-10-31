package fluent.dsl.parser;

import fluent.api.model.AnnotationModel;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import java.util.List;

public interface ParserState {
    ParserState annotation(AnnotationModel annotationModel);
    ParserState keyword(String name, List<String> aliases, boolean useVarargs);
    ParserState method(String name);
    ParserState constant(String name);
    ParserState parameter(VariableElement parameterModel);
    void bind(ExecutableElement method);
}
