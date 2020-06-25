package fluent.dsl.plugin.check;

import fluent.api.model.*;
import fluent.dsl.Dsl;
import fluent.dsl.processor.DslAnnotationProcessorPlugin;
import fluent.dsl.processor.DslAnnotationProcessorPluginFactory;
import fluent.validation.Check;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static fluent.dsl.plugin.DslUtils.isGetter;
import static fluent.dsl.plugin.DslUtils.override;
import static java.util.Arrays.asList;

public class CheckDslPlugin implements DslAnnotationProcessorPlugin {
    private final ModelFactory factory;

    public CheckDslPlugin(ModelFactory factory) {
        this.factory = factory;
        boxedTypes.put("int", factory.classModel("java.lang", "Integer"));
        boxedTypes.put("boolean", factory.classModel("java.lang", "Boolean"));
        boxedTypes.put("long", factory.classModel("java.lang", "Long"));
    }

    @Override
    public boolean isFor(Element element) {
        return (element instanceof VariableElement) && factory.parameter((VariableElement) element).type().rawType().fullName().equals(Check.class.getCanonicalName());
    }

    @Override
    public TypeModel<?> process(Element element, Dsl dsl) {
        VarModel v = factory.parameter((VariableElement) element);
        TypeModel<?> typeModel = v.type().typeParameters().get(0);
        String packageName = override(dsl.packageName(), typeModel.packageName());
        String className = override(dsl.className(), typeModel.rawType().simpleName() + "With");
        ClassModel fluentCheck = factory.classModel(packageName, className);
        //fluentCheck.superClass(factory.classModel("fluent.validation", "AbstractCheckDsl").typeParameters(asList(fluentCheck, typeModel)));
        for(MethodModel method : typeModel.methods())
            if(isGetter(method))
                processGetter(fluentCheck, method);
        return fluentCheck;
    }

    private boolean isGetter(MethodModel method) {
        return method.name().startsWith("get") && method.parameters().isEmpty();
    }

    private void processGetter(ClassModel fluentCheck, MethodModel method) {
        String name = method.name();
        //fluentCheck.methods().add(factory.method(name, factory.parameter(method.returnType(), "expectedValue")).returnType(fluentCheck));
        //fluentCheck.methods().add(factory.method(name, factory.parameter(factory.interfaceModel("fluent.validation", "Check<? super " + boxed(method.returnType()).fullName() + ">"), "expectation")).returnType(fluentCheck));
    }

    private TypeModel<?> boxed(TypeModel<?> type) {
        return boxedTypes.getOrDefault(type.fullName(), type);
    }

    private final Map<String, TypeModel<?>> boxedTypes = new HashMap<>();

    public static class Factory implements DslAnnotationProcessorPluginFactory {

        @Override
        public DslAnnotationProcessorPlugin createPlugin(ModelFactory factory) {
            return new CheckDslPlugin(factory);
        }
    }
}
