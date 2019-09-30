package fluent.dsl.plugin;

import fluent.dsl.model.ParameterModel;
import fluent.dsl.model.TypeModel;

import javax.annotation.processing.Filer;

public class SenderPlugin implements Plugin {

    @Override
    public TypeModel generate(ParameterModel parameterModel, Filer filer) {
        return null;
    }

}
