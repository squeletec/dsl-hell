package fluent.dsl.plugin;

import fluent.dsl.model.ParameterModel;
import fluent.dsl.model.TypeModel;

import javax.annotation.processing.Filer;

public interface Plugin {

    TypeModel generate(ParameterModel parameterModel, Filer filer);

}
